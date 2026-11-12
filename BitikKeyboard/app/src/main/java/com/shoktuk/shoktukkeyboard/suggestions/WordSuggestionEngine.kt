package com.shoktuk.shoktukkeyboard.suggestions

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Prefix autocomplete, typo tolerance and glide matching for the Latin keyboard.
 *
 * The word list (Kyrgyz Cyrillic, merged from many corpora) is split into one asset file per first letter,
 * `words/kyw_<hex of the letter>.txt`, most frequent word first. Only the file(s) for the letter being typed are read,
 * transliterated to Kyrgyz Latin and kept in memory (a small cache holds the rest). Files load on a background thread
 * and [onShardLoaded] fires when one a lookup was waiting for is ready. Matching ignores hard/soft к/г: the key folds
 * q→k and ğ→g, so "kal" finds "qalğan".
 */
object WordSuggestionEngine {
    /** A completion in the keyboard's own script ([word]) with its Cyrillic and Latin spellings. */
    class Suggestion(val word: String, val cyr: String, val latin: String)

    private class Entry(val word: String, val key: String, val pct: Float, val len: Int, val cyr: String)
    private class Shard(val entries: Array<Entry>)

    private const val ALPHABET = "абвгдеёжзийклмнңоөпрстуүфхцчшщыэюя"
    private const val MAX_CACHED_ENTRIES = 250_000

    private lateinit var appContext: Context
    private var configured = false
    private val main = Handler(Looper.getMainLooper())
    private val io = Executors.newSingleThreadExecutor()
    private val cache = HashMap<Char, Shard>()
    private val recency = ArrayList<Char>()
    private val loading = HashSet<Char>()
    private var inUse = HashSet<Char>()
    private var pendingRefresh = false
    private var exceptions = HarmonyExceptions()
    private val latinFirst = HashMap<Char, MutableList<Char>>()  // typed Latin first letter -> Cyrillic first letters

    /** Cyrillic typing matches the word files as they are; Latin typing matches their transliteration. */
    private var cyrillic = false

    /** Called on the main thread when a file a lookup was waiting for has finished loading. */
    var onShardLoaded: (() -> Unit)? = null

    private var zh = "j"
    private var ch = "ç"
    private var ng = "ñ"
    private var o = "ö"
    private var signature = ""

    /**
     * Sets the script being typed and the Latin letter settings (the engine spells Latin words the way the keys do).
     * Cached files are dropped when any of them changes.
     */
    fun configure(context: Context, cyrillic: Boolean, zh: String = "j", ch: String = "ç", ng: String = "ñ", o: String = "ö") {
        if (!configured) {
            appContext = context.applicationContext
            exceptions = HarmonyExceptions.load(appContext)
            configured = true
        }
        val sig = "$cyrillic|$zh|$ch|$ng|$o"
        if (sig == signature) return
        signature = sig
        this.cyrillic = cyrillic
        this.zh = zh; this.ch = ch; this.ng = ng; this.o = o
        val map = buildTable()
        table = arrayOfNulls(0xD0)
        for ((c, v) in map) if (c.code in 0x430 until 0x500) table[c.code - 0x430] = v
        extra = map
        cache.clear(); recency.clear(); loading.clear()
        generation++
        latinFirst.clear()
        if (!cyrillic) for (c in ALPHABET) {
            val first = fold(transliterate(c.toString(), HarmonyExceptions())).firstOrNull() ?: continue
            latinFirst.getOrPut(first) { ArrayList() }.add(c)
        }
    }

    private var generation = 0

    /** Reads the writing system and Latin letter settings from the keyboard service. */
    fun configureFor(service: com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService) {
        val kiril = com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService.current_writingSystem == com.shoktuk.shoktukkeyboard.project.data.WritingSystem.Kiril
        with(com.shoktuk.shoktukkeyboard.project.data.SettingsManager) {
            configure(service, kiril, service.latinZH.id, service.latinCH.id, service.latinNG.id, service.latinO.id)
        }
    }

    // ---------------------------------------------------------------- typing

    /** Pairs of keys that sit next to each other, encoded `(a shl 32) or b`, for slips of the finger. */
    fun pair(a: Char, b: Char): Long = (a.code.toLong() shl 32) or b.code.toLong()

