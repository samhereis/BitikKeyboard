package com.shoktuk.shoktukkeyboard.suggestions

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Locks the engine's answers: the first run writes `src/test/resources/suggestion_golden.txt`, later runs must produce
 * exactly the same words in the same order. Delete the file to accept an intended change.
 */
class SuggestionGoldenTest {
    private val source = WordSuggestionEngine.Source { path -> File("src/main/assets", path).takeIf { it.exists() }?.inputStream() }
    private val golden = File("src/test/resources/suggestion_golden.txt")

    private fun render(): String {
        val sb = StringBuilder()
        for (cyr in listOf(false, true)) {
            WordSuggestionEngine.configure(source, cyrillic = cyr)
            val prefixes = ArrayList<String>()
            if (cyr) {
                for (a in "абвгдеёжзийклмнңоөпрстуүфхцчшыэюя") { prefixes.add("$a"); for (b in "аеиоуыөү") prefixes.add("$a$b") }
                prefixes.addAll(listOf("кыргыз", "балыкч", "кыргзы", "мектеб", "Кыр", "КЫР", "салам", "рахмат"))
            } else {
                for (a in "abdefgiıklmnoprstuyzşçñ") { prefixes.add("$a"); for (b in "aeıiouyü") prefixes.add("$a$b") }
                prefixes.addAll(listOf("kyrgyz", "balıkç", "kyrgzy", "mekteb", "Kyr", "KYR", "salam", "rahmat", "qal", "kal", "ğa", "ga"))
            }
            for (p in prefixes) sb.append(if (cyr) "C" else "L").append('\t').append(p).append('\t')
                .append(WordSuggestionEngine.completions(p, 16).joinToString(",") { it.word + "|" + it.cyr + "|" + it.latin }).append('\n')
        }
        WordSuggestionEngine.configure(source, cyrillic = true)
        val readings = listOf(
            listOf(listOf("к", "г"), listOf("ы", "и")), listOf(listOf("к", "г"), listOf("ы", "и"), listOf("р"), listOf("г", "к")),
            listOf(listOf("б"), listOf("а"), listOf("л"), listOf("ы", "и"), listOf("к", "г"), listOf("ч")), listOf(listOf("а"), listOf("л")),
            listOf(listOf("м"), listOf("о", "у"), listOf("н")), listOf(listOf("й", "ж"), listOf("а"), listOf("й"), listOf("ы", "и")),
        )
        for (r in readings) sb.append("B\t").append(r.joinToString(" ") { it.joinToString("/") }).append('\t')
            .append(WordSuggestionEngine.bitikCompletions(r, 12).joinToString(",") { it.word }).append('\n')
        return sb.toString()
    }

    @Test
    fun answersAreUnchanged() {
        val now = render()
        if (!golden.exists()) { golden.parentFile.mkdirs(); golden.writeText(now); println("GOLDEN written (${now.lines().size} lines)"); return }
        val before = golden.readText().lines(); val after = now.lines()
        for (i in before.indices) assertEquals("line ${i + 1}", before[i], after.getOrNull(i))
        assertEquals(before.size, after.size)
    }
}
