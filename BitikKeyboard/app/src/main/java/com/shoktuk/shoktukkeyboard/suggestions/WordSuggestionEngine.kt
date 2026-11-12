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

    /** Arabic typing matches the transliteration into the Arabic keyboard's letters. */
    private var arabic = false

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
    fun configure(context: Context, cyrillic: Boolean, zh: String = "j", ch: String = "ç", ng: String = "ñ", o: String = "ö", arabic: Boolean = false) {
        if (!configured) {
            appContext = context.applicationContext
            LearnedWords.attach(appContext)
            exceptions = HarmonyExceptions.load(appContext)
            configured = true
        }
        val sig = "$cyrillic|$zh|$ch|$ng|$o|$arabic"
        if (sig == signature) return
        signature = sig
        this.cyrillic = cyrillic
        this.arabic = arabic
        this.zh = zh; this.ch = ch; this.ng = ng; this.o = o
        val map = buildTable()
        table = arrayOfNulls(0xD0)
        for ((c, v) in map) if (c.code in 0x430 until 0x500) table[c.code - 0x430] = v
        extra = map
        cache.clear(); recency.clear(); loading.clear(); learnedSpelled.clear()
        generation++
        latinFirst.clear()
        if (!cyrillic) for (c in ALPHABET) {
            val first = fold(spell(c.toString(), HarmonyExceptions())).firstOrNull() ?: continue
            latinFirst.getOrPut(first) { ArrayList() }.add(c)
        }
    }

    private var generation = 0

    /** Reads the writing system and Latin letter settings from the keyboard service. */
    fun configureFor(service: com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService) {
        val system = com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService.current_writingSystem
        val kiril = system == com.shoktuk.shoktukkeyboard.project.data.WritingSystem.Kiril
        val arab = system == com.shoktuk.shoktukkeyboard.project.data.WritingSystem.Arab
        with(com.shoktuk.shoktukkeyboard.project.data.SettingsManager) {
            if (arab) configure(service, false, "j", "ç", "ñ", "ö", arabic = true)
            else configure(service, kiril, service.latinZH.id, service.latinCH.id, service.latinNG.id, service.latinO.id)
        }
    }

    // ---------------------------------------------------------------- typing

    /** Pairs of keys that sit next to each other, encoded `(a shl 32) or b`, for slips of the finger. */
    fun pair(a: Char, b: Char): Long = (a.code.toLong() shl 32) or b.code.toLong()

    /** Up to [limit] completions of [prefix], shortest first among the most frequent, then near-misses for typos. */
    fun completions(prefix: String, limit: Int = 40, neighbours: Set<Long> = emptySet()): List<Suggestion> {
        val result = rawCompletions(prefix, limit, neighbours)
        if (!configured || prefix.isEmpty()) return result
        return applyLearned(result, prefix, if (cyrillic) prefix.lowercase() else fold(prefix.lowercase()), limit)
    }

    private fun rawCompletions(prefix: String, limit: Int, neighbours: Set<Long>): List<Suggestion> {
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
            result.add(Suggestion(w, c, if (cyrillic) applyCase(prefix, spell(e.cyr, exceptions)) else if (arabic) applyCase(prefix, transliterate(e.cyr, exceptions)) else w))
            return result.size == limit
        }
        for (m in matches) if (add(m)) return result
        if (result.size < 6 && lower.length >= 3) {
            for (e in fuzzy(lower, first, neighbours)) if (add(e)) break
        }
        return result
    }

    // ---------------------------------------------------------------- Bitik

    /**
     * Words (Cyrillic, as in the word list) the typed Bitik glyphs can stand for. `readings[i]` lists every Cyrillic
     * spelling glyph `i` can have, so a glyph for "о" or "у" finds both bolot and bulut. A vowel of the word that no
     * glyph covers is skipped (short writing), never two in a row. Exact fits come first, then longer words.
     * Needs [configure] with `cyrillic = true`. Each result carries the Latin spelling for display in [Suggestion.latin].
     */
    fun bitikCompletions(readings: List<List<String>>, limit: Int = 12): List<Suggestion> {
        if (!configured || !cyrillic || readings.isEmpty()) return emptyList()
        val letters = LinkedHashSet<Char>()
        for (f in readings[0].mapNotNull { it.firstOrNull() }) {
            if (ALPHABET.indexOf(f) >= 0) letters.add(f)
            when (f) {
                'й' -> letters.addAll(listOf('ю', 'я', 'ё'))
                'т' -> letters.add('ц')
                'ш' -> letters.add('щ')
                'е' -> letters.add('э')
            }
        }
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
            out.add(Suggestion(h.entry.word, h.entry.word, transliterate(h.entry.word, exceptions)))
            if (out.size == limit) break
        }
        return applyLearnedBitik(out, readings, limit)
    }

    // ---------------------------------------------------------------- Personalize T9

    private val learnedSpelled = HashMap<String, Pair<String, String>>()  // learned Cyrillic word -> (shown, key) for the current script

    /** The Cyrillic spelling of a word typed on this keyboard, as the word list knows it (null for any word it doesn't know). */
    fun cyrillicSpelling(typed: String): String? {
        if (!configured) return null
        val lower = if (cyrillic) typed.lowercase() else fold(typed.lowercase())
        val first = lower.firstOrNull() ?: return null
        for (letter in lettersFor(first) ?: return null) {
            val entries = (shard(letter, wait = false) ?: continue).entries
            var low = 0
            var high = entries.size
            while (low < high) {
                val mid = (low + high) ushr 1
                if (entries[mid].key < lower) low = mid + 1 else high = mid
            }
            if (low < entries.size && entries[low].key == lower) return entries[low].cyr
        }
        return null
    }

    /** A word typed on the keyboard has been finished: count it as used once more. */
    fun learnTyped(typed: String) {
        if (typed.length < 2) return
        cyrillicSpelling(typed)?.let { LearnedWords.learn(it) }
    }

    /** [result] with the learned words first (most used first): used words it already holds, and learned words (used twice or more) that start with the typed letters. */
    private fun applyLearned(result: List<Suggestion>, prefix: String, lower: String, limit: Int): List<Suggestion> {
        val learned = LearnedWords.snapshot()
        if (learned.isEmpty()) return result
        val boosted = ArrayList<Pair<Suggestion, Int>>()
        val rest = ArrayList<Suggestion>()
        val seen = HashSet<String>()
        for (s in result) {
            seen.add(s.word)
            val n = learned[s.cyr.lowercase()]
            if (n != null) boosted.add(s to n) else rest.add(s)
        }
        for ((cyr, n) in learned) {
            if (n < 2) continue
            val (shown, key) = learnedSpelled.getOrPut(cyr) {
                val shown = if (cyrillic) cyr else spell(cyr, exceptions)
                shown to (if (cyrillic) cyr else fold(shown.lowercase()))
            }
            if (key.length <= lower.length || !key.startsWith(lower)) continue
            val word = applyCase(prefix, shown)
            if (!seen.add(word)) continue
            val latin = if (cyrillic) applyCase(prefix, spell(cyr, exceptions)) else if (arabic) applyCase(prefix, transliterate(cyr, exceptions)) else word
            boosted.add(Suggestion(word, applyCase(prefix, cyr), latin) to n)
        }
        boosted.sortByDescending { it.second }
        return (boosted.map { it.first } + rest).take(limit)
    }

    private fun applyLearnedBitik(hits: List<Suggestion>, readings: List<List<String>>, limit: Int): List<Suggestion> {
        val learned = LearnedWords.snapshot()
        if (learned.isEmpty()) return hits
        val boosted = ArrayList<Pair<Suggestion, Int>>()
        val rest = ArrayList<Suggestion>()
        val seen = HashSet<String>()
        for (h in hits) {
            seen.add(h.word)
            val n = learned[h.word]
            if (n != null) boosted.add(h to n) else rest.add(h)
        }
        for ((cyr, n) in learned) {
            if (n < 2 || cyr in seen || bitikMatch(cyr, readings) <= 0) continue
            boosted.add(Suggestion(cyr, cyr, transliterate(cyr, exceptions)) to n)
        }
        boosted.sortByDescending { it.second }
        return (boosted.map { it.first } + rest).take(limit)
    }

    private fun isVowel(c: Char) = c == 'а' || c == 'е' || c == 'и' || c == 'о' || c == 'у' || c == 'ы' || c == 'ө' || c == 'ү'

    /** How many sounds a Cyrillic letter stands for in Bitik: я = йа, ю = йу, ё = йо, ц = тс; ь and ъ are silent. */
    private fun soundCount(c: Char): Int = when (c) { 'я', 'ю', 'ё', 'ц' -> 2; 'ъ', 'ь' -> 0; else -> 1 }

    private fun sound(c: Char, sub: Int): Char = when (c) {
        'я' -> if (sub == 0) 'й' else 'а'
        'ю' -> if (sub == 0) 'й' else 'у'
        'ё' -> if (sub == 0) 'й' else 'о'
        'ц' -> if (sub == 0) 'т' else 'с'
        'э' -> 'е'
        'щ' -> 'ш'
        else -> c
    }

    /** 2 = the glyphs spell exactly this word, 1 = the word starts with what they spell, 0 = no match. */
    private fun bitikMatch(w: String, g: List<List<String>>): Int {
        val n = w.length
        fun go(gi: Int, wi0: Int, sub0: Int, prevVowel: Boolean): Int {
            var wi = wi0; var sub = sub0
            while (wi < n && sub >= soundCount(w[wi])) { wi++; sub = 0 }
            if (gi == g.size) return if (wi >= n) 2 else 1
            if (wi >= n) return 0
            var best = 0
            for (r in g[gi]) {
                var cw = wi; var cs = sub; var ok = true
                for (ch in r) {
                    while (cw < n && cs >= soundCount(w[cw])) { cw++; cs = 0 }
                    if (cw >= n || sound(w[cw], cs) != ch) { ok = false; break }
                    cs++
                }
                if (ok) {
                    best = max(best, go(gi + 1, cw, cs, r.isNotEmpty() && isVowel(r.last())))
                    if (best == 2) return 2
                }
            }
            if (gi > 0 && !prevVowel && isVowel(sound(w[wi], sub))) best = max(best, go(gi, wi, sub + 1, true))
            return best
        }
        return go(0, 0, 0, false)
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
        if (cyrillic) (if (ALPHABET.indexOf(first) >= 0) listOf(first) else null) else latinFirst[fold(first.toString()).first()]

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
            val word = if (cyrillic) w else spell(w, exceptions)
            list.add(Entry(word, if (cyrillic) word else fold(word), i / total, word.length, w))
        }
        list.sortBy { it.key }
        return Shard(list.toTypedArray())
    }

    private fun fold(s: String): String = s.replace('q', 'k').replace('ğ', 'g').replace('ق', 'ك').replace('ع', 'گ')

    /** The word as the keyboard spells it: Latin letters, or the Arabic keyboard's letters. */
    private fun spell(word: String, exceptions: HarmonyExceptions): String {
        val latin = transliterate(word, exceptions)
        if (!arabic) return latin
        val sb = StringBuilder(latin.length)
        for (c in latin) sb.append(ARABIC_LETTERS[c] ?: c)
        return sb.toString()
    }

    private val ARABIC_LETTERS = mapOf(
        'a' to 'ا', 'b' to 'ب', 'c' to 'چ', 'ç' to 'چ', 'd' to 'د', 'e' to 'ە', 'f' to 'ف', 'g' to 'گ', 'ğ' to 'ع', 'h' to 'ه',
        'ı' to 'ى', 'i' to 'ئ', 'j' to 'ج', 'k' to 'ك', 'l' to 'ل', 'm' to 'م', 'n' to 'ن', 'ñ' to 'ڭ', 'ŋ' to 'ڭ', 'o' to 'و',
        'ö' to 'ۅ', 'p' to 'پ', 'q' to 'ق', 'r' to 'ر', 's' to 'س', 'ş' to 'ش', 't' to 'ت', 'u' to 'ۇ', 'ü' to 'ۉ', 'v' to 'ۋ',
        'y' to 'ي', 'z' to 'ز',
    )

    private fun applyCase(typed: String, word: String): String {
        // Scripts without case (Arabic) read the same in upper case, so they must not count as typed in capitals.
        if (typed.length > 1 && typed == typed.uppercase() && typed != typed.lowercase()) return word.uppercase()
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