    /** Up to [limit] completions of [prefix], shortest first among the most frequent, then near-misses for typos. */
    fun completions(prefix: String, limit: Int = 40, neighbours: Set<Long> = emptySet()): List<Suggestion> {
        if (!configured || prefix.isEmpty()) return emptyList()
        val lower = if (cyrillic) prefix.lowercase() else fold(prefix.lowercase())
        val first = lower.firstOrNull() ?: return emptyList()
        val letters = lettersFor(first) ?: return emptyList()
        inUse = HashSet(letters)

        val matches = ArrayList<Entry>()
        for (letter in letters) {
            val shard = shard(letter, wait = false)
            if (shard == null) { pendingRefresh = true; continue }
            val entries = shard.entries
            var low = 0
            var high = entries.size
            while (low < high) {
                val mid = (low + high) ushr 1
                if (entries[mid].key < lower) low = mid + 1 else high = mid
            }
            var i = low
            while (i < entries.size && entries[i].key.startsWith(lower)) {
                if (entries[i].key != lower) matches.add(entries[i])
                i++
            }
        }
        // Most frequent matches first (so rare one-off strings never get in), then the shortest first.
        if (matches.size > 120) {
            // Keep the 120 most frequent without sorting everything: find the cut-off rank first.
            val ranks = FloatArray(matches.size) { matches[it].pct }
            ranks.sort()
            val cut = ranks[119]
            matches.retainAll { it.pct <= cut }
            if (matches.size > 120) { matches.sortBy { it.pct }; matches.subList(120, matches.size).clear() }
        }
        matches.sortWith(compareBy<Entry>({ it.len }, { it.pct }))

        val seen = HashSet<String>()
        val result = ArrayList<Suggestion>()
        fun add(e: Entry): Boolean {
            if (!seen.add(e.word)) return false
            val w = applyCase(prefix, e.word)
            val c = applyCase(prefix, e.cyr)
            result.add(Suggestion(w, c, if (cyrillic) applyCase(prefix, transliterate(e.cyr, exceptions)) else w))
            return result.size == limit
        }
        for (m in matches) if (add(m)) return result
        if (result.size < 6 && lower.length >= 3) {
            for (e in fuzzy(lower, first, neighbours)) if (add(e)) break
        }
        return result
    }

    // ---------------------------------------------------------------- Bitik

    /** Latin units compared loosely: q/ğ fold to k/g, ŋ and ñ to n (the ң spelling depends on a setting). */
    fun bitikNorm(s: String): String = s.replace('q', 'k').replace('ğ', 'g').replace('ŋ', 'n').replace('ñ', 'n')

    /**
     * Words (Latin spelling) the typed Bitik glyphs can stand for. `readings[i]` lists every Latin spelling glyph `i`
     * can have (already through [bitikNorm]), so a glyph for "o" or "u" finds both bolot and bulut. A vowel of the word
     * that no glyph covers is skipped (short writing), never two in a row. Exact fits come first, then longer words.
     */
    fun bitikCompletions(readings: List<List<String>>, limit: Int = 12): List<Suggestion> {
        if (!configured || readings.isEmpty()) return emptyList()
        val firsts = readings[0].mapNotNull { it.firstOrNull() }.toSet()
        val letters = LinkedHashSet<Char>()
        for (f in firsts) lettersFor(f)?.let { letters.addAll(it) }
        inUse = HashSet(letters)

        class Hit(val entry: Entry, val exact: Boolean)
        val hits = ArrayList<Hit>()
        for (letter in letters) {
            val shard = shard(letter, wait = false)
            if (shard == null) { pendingRefresh = true; continue }
            for (entry in shard.entries) {
                val r = bitikMatch(entry.key, readings)
                if (r > 0) hits.add(Hit(entry, r == 2))
            }
        }
        hits.sortBy { it.entry.pct }
        if (hits.size > 120) hits.subList(120, hits.size).clear()
        hits.sortWith(Comparator { a, b ->
            if (a.exact != b.exact) return@Comparator if (a.exact) -1 else 1
            if (a.exact) return@Comparator a.entry.pct.compareTo(b.entry.pct)
            if (a.entry.len != b.entry.len) a.entry.len.compareTo(b.entry.len) else a.entry.pct.compareTo(b.entry.pct)
        })
        val seen = HashSet<String>()
        val out = ArrayList<Suggestion>()
        for (h in hits) {
            if (!seen.add(h.entry.word)) continue
            out.add(Suggestion(h.entry.word, h.entry.cyr, h.entry.word))
            if (out.size == limit) break
        }
        return out
    }

