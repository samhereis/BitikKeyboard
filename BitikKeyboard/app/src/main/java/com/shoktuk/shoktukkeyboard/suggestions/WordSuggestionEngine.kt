package com.shoktuk.shoktukkeyboard.suggestions

import android.content.Context
import java.io.File
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Prefix autocomplete, typo tolerance and glide matching for the Latin keyboard.
 *
 * The word list (Kyrgyz Cyrillic, merged from many corpora) is split into one asset file per first letter,
 * `words/kyw_<hex of the letter>.txt`, most frequent word first. Only the file(s) for the letter being typed are read,
 * transliterated to Kyrgyz Latin and kept in memory (a small cache holds the rest). Everything here runs on the
 * [SuggestionWorker] thread, never on the main thread, so a file simply loads when a lookup needs it. Matching ignores hard/soft к/г: the key folds
 * q→k and ğ→g, so "kal" finds "qalğan".
 */
object WordSuggestionEngine {
    /** A completion in the keyboard's own script ([word]) with its Cyrillic and Latin spellings. */
    class Suggestion(val word: String, val cyr: String, val latin: String)

    /** One word. [id] (file + position) makes two reads of the same word equal, since a file on disk hands out a fresh object each time. */
    private class Entry(val word: String, val key: String, val pct: Float, val len: Int, val cyr: String, val id: Long = 0) {
        override fun equals(other: Any?) = this === other || (id != 0L && other is Entry && other.id == id)
        override fun hashCode() = if (id != 0L) id.hashCode() else System.identityHashCode(this)
    }

    /**
     * The words of one file, sorted by key. Either on the heap (a file just built) or read straight from a memory-mapped file
     * on disk: opening one costs next to nothing, a lookup touches only the few pages it needs, and nothing sits on the heap,
     * so the dictionary can grow far past what memory holds.
     *
     * File layout (big-endian): magic, n, n frequency floats, n+1 offsets into the text, then per entry "word\0key\0cyr".
     */
    private class Entries(private val array: Array<Entry>?, private val map: java.nio.MappedByteBuffer?, val size: Int, private val letter: Int) : Iterable<Entry> {
        constructor(array: Array<Entry>) : this(array, null, array.size, 0)

        private val reader: java.nio.ByteBuffer? = map?.duplicate()
        private val offsetsAt = 8 + 4 * size
        private val textAt = offsetsAt + 4 * (size + 1)

        val indices: IntRange get() = 0 until size
        fun isEmpty() = size == 0
        fun isNotEmpty() = size != 0
        /** What this holds on the heap, in entries (a mapped file holds none). */
        val heapCost: Int get() = if (array != null) size else 0

        fun pct(i: Int): Float = if (array != null) array[i].pct else map!!.getFloat(8 + 4 * i)

        operator fun get(i: Int): Entry {
            array?.let { return it[i] }
            val m = map!!
            val from = m.getInt(offsetsAt + 4 * i); val to = m.getInt(offsetsAt + 4 * (i + 1))
            val bytes = ByteArray(to - from)
            reader!!.position(textAt + from); reader.get(bytes)
            val text = String(bytes, Charsets.UTF_8)
            val a = text.indexOf('\u0000'); val b = text.indexOf('\u0000', a + 1)
            val word = text.substring(0, a)
            return Entry(word, text.substring(a + 1, b), pct(i), word.length, text.substring(b + 1), (letter.toLong() shl 32) or (i + 1).toLong())
        }

        override fun iterator() = object : Iterator<Entry> {
            var i = 0
            override fun hasNext() = i < size
            override fun next() = get(i++)
        }

