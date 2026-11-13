package com.shoktuk.shoktukkeyboard.suggestions

import java.io.File
import java.util.Random
import org.junit.Assert.assertEquals
import org.junit.Test

/** The trie walk must give exactly what checking every word gives, on glyph sequences made from real words. */
class BitikTrieEquivalenceTest {
    private val source = WordSuggestionEngine.Source { path -> File("src/main/assets", path).takeIf { it.exists() }?.inputStream() }
    private val vowels = "аеиоуыөү"
    private val alt = mapOf('к' to "г", 'г' to "к", 'ы' to "и", 'и' to "ы", 'о' to "у", 'у' to "о", 'й' to "ж", 'ж' to "й", 'ч' to "ш", 'т' to "д")

    @Test
    fun sameAsScanning() {
        WordSuggestionEngine.configure(source, cyrillic = true)
        val rnd = Random(7)
        val words = File("src/main/assets/words").listFiles { f -> f.name.startsWith("kyw_") }!!.sortedBy { it.name }
            .flatMap { f -> f.readLines().take(4000) }.filter { it.length in 2..9 && it.all { c -> c in "абвгдеёжзийклмнңоөпрстуүфхцчшщыэюяъь" } }
        var checked = 0
        repeat(2500) {
            val w = words[rnd.nextInt(words.size)]
            val readings = ArrayList<List<String>>()
            for (c in w) {
                if (c in vowels && rnd.nextInt(3) == 0) continue  // short writing: the vowel is not typed
                if (c == 'ъ' || c == 'ь') continue
                val list = arrayListOf(c.toString()); alt[c]?.let { list.add(it) }
                readings.add(list)
            }
            if (readings.isEmpty()) return@repeat
            val cut = if (rnd.nextInt(3) == 0) maxOf(1, readings.size - 1) else readings.size
            val r = readings.take(cut)
            WordSuggestionEngine.useScan = false; val fast = WordSuggestionEngine.bitikCompletions(r, 12).map { it.word }
            WordSuggestionEngine.useScan = true; val slow = WordSuggestionEngine.bitikCompletions(r, 12).map { it.word }
            WordSuggestionEngine.useScan = false
            assertEquals("glyphs of '$w': ${r.joinToString(" ") { it.joinToString("/") }}", slow, fast)
            checked++
        }
        println("EQUIV checked $checked")
    }
}