    private fun nc(c: Char): Char = when (c) { 'q' -> 'k'; 'ğ' -> 'g'; 'ŋ', 'ñ' -> 'n'; else -> c }

    private fun isVowel(c: Char) = c == 'a' || c == 'e' || c == 'ı' || c == 'i' || c == 'o' || c == 'u' || c == 'ö' || c == 'ü'

    /** 2 = the glyphs spell exactly this word, 1 = the word starts with what they spell, 0 = no match. */
    private fun bitikMatch(w: String, g: List<List<String>>): Int {
        val n = w.length
        fun go(gi: Int, wi: Int): Int {
            if (gi == g.size) return if (wi == n) 2 else 1
            if (wi >= n) return 0
            var best = 0
            for (r in g[gi]) {
                var ok = wi + r.length <= n
                if (ok) for (k in r.indices) if (nc(w[wi + k]) != r[k]) { ok = false; break }
                if (ok) {
                    best = max(best, go(gi + 1, wi + r.length))
                    if (best == 2) return 2
                }
            }
            if (gi > 0 && isVowel(nc(w[wi])) && (wi == 0 || !isVowel(nc(w[wi - 1])))) best = max(best, go(gi, wi + 1))
            return best
        }
        return go(0, 0)
    }

    /** Words whose beginning is within about one typo of [typed] (a neighbouring key costs half), nearest first. */
    private fun fuzzy(typed: String, first: Char, neighbours: Set<Long>): List<Entry> {
        val m = typed.length
        val maxCost = if (m >= 5) 3 else 2  // doubled: a neighbouring-key slip costs 1, any other edit 2
        updateNeighbourBits(neighbours)
        val starts = ArrayList<Char>().apply { add(first) }
        for (c in ALPHABET + "qğ") if (neighbours.contains(pair(first, c))) starts.add(c)
        val letters = LinkedHashSet<Char>()
        for (s in starts) lettersFor(s)?.let { letters.addAll(it) }
        inUse.addAll(letters)

        val t = IntArray(m) { typed[it].code }
        var previous = IntArray(m + 1)
        var row = IntArray(m + 1)
        class Hit(val entry: Entry, val cost: Int)
        val found = ArrayList<Hit>()
        for (letter in letters) {
            val shard = shard(letter, wait = false)
            if (shard == null) { pendingRefresh = true; continue }
            for (entry in shard.entries) {
                if (entry.pct >= 0.4f) continue
                val n = entry.len
                if (n < m - 1 || n > 39 || entry.key == typed) continue
                val key = entry.key
                for (j in 0..m) previous[j] = j * 2
                var best = Int.MAX_VALUE
                var i = 0
                while (i < n) {
                    row[0] = (i + 1) * 2
                    var rowMin = row[0]
                    val wc = key[i].code
                    for (j in 0 until m) {
                        val tc = t[j]
                        val sub = if (wc == tc) 0 else if (isNeighbour(wc, tc)) 1 else 2
                        val v = min(min(previous[j + 1] + 2, row[j] + 2), previous[j] + sub)
                        row[j + 1] = v
                        if (v < rowMin) rowMin = v
                    }
                    if (i + 1 >= m - 1 && row[m] < best) best = row[m]
                    if (rowMin > maxCost) break
                    val tmp = previous; previous = row; row = tmp
                    i++
                }
                if (best <= maxCost) found.add(Hit(entry, best))
            }
        }
        found.sortWith(compareBy<Hit>({ it.cost }, { it.entry.pct }))
        return found.take(12).map { it.entry }
    }

    // Adjacent-key pairs as a bit table (index = a * 0x500 + b), rebuilt only when the set changes. Avoids boxing in the hot loops.
    private val neighbourBits = LongArray(0x500 * 0x500 / 64 + 1)
    private var neighbourSource: Set<Long>? = null