        companion object {
            private const val MAGIC = 0x42545631

            fun write(file: File, entries: Array<Entry>) {
                val texts = entries.map { (it.word + "\u0000" + it.key + "\u0000" + it.cyr).toByteArray(Charsets.UTF_8) }
                val n = entries.size
                val total = 8 + 4 * n + 4 * (n + 1) + texts.sumOf { it.size }
                val out = java.nio.ByteBuffer.allocate(total)
                out.putInt(MAGIC).putInt(n)
                for (e in entries) out.putFloat(e.pct)
                var at = 0
                for (t in texts) { out.putInt(at); at += t.size }
                out.putInt(at)
                for (t in texts) out.put(t)
                out.flip()
                file.parentFile?.mkdirs()
                val tmp = File(file.path + ".tmp")
                java.io.FileOutputStream(tmp).use { it.channel.write(out) }
                tmp.renameTo(file)
            }

            fun open(file: File, letter: Char): Entries? = try {
                if (!file.exists()) null else java.io.RandomAccessFile(file, "r").use { raf ->
                    val map = raf.channel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, raf.length())
                    if (raf.length() < 8 || map.getInt(0) != MAGIC) null else Entries(null, map, map.getInt(4), letter.code)
                }
            } catch (e: Exception) { null }
        }
    }
    /**
     * One loaded word file, sorted by key. [best] answers "which entry of this range is the most frequent" in constant
     * time (a sparse table over blocks of [BLOCK] entries), so the most frequent words of any prefix are found without
     * looking at every word that starts with it, however long the run is.
     */
    private class Shard(val entries: Entries) {
        private val n = entries.size
        private val levels: Array<IntArray>

        init {
            val blocks = (n + BLOCK - 1) / BLOCK
            val first = IntArray(blocks) { b -> scanBest(b * BLOCK, minOf(n, (b + 1) * BLOCK) - 1) }
            val list = ArrayList<IntArray>()
            list.add(first)
            var span = 1
            while (span * 2 <= blocks) {
                val prev = list.last()
                list.add(IntArray(blocks - span * 2 + 1) { i -> if (entries.pct(prev[i]) <= entries.pct(prev[i + span])) prev[i] else prev[i + span] })
                span *= 2
            }
            levels = list.toTypedArray()
        }

        private fun scanBest(from: Int, to: Int): Int {
            var best = from
            for (i in from + 1..to) if (entries.pct(i) < entries.pct(best)) best = i
            return best
        }

        /** The run of entries whose key starts with [c]. */
        fun firstCharRange(c: Char): IntRange {
            fun bound(prefix: String): Int { var lo = 0; var hi = n; while (lo < hi) { val mid = (lo + hi) ushr 1; if (entries[mid].key < prefix) lo = mid + 1 else hi = mid }; return lo }
            val from = bound(c.toString())
            return from until bound((c + 1).toString())
        }

        /** Index of the most frequent entry in `from..to` (inclusive). */
        fun best(from: Int, to: Int): Int {
            val bf = from / BLOCK; val bt = to / BLOCK
            if (bt - bf < 2) return scanBest(from, to)
            var best = scanBest(from, (bf + 1) * BLOCK - 1)
            val tail = scanBest(bt * BLOCK, to)
            if (entries.pct(tail) < entries.pct(best)) best = tail
            val lo = bf + 1; val hi = bt - 1
            val level = 31 - Integer.numberOfLeadingZeros(hi - lo + 1)
            val a = levels[level][lo]; val b = levels[level][hi - (1 shl level) + 1]
            val mid = if (entries.pct(a) <= entries.pct(b)) a else b
            return if (entries.pct(mid) < entries.pct(best)) mid else best
        }

        /** The [k] most frequent entries of `from until to`, most frequent first. Costs about k lookups, whatever the run length. */
        fun top(from: Int, to: Int, k: Int, below: Float = Float.MAX_VALUE, accept: (Entry) -> Boolean = { true }): List<Entry> {
            val out = ArrayList<Entry>(k)
            if (from >= to) return out
            val heap = java.util.PriorityQueue<IntArray>(compareBy { entries.pct(it[2]) })
            heap.add(intArrayOf(from, to, best(from, to - 1)))
            while (out.size < k) {
                val seg = heap.poll() ?: break
                if (entries.pct(seg[2]) >= below) break  // everything left is rarer still
                if (accept(entries[seg[2]])) out.add(entries[seg[2]])
                if (seg[2] > seg[0]) heap.add(intArrayOf(seg[0], seg[2], best(seg[0], seg[2] - 1)))
                if (seg[2] + 1 < seg[1]) heap.add(intArrayOf(seg[2] + 1, seg[1], best(seg[2] + 1, seg[1] - 1)))
            }
            return out
        }
    }

    private const val BLOCK = 32
    private const val ALPHABET = "абвгдеёжзийклмнңоөпрстуүфхцчшщыэюя"
    private const val MAX_CACHED_ENTRIES = 250_000

    /** A point on screen (the engine has no Android dependency, so it can run in plain JVM tests). */
    class GlidePoint(val x: Float, val y: Float)

    /** Where the word files come from: the app's assets on a device, a folder in tests and benchmarks. */
    fun interface Source { fun open(path: String): java.io.InputStream? }

    private lateinit var source: Source
    private var configured = false
    private val cache = HashMap<Char, Shard>()
    private val recency = ArrayList<Char>()
    private var inUse = HashSet<Char>()
    private var exceptions = HarmonyExceptions()
    private val latinFirst = HashMap<Char, MutableList<Char>>()  // typed Latin first letter -> Cyrillic first letters

    /** Cyrillic typing matches the word files as they are; Latin typing matches their transliteration. */
    private var cyrillic = false

    /** Arabic typing matches the transliteration into the Arabic keyboard's letters. */
    private var arabic = false

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
            LearnedWords.attach(context.applicationContext)
            diskCache = File(context.applicationContext.cacheDir, "shards")
            cacheTag = try {
                val info = context.applicationContext.packageManager.getPackageInfo(context.packageName, 0)
                "${info.longVersionCode}_${info.lastUpdateTime}"
            } catch (e: Exception) { "0" }
            // Files of an older version (or the old text format) are of no use any more.
            diskCache?.listFiles()?.forEach { if (!it.name.startsWith("${cacheTag}_") || !it.name.endsWith(".bin")) it.delete() }
            val assets = context.applicationContext.assets
            configure(Source { path -> try { assets.open(path) } catch (e: java.io.IOException) { null } }, cyrillic, zh, ch, ng, o, arabic)
            return
        }
        configure(source, cyrillic, zh, ch, ng, o, arabic)
    }

    fun configure(source: Source, cyrillic: Boolean, zh: String = "j", ch: String = "ç", ng: String = "ñ", o: String = "ö", arabic: Boolean = false) {
        if (!configured) {
            this.source = source
            exceptions = HarmonyExceptions.load(source)
            configured = true
        }
        val sig = "$cyrillic|$zh|$ch|$ng|$o|$arabic"
        if (sig == signature) return
        com.shoktuk.shoktukkeyboard.bench.TypingBench.note("engine settings changed: $signature -> $sig (cache of ${cache.size} files dropped)")
        signature = sig
        this.cyrillic = cyrillic
        this.arabic = arabic
        this.zh = zh; this.ch = ch; this.ng = ng; this.o = o
        val map = buildTable()
        table = arrayOfNulls(0xD0)
        for ((c, v) in map) if (c.code in 0x430 until 0x500) table[c.code - 0x430] = v
        extra = map
        cache.clear(); recency.clear(); learnedSpelled.clear()
        latinFirst.clear()
        if (!cyrillic) for (c in ALPHABET) {
            val first = fold(spell(c.toString(), HarmonyExceptions())).firstOrNull() ?: continue
            latinFirst.getOrPut(first) { ArrayList() }.add(c)
        }
    }

    /**
     * Reads the writing system and Latin letter settings from the keyboard service (call on the main thread) and returns
     * the call that applies them; run that on the [SuggestionWorker] before looking anything up.
     */
    fun configurator(service: com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService): () -> Unit {
        val system = com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService.current_writingSystem
        // Bitik suggestions match the Cyrillic words, so they use the Cyrillic files.
        val kiril = system == com.shoktuk.shoktukkeyboard.project.data.WritingSystem.Kiril || system == com.shoktuk.shoktukkeyboard.project.data.WritingSystem.Bitik
        val arab = system == com.shoktuk.shoktukkeyboard.project.data.WritingSystem.Arab
        with(com.shoktuk.shoktukkeyboard.project.data.SettingsManager) {
            val zh = service.latinZH.id; val ch = service.latinCH.id; val ng = service.latinNG.id; val o = service.latinO.id
            return if (arab) { { configure(service, false, "j", "ç", "ñ", "ö", arabic = true) } }
            else { { configure(service, kiril, zh, ch, ng, o) } }
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
            val shard = shard(letter)
            val entries = shard.entries
            var low = 0
            var high = entries.size
            while (low < high) {
                val mid = (low + high) ushr 1
                if (entries[mid].key < lower) low = mid + 1 else high = mid
            }
            // The run of entries whose key starts with the typed text; the ones equal to it are not completions.
            var start = low
            while (start < entries.size && entries[start].key == lower) start++
            var end = start
            run {
                var lo2 = start; var hi2 = entries.size
                while (lo2 < hi2) { val mid = (lo2 + hi2) ushr 1; if (entries[mid].key.startsWith(lower)) lo2 = mid + 1 else hi2 = mid }
                end = lo2
            }
            // The 120 most frequent of the run (so rare one-off strings never get in).
            matches.addAll(shard.top(start, end, 120))
        }
        matches.sortBy { it.pct }
        if (matches.size > 120) matches.subList(120, matches.size).clear()
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
    /** Tests only: answer by checking every word one by one (the slow reference) instead of walking the trie. */
    internal var glideFullScan = false
    internal var useScan = false

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
            val shard = shard(letter)
            val found = if (useScan) HashMap<Entry, Boolean>().also { m -> for (entry in shard.entries) { val r = bitikMatch(entry.key, readings); if (r > 0) m[entry] = r == 2 } } else bitikHits(shard, readings)
            for ((entry, exact) in found) hits.add(Hit(entry, exact))
            if (SuggestionWorker.isStale()) return emptyList()
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

    /**
     * Words a swipe over Bitik keys can mean. `readings[i]` lists every Cyrillic spelling of the i-th key the finger passed.
     * A word fits when its sounds can be read off those keys in order: the first and last key are the word's first and last
     * glyph, keys crossed on the way may be left out, and a vowel no key covers may be skipped (short writing). The fewer
     * crossed keys a word leaves out, the better; frequency breaks ties. Needs [configure] with `cyrillic = true`.
     */
    fun bitikGlideMatches(readings: List<List<String>>, limit: Int = 8): List<Suggestion> {
        if (!configured || !cyrillic) return emptyList()
        val n = minOf(readings.size, 40)
        if (n < 2 || readings.take(n).any { it.isEmpty() }) return emptyList()
        val keys = readings.take(n)
        val firstSounds = HashSet<Char>(); val lastSounds = HashSet<Char>()
        val letters = LinkedHashSet<Char>()
        for (r in keys[0]) {
            val f = r.firstOrNull() ?: continue
            firstSounds.add(f)
            if (ALPHABET.indexOf(f) >= 0) letters.add(f)
            when (f) { 'й' -> letters.addAll(listOf('ю', 'я', 'ё')); 'т' -> letters.add('ц'); 'ш' -> letters.add('щ'); 'е' -> letters.add('э') }
        }
        for (r in keys[n - 1]) r.lastOrNull()?.let { lastSounds.add(it) }
        inUse = HashSet(letters)

        class Hit(val entry: Entry, val cost: Double)
        val hits = ArrayList<Hit>()
        val none = Int.MAX_VALUE
        val sounds = CharArray(64)
        val cost = IntArray(41 * 64)
        for (letter in letters) {
            val shard = shard(letter)
            val limitPct = 0.6f
            for ((k, entry) in shard.entries.withIndex()) {
                if (k and 1023 == 0 && SuggestionWorker.isStale()) return emptyList()
                if (entry.pct >= limitPct) continue
                val w = entry.word
                if (w.length < 2 || w.length > 30) continue
                var m = 0
                for (c in w) { for (sub in 0 until soundCount(c)) { sounds[m++] = sound(c, sub) } }
                if (m < 2 || sounds[0] !in firstSounds) continue
                val endsOnKey = sounds[m - 1] in lastSounds
                val endsOnVowel = isVowel(sounds[m - 1]) && sounds[m - 2] in lastSounds
                if (!endsOnKey && !endsOnVowel) continue

                for (i in 0 until n * (m + 1)) cost[i] = none
                fun fits(r: String, at: Int): Boolean {
                    if (at + r.length > m) return false
                    for (t in r.indices) if (sounds[at + t] != r[t]) return false
                    return true
                }
                for (r in keys[0]) if (fits(r, 0)) cost[r.length] = 0
                for (pi in 0 until n - 1) {
                    for (wi in 1 until m) {
                        val c = cost[pi * (m + 1) + wi]
                        if (c == none) continue
                        for (pj in pi + 1 until n) {
                            val unused = pj - pi - 1
                            for (r in keys[pj]) for (skip in 0..1) {
                                if (skip == 1 && !(wi < m && isVowel(sounds[wi]))) continue
                                val w0 = wi + skip
                                if (!fits(r, w0)) continue
                                val nc = c + unused + skip * 3
                                val slot = pj * (m + 1) + w0 + r.length
                                if (nc < cost[slot]) cost[slot] = nc
                            }
                        }
                    }
                }
                var best = none
                val end = (n - 1) * (m + 1)
                if (cost[end + m] != none) best = cost[end + m]
                if (cost[end + m - 1] != none && isVowel(sounds[m - 1])) best = minOf(best, cost[end + m - 1] + 3)
                if (best == none) continue
                hits.add(Hit(entry, best * 0.25 + entry.pct * 8.0))
            }
        }
        hits.sortBy { it.cost }
        val seen = HashSet<String>()
        val out = ArrayList<Suggestion>()
        for (h in hits) {
            if (!seen.add(h.entry.word)) continue
            out.add(Suggestion(h.entry.word, h.entry.word, transliterate(h.entry.word, exceptions)))
            if (out.size == limit) break
        }
        return out
    }

    // ---------------------------------------------------------------- Personalize T9

    private val learnedSpelled = HashMap<String, Pair<String, String>>()  // learned Cyrillic word -> (shown, key) for the current script

    /** The Cyrillic spelling of a word typed on this keyboard, as the word list knows it (null for any word it doesn't know). */
    fun cyrillicSpelling(typed: String): String? {
        if (!configured) return null
        val lower = if (cyrillic) typed.lowercase() else fold(typed.lowercase())
        val first = lower.firstOrNull() ?: return null
        for (letter in lettersFor(first) ?: return null) {
            val entries = shard(letter).entries
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

    /**
     * The words of [shard] the glyphs can stand for (true = they spell exactly this word, false = the word starts with what
     * they spell), found by walking the sorted words as a trie with the set of ways the glyphs can have matched so far as
     * state. It only visits prefixes that can still match, so it costs the same however many words the file holds. The
     * outcome is the same as [bitikMatch] on every word. At most the 120 most frequent hits under any one prefix are kept.
     */
    private fun bitikHits(shard: Shard, g: List<List<String>>): Map<Entry, Boolean> {
        val entries = shard.entries
        val out = LinkedHashMap<Entry, Boolean>()
        if (entries.isEmpty()) return out
        val k = g.size
        // A state is "between glyphs" (gi glyphs done, whether the last reading ended in a vowel) or "inside reading ri of glyph gi, off sounds matched".
        fun between(gi: Int, pv: Boolean) = (gi shl 12) or (if (pv) 1 else 0)
        fun inside(gi: Int, ri: Int, off: Int) = (gi shl 12) or 2 or (ri shl 2) or (off shl 8)
        // Steps every state over one sound; sets completeAt when all glyphs are matched by it.
        fun step(states: IntArray, count: Int, snd: Char, next: IntArray, completed: BooleanArray): Int {
            var n = 0
            fun add(st: Int) { for (q in 0 until n) if (next[q] == st) return; next[n++] = st }
            for (q in 0 until count) {
                val st = states[q]
                val gi = st ushr 12
                if (st and 2 == 0) {  // between glyphs
                    val pv = st and 1 == 1
                    for ((ri, r) in g[gi].withIndex()) {
                        if (r[0] != snd) continue
                        if (r.length == 1) { if (gi + 1 == k) completed[0] = true else add(between(gi + 1, isVowel(r.last()))) } else add(inside(gi, ri, 1))
                    }
                    if (gi > 0 && !pv && isVowel(snd)) add(between(gi, true))
                } else {
                    val ri = (st shr 2) and 63; val off = (st shr 8) and 15
                    val r = g[gi][ri]
                    if (r[off] != snd) continue
                    if (off + 1 == r.length) { if (gi + 1 == k) completed[0] = true else add(between(gi + 1, isVowel(r.last()))) } else add(inside(gi, ri, off + 1))
                }
            }
            return n
        }
        fun record(from: Int, to: Int, exact: Boolean) {
            for (e in shard.top(from, to, 120)) { if (exact) out[e] = true else if (out[e] == null) out[e] = false }
        }
        val depthStates = Array(42) { IntArray(256) }
        val scratch = Array(2) { IntArray(256) }
        val flag = BooleanArray(1)
        // exactCarry: all glyphs were matched exactly at the last sound (kept through silent letters ь and ъ).
        fun walk(from: Int, to: Int, depth: Int, count: Int) {
            if (SuggestionWorker.isStale()) return
            var i = from
            while (i < to && entries[i].key.length == depth) i++
            while (i < to) {
                val c = entries[i].key[depth]
                var lo = i + 1; var hi = to
                while (lo < hi) { val mid = (lo + hi) ushr 1; if (entries[mid].key[depth] == c) lo = mid + 1 else hi = mid }
                val end = lo
                val sc = soundCount(c)
                // Step the states over this letter's sounds.
                var cur = depthStates[depth]
                var curN = count
                var doneMid = false; var doneLast = false
                var a = 0
                val buf = arrayOf(IntArray(256), IntArray(256))
                var src = cur
                for (sIdx in 0 until sc) {
                    flag[0] = false
                    val dst = buf[sIdx and 1]
                    curN = step(src, curN, sound(c, sIdx), dst, flag)
                    if (flag[0]) { if (sIdx == sc - 1) doneLast = true else doneMid = true }
                    src = dst
                    a++
                }
                if (doneMid) record(i, end, false)  // all glyphs matched part-way through this letter: every word here is longer than that
                if (doneLast) {
                    var own = i
                    while (own < end && entries[own].key.length == depth + 1) own++
                    if (own > i) record(i, own, true)
                    if (own < end) record(own, end, false)
                    // Only a silent letter after it can still make a word exact.
                    var j = own
                    while (j < end) {
                        val c2 = entries[j].key[depth + 1]
                        var l2 = j + 1; var h2 = end
                        while (l2 < h2) { val mid = (l2 + h2) ushr 1; if (entries[mid].key[depth + 1] == c2) l2 = mid + 1 else h2 = mid }
                        if (soundCount(c2) == 0) {
                            var o2 = j
                            while (o2 < l2 && entries[o2].key.length == depth + 2) o2++
                            if (o2 > j) record(j, o2, true)
                            // deeper silent letters: rare; the prefix hits above already list them
                        }
                        j = l2
                    }
                }
                // Other ways the glyphs may still match (a skipped vowel, another reading) can make a longer word exact.
                if (curN > 0) {
                    val nextStates = depthStates[depth + 1]
                    System.arraycopy(src, 0, nextStates, 0, curN)
                    if (depth + 1 < 41) walk(i, end, depth + 1, curN)
                }
                i = end
            }
        }
        depthStates[0][0] = between(0, false)
        walk(0, entries.size, 0, 1)
        return out
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
        class Hit(val entry: Entry, var cost: Int)
        val found = LinkedHashMap<Entry, Hit>()
        for (letter in letters) {
            val shard = shard(letter)
            val entries = shard.entries
            if (entries.isEmpty()) continue
            // Walk the words as a trie (sorted keys: a node is a run of entries sharing a prefix) with the edit-distance row
            // as state, and stop going down as soon as no word below can be within reach. This only ever visits the
            // prefixes near the typed text, so it costs the same however many words the file holds.
            val rows = Array(41) { IntArray(m + 1) }
            for (j in 0..m) rows[0][j] = j * 2
            fun accept(from: Int, to: Int, cost: Int) {
                for (e in shard.top(from, to, 12, below = 0.4f) { it.len <= 39 && it.key != typed }) {
                    val h = found[e]
                    if (h == null) found[e] = Hit(e, cost) else if (cost < h.cost) h.cost = cost
                }
            }
            fun walk(from: Int, to: Int, depth: Int) {
                if (SuggestionWorker.isStale()) return
                var i = from
                while (i < to && entries[i].key.length == depth) i++  // words that end exactly here have no children
                val prev = rows[depth]
                while (i < to) {
                    val c = entries[i].key[depth]
                    var lo = i + 1; var hi = to
                    while (lo < hi) { val mid = (lo + hi) ushr 1; if (entries[mid].key[depth] == c) lo = mid + 1 else hi = mid }
                    val end = lo
                    val row = rows[depth + 1]
                    row[0] = (depth + 1) * 2
                    var rowMin = row[0]
                    val wc = c.code
                    for (j in 0 until m) {
                        val tc = t[j]
                        val sub = if (wc == tc) 0 else if (isNeighbour(wc, tc)) 1 else 2
                        val v = min(min(prev[j + 1] + 2, row[j] + 2), prev[j] + sub)
                        row[j + 1] = v
                        if (v < rowMin) rowMin = v
                    }
                    if (depth + 1 >= m - 1 && row[m] <= maxCost) accept(i, end, row[m])
                    if (rowMin <= maxCost && depth + 1 < 40) walk(i, end, depth + 1)
                    i = end
                }
            }
            walk(0, entries.size, 0)
        }
        val ordered = found.values.toMutableList()
        ordered.sortWith(compareBy<Hit>({ it.cost }, { it.entry.pct }))
        return ordered.take(12).map { it.entry }
    }

    // Adjacent-key pairs as a bit table (index = a * 0x700 + b), rebuilt only when the set changes. Avoids boxing in the hot loops.
    private val neighbourBits = LongArray(0x700 * 0x700 / 64 + 1)
    private var neighbourSource: Set<Long>? = null

    private fun updateNeighbourBits(pairs: Set<Long>) {
        if (pairs === neighbourSource) return
        neighbourSource = pairs
        neighbourBits.fill(0L)
        for (p in pairs) {
            val a = (p ushr 32).toInt(); val b = (p and 0xFFFFFFFFL).toInt()
            if (a < 0x700 && b < 0x700) { val bit = a * 0x700 + b; neighbourBits[bit ushr 6] = neighbourBits[bit ushr 6] or (1L shl (bit and 63)) }
        }
    }

    private fun isNeighbour(a: Int, b: Int): Boolean {
        if (a >= 0x700 || b >= 0x700) return false
        val bit = a * 0x700 + b
        return neighbourBits[bit ushr 6] and (1L shl (bit and 63)) != 0L
    }

    // ----------------------------------------------------------------- glide

    /**
     * Words a glide can mean, best first. Each candidate's ideal path through its key centres is compared with the
     * finger [trace]: it must start where the finger went down, end where it lifted, have a similar length, and follow
     * the same shape. Frequency breaks ties. [centres] maps each typable letter (and held letter) to its key centre.
     */
    /**
     * The word files a glide that went down at [begin] can need. A candidate word must start with a letter whose key is
     * within about one key width of that point (the matcher rejects every other word), so only those letters' files matter.
     */
    private fun glideLetters(centres: Map<Char, GlidePoint>, keyWidth: Float, startLetter: Char, begin: GlidePoint): LinkedHashSet<Char> {
        val starts = LinkedHashSet<Char>().apply { add(startLetter) }
        for ((l, p) in centres) if (hypot(p.x - begin.x, p.y - begin.y) < keyWidth * 1.1f) starts.add(l)
        val letters = LinkedHashSet<Char>()
        for (s in starts) lettersFor(s)?.let { letters.addAll(it) }
        return letters
    }

    /**
     * Loads (and keeps) the word files a glide that went down at [begin] will need. Called when the finger goes down, so
     * the files are ready by the time it lifts instead of being read, spelled and sorted after the release.
     */
    fun prefetchGlide(centres: Map<Char, GlidePoint>, keyWidth: Float, startLetter: Char, begin: GlidePoint) {
        if (!configured || keyWidth <= 0f) return
        val letters = glideLetters(centres, keyWidth, startLetter, begin)
        inUse = HashSet(letters)
        for (l in letters) shard(l)
    }

    fun glideMatches(trace: List<GlidePoint>, centres: Map<Char, GlidePoint>, keyWidth: Float, startLetter: Char, limit: Int = 8): List<String> {
        if (!configured || trace.size < 2 || keyWidth <= 0f) return emptyList()
        val begin = trace.first(); val end = trace.last()
        // The word's first letter is the key the swipe started on, or one beside it.
        val letters = glideLetters(centres, keyWidth, startLetter, begin)
        inUse = HashSet(letters)

        val kw = keyWidth.toDouble()
        val has = BooleanArray(0x700); val cx = DoubleArray(0x700); val cy = DoubleArray(0x700)
        for ((l, p) in centres) if (l.code < 0x700) { has[l.code] = true; cx[l.code] = p.x.toDouble(); cy[l.code] = p.y.toDouble() }
        val traceXY = DoubleArray(trace.size * 2).also { for (i in trace.indices) { it[2 * i] = trace[i].x.toDouble(); it[2 * i + 1] = trace[i].y.toDouble() } }
        val sample = resample(traceXY, 32)
        val traceLength = length(traceXY)
        class Hit(val word: String, val cost: Double)
        val hits = ArrayList<Hit>()
        var pts = DoubleArray(80)
        // The word's first letter must be near where the finger went down: only entries whose key starts with one of those
        // letters (the files are sorted by key) are looked at, not the whole file.
        val firstKeys = LinkedHashSet<Char>()
        for ((l, p) in centres) if (hypot(p.x - begin.x, p.y - begin.y) < kw * 1.1) firstKeys.add(fold(l.toString()).first())
        for (letter in letters) {
            val shard = shard(letter)
            val ranges = if (glideFullScan) listOf(shard.entries.indices) else firstKeys.map { shard.firstCharRange(it) }
            for (range in ranges) for (n0 in range) {
                val entry = shard.entries[n0]
                if (n0 and 1023 == 0 && SuggestionWorker.isStale()) return emptyList()
                if (entry.pct >= 0.5f) continue
                val w = entry.word
                val f = w[0].code; val l = w[w.length - 1].code
                if (f >= 0x700 || l >= 0x700 || !has[f] || !has[l]) continue
                if (hypot(cx[f] - begin.x, cy[f] - begin.y) >= kw * 1.1 || hypot(cx[l] - end.x, cy[l] - end.y) >= kw * 1.3) continue
                if (pts.size < w.length * 2) pts = DoubleArray(w.length * 2)
                var count = 0
                var ok = true
                var prev = -1
                for (c in w) {
                    val u = c.code
                    if (u >= 0x700 || !has[u]) { ok = false; break }
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

    private fun shard(letter: Char): Shard {
        cache[letter]?.let { recency.remove(letter); recency.add(letter); return it }
        val s = loadEntries(letter)
        store(s, letter)
        return s
    }

    private fun store(shard: Shard, letter: Char) {
        cache[letter] = shard
        recency.remove(letter); recency.add(letter)
        var total = cache.values.sumOf { it.entries.heapCost }
        while (total > MAX_CACHED_ENTRIES) {
            val oldest = recency.firstOrNull { it != letter && !inUse.contains(it) } ?: break
            recency.remove(oldest)
            cache.remove(oldest)?.let { total -= it.entries.heapCost }
        }
    }

    /** Where the finished (spelled, sorted) word files are kept between loads; null in tests. Cleared by the system when space runs low. */
    private var diskCache: File? = null

    /** For tests: keeps the finished word files in [dir] (null = none, everything built in memory). */
    internal fun useDiskCache(dir: File?, tag: String = "test") { diskCache = dir; cacheTag = tag; cache.clear(); recency.clear() }
    private var cacheTag = "0"

    private fun cacheFile(letter: Char): File? =
        diskCache?.let { File(it, "%s_%08x_%04x.bin".format(cacheTag, signature.hashCode(), letter.code)) }

    private fun loadEntries(letter: Char): Shard {
        val file = cacheFile(letter)
        if (file != null) Entries.open(file, letter)?.let { return Shard(it) }
        val built = buildEntries(letter)
        if (file != null && built.entries.isNotEmpty()) {
            Entries.write(file, Array(built.entries.size) { built.entries[it] })
            Entries.open(file, letter)?.let { return Shard(it) }
        }
        return built
    }

    /**
     * Builds the next word file that is not on disk yet (spelled, sorted, saved), so typing never has to. True when it built
     * one (call again later), false when every file is there. Run it on the [SuggestionWorker], one file at a time.
     */
    fun warmNext(): Boolean {
        if (!configured || diskCache == null) return false
        for (c in ALPHABET) {
            val file = cacheFile(c) ?: return false
            if (file.exists()) continue
            val built = buildEntries(c)
            if (built.entries.isNotEmpty()) Entries.write(file, Array(built.entries.size) { built.entries[it] })
            return true
        }
        return false
    }

    private fun buildEntries(letter: Char): Shard {
        val name = "words/kyw_%04x.txt".format(letter.code)
        val words = try {
            com.shoktuk.shoktukkeyboard.bench.TypingBench.measure("shard read") {
                (source.open(name) ?: return Shard(Entries(emptyArray()))).bufferedReader().useLines { seq -> seq.map { it.trim() }.filter { it.isNotEmpty() }.toList() }
            }
        } catch (e: java.io.IOException) {
            return Shard(Entries(emptyArray()))
        }
        val total = max(words.size, 1).toFloat()
        val list = ArrayList<Entry>(words.size)
        com.shoktuk.shoktukkeyboard.bench.TypingBench.measure("shard spell (${words.size})") {
            for ((i, w) in words.withIndex()) {
                val word = if (cyrillic) w else spell(w, exceptions)
                list.add(Entry(word, if (cyrillic) word else fold(word), i / total, word.length, w, (letter.code.toLong() shl 32) or (i + 1).toLong()))
            }
        }
        com.shoktuk.shoktukkeyboard.bench.TypingBench.measure("shard sort") { list.sortBy { it.key } }
        return Shard(Entries(list.toTypedArray()))
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