    private fun updateNeighbourBits(pairs: Set<Long>) {
        if (pairs === neighbourSource) return
        neighbourSource = pairs
        neighbourBits.fill(0L)
        for (p in pairs) {
            val a = (p ushr 32).toInt(); val b = (p and 0xFFFFFFFFL).toInt()
            if (a < 0x500 && b < 0x500) { val bit = a * 0x500 + b; neighbourBits[bit ushr 6] = neighbourBits[bit ushr 6] or (1L shl (bit and 63)) }
        }
    }

    private fun isNeighbour(a: Int, b: Int): Boolean {
        if (a >= 0x500 || b >= 0x500) return false
        val bit = a * 0x500 + b
        return neighbourBits[bit ushr 6] and (1L shl (bit and 63)) != 0L
    }

    // ----------------------------------------------------------------- glide

    /**
     * Words a glide can mean, best first. Each candidate's ideal path through its key centres is compared with the
     * finger [trace]: it must start where the finger went down, end where it lifted, have a similar length, and follow
     * the same shape. Frequency breaks ties. [centres] maps each typable letter (and held letter) to its key centre.
     */
    fun glideMatches(trace: List<PointF>, centres: Map<Char, PointF>, keyWidth: Float, startLetter: Char, limit: Int = 8): List<String> {
        if (!configured || trace.size < 2 || keyWidth <= 0f) return emptyList()
        val begin = trace.first(); val end = trace.last()
        // The word's first letter is the key the swipe started on, or one beside it.
        val starts = LinkedHashSet<Char>().apply { add(startLetter) }
        centres[startLetter]?.let { c -> for ((l, p) in centres) if (hypot(p.x - c.x, p.y - c.y) < keyWidth * 2.2f) starts.add(l) }
        val letters = LinkedHashSet<Char>()
        for (s in starts) lettersFor(s)?.let { letters.addAll(it) }
        inUse = HashSet(letters)

        val kw = keyWidth.toDouble()
        val has = BooleanArray(0x500); val cx = DoubleArray(0x500); val cy = DoubleArray(0x500)
        for ((l, p) in centres) if (l.code < 0x500) { has[l.code] = true; cx[l.code] = p.x.toDouble(); cy[l.code] = p.y.toDouble() }
        val traceXY = DoubleArray(trace.size * 2).also { for (i in trace.indices) { it[2 * i] = trace[i].x.toDouble(); it[2 * i + 1] = trace[i].y.toDouble() } }
        val sample = resample(traceXY, 32)
        val traceLength = length(traceXY)
        class Hit(val word: String, val cost: Double)
        val hits = ArrayList<Hit>()
        var pts = DoubleArray(80)
        for (letter in letters) {
            val shard = shard(letter, wait = true) ?: continue
            for (entry in shard.entries) {
                if (entry.pct >= 0.5f) continue
                val w = entry.word
                val f = w[0].code; val l = w[w.length - 1].code
                if (f >= 0x500 || l >= 0x500 || !has[f] || !has[l]) continue
                if (hypot(cx[f] - begin.x, cy[f] - begin.y) >= kw * 1.1 || hypot(cx[l] - end.x, cy[l] - end.y) >= kw * 1.3) continue
                if (pts.size < w.length * 2) pts = DoubleArray(w.length * 2)
                var count = 0
                var ok = true
                var prev = -1
                for (c in w) {
                    val u = c.code
                    if (u >= 0x500 || !has[u]) { ok = false; break }
                    if (u != prev) { pts[2 * count] = cx[u]; pts[2 * count + 1] = cy[u]; count++ }
                    prev = u
                }
                if (!ok || count < 2) continue
                val ideal = length(pts, count)
                val ratio = ideal / max(traceLength, 1.0)
                if (ratio <= 0.55 || ratio >= 1.8) continue
                val rs = resample(pts, 32, count)
                var sum = 0.0
                for (i in 0 until 32) sum += hypot(rs[2 * i] - sample[2 * i], rs[2 * i + 1] - sample[2 * i + 1])
                val shape = sum / 32 / kw
                if (shape >= 1.4) continue
                hits.add(Hit(w, shape + entry.pct * 1.2))
            }
        }
        hits.sortBy { it.cost }
        val seen = HashSet<String>()
        return hits.filter { seen.add(it.word) }.take(limit).map { it.word }
    }

    private fun length(xy: DoubleArray, count: Int = xy.size / 2): Double {
        var total = 0.0
        for (i in 1 until count) total += hypot(xy[2 * i] - xy[2 * i - 2], xy[2 * i + 1] - xy[2 * i - 1])
        return total
    }

    /** [n] points spaced evenly along the polyline. */
    private fun resample(xy: DoubleArray, n: Int, count: Int = xy.size / 2): DoubleArray {
        val out = DoubleArray(n * 2)
        val total = length(xy, count)
        if (count < 2 || total <= 0.0) { for (i in 0 until n) { out[2 * i] = xy[0]; out[2 * i + 1] = xy[1] }; return out }
        var seg = 0
        var walked = 0.0
        for (i in 0 until n) {
            val target = total * i / (n - 1)
            while (seg < count - 2) {
                val d = hypot(xy[2 * seg + 2] - xy[2 * seg], xy[2 * seg + 3] - xy[2 * seg + 1])
                if (walked + d < target) { walked += d; seg++ } else break
            }
            val d = hypot(xy[2 * seg + 2] - xy[2 * seg], xy[2 * seg + 3] - xy[2 * seg + 1])
            val t = if (d > 0) min(max((target - walked) / d, 0.0), 1.0) else 0.0
            out[2 * i] = xy[2 * seg] + (xy[2 * seg + 2] - xy[2 * seg]) * t
            out[2 * i + 1] = xy[2 * seg + 1] + (xy[2 * seg + 3] - xy[2 * seg + 1]) * t
        }
        return out
    }

    // ------------------------------------------------------- files and cache

    private fun lettersFor(first: Char): List<Char>? =
        if (cyrillic) (if (ALPHABET.indexOf(first) >= 0) listOf(first) else null) else latinFirst[if (first == 'q') 'k' else if (first == 'ğ') 'g' else first]

    private fun shard(letter: Char, wait: Boolean): Shard? {
        cache[letter]?.let { recency.remove(letter); recency.add(letter); return it }
        if (wait) {
            val s = loadEntries(letter)
            store(s, letter)
            return s
        }
        if (!loading.add(letter)) return null
        val gen = generation
        io.execute {
            val s = loadEntries(letter)
            main.post {
                if (gen != generation) return@post
                loading.remove(letter)
                store(s, letter)
                if (pendingRefresh) { pendingRefresh = false; onShardLoaded?.invoke() }
            }
        }
        return null
    }

    private fun store(shard: Shard, letter: Char) {
        cache[letter] = shard
        recency.remove(letter); recency.add(letter)
        var total = cache.values.sumOf { it.entries.size }
        while (total > MAX_CACHED_ENTRIES) {
            val oldest = recency.firstOrNull { it != letter && !inUse.contains(it) } ?: break
            recency.remove(oldest)
            cache.remove(oldest)?.let { total -= it.entries.size }
        }
    }

    private fun loadEntries(letter: Char): Shard {
        val name = "words/kyw_%04x.txt".format(letter.code)
        val words = try {
            appContext.assets.open(name).bufferedReader().useLines { seq -> seq.map { it.trim() }.filter { it.isNotEmpty() }.toList() }
        } catch (e: java.io.IOException) {
            return Shard(emptyArray())
        }
        val total = max(words.size, 1).toFloat()
        val list = ArrayList<Entry>(words.size)
        for ((i, w) in words.withIndex()) {
            val word = if (cyrillic) w else transliterate(w, exceptions)
            list.add(Entry(word, if (cyrillic) word else fold(word), i / total, word.length, w))
        }
        list.sortBy { it.key }
        return Shard(list.toTypedArray())
    }

    private fun fold(s: String): String = s.replace('q', 'k').replace('ğ', 'g')

    private fun applyCase(typed: String, word: String): String {
        if (typed.length > 1 && typed == typed.uppercase()) return word.uppercase()
        if (typed.first().isUpperCase()) return word.replaceFirstChar { it.uppercase() }
        return word
    }

    // -------------------------------------------------- Cyrillic -> Kyrgyz Latin

    private var table: Array<String?> = arrayOfNulls(0xD0)  // index = letter - 0x430 (а…я, ё, ң, ү, ө)
    private var extra: Map<Char, String> = emptyMap()
    private fun buildTable(): Map<Char, String> = mapOf(
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'е' to "e", 'ё' to "yo", 'ж' to zh, 'з' to "z",
        'и' to "i", 'й' to "y", 'к' to "k", 'л' to "l", 'м' to "m", 'н' to "n", 'ң' to ng, 'о' to "o", 'ө' to o,
        'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t", 'у' to "u", 'ү' to "ü", 'ф' to "f", 'х' to "h", 'ц' to "ts",
        'ч' to ch, 'ш' to "ş", 'щ' to "ş", 'ы' to "ı", 'э' to "e", 'ю' to "yu", 'я' to "ya", 'ъ' to "", 'ь' to "", '-' to "-",
    )

    /** Two stages: first the vowel-harmony choice of q/ğ vs k/g, then the letter table. */
    private fun transliterate(word: String, exceptions: HarmonyExceptions): String {
        val hard = exceptions.hardness(word)
        val sb = StringBuilder(word.length + 2)
        var guttural = 0
        for (c in word) {
            if (c == 'к' || c == 'г') {
                val h = hard[guttural++]
                sb.append(if (c == 'к') (if (h) 'q' else 'k') else (if (h) 'ğ' else 'g'))
            } else { val t = if (c.code in 0x430 until 0x500) table[c.code - 0x430] else null; sb.append(t ?: extra[c] ?: c.toString()) }
        }
        return sb.toString()
    }
}

/** к/г exceptions to the harmony rule, loaded from `words/latin_exceptions.txt` (same format as on iOS). */
class HarmonyExceptions {
    private val exact = HashMap<String, BooleanArray?>()
    private val stems = ArrayList<Pair<String, BooleanArray?>>()

    companion object {
        fun load(context: Context): HarmonyExceptions {
            val result = HarmonyExceptions()
            try {
                context.assets.open("words/latin_exceptions.txt").bufferedReader().useLines { lines ->
                    for (raw in lines) {
                        var line = raw.trim().lowercase()
                        if (line.isEmpty() || line.startsWith("#")) continue
                        var pattern: BooleanArray? = null
                        val eq = line.indexOf('=')
                        if (eq >= 0) {
                            pattern = line.substring(eq + 1).mapNotNull { ch ->
                                when (ch) { 'q', 'ğ' -> true; 'k', 'g' -> false; else -> null }
                            }.toBooleanArray()
                            line = line.substring(0, eq).trim()
                        }
                        if (line.endsWith("*")) result.stems.add(line.dropLast(1) to pattern) else result.exact[line] = pattern
                    }
                }
            } catch (_: java.io.IOException) {
            }
            result.stems.sortByDescending { it.first.length }  // the most specific stem wins
            return result
        }
    }

    private val back = setOf('а', 'о', 'ы', 'у', 'я', 'ё', 'ю')
    private val front = setOf('е', 'э', 'и', 'ө', 'ү')

    /** For each к/г in [word], in order: whether it is hard (q/ğ). */
    fun hardness(word: String): BooleanArray {
        val count = word.count { it == 'к' || it == 'г' }
        if (exact.containsKey(word)) return fromPattern(exact[word], count)
        stems.firstOrNull { word.startsWith(it.first) }?.let { return fromPattern(it.second, count) }
        // Harmony: a к/г follows the nearest vowel after it, or the one before it at the end of a word.
        val out = BooleanArray(count)
        var n = 0
        for (i in word.indices) {
            if (word[i] != 'к' && word[i] != 'г') continue
            var hard = false
            var decided = false
            for (j in i + 1 until word.length) {
                if (word[j] in back) { hard = true; decided = true; break }
                if (word[j] in front) { decided = true; break }
            }
            if (!decided) for (j in i - 1 downTo 0) {
                if (word[j] in back) { hard = true; break }
                if (word[j] in front) break
            }
            out[n++] = hard
        }
        return out
    }

    private fun fromPattern(pattern: BooleanArray?, count: Int) =
        BooleanArray(count) { if (pattern != null && it < pattern.size) pattern[it] else false }
}
