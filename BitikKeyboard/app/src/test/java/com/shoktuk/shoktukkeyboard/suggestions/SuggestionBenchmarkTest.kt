package com.shoktuk.shoktukkeyboard.suggestions

import java.io.File
import org.junit.Test

/**
 * Timing of the suggestion engine on the real word files, on the JVM (run: `gradle :app:testDebugUnitTest --tests '*SuggestionBenchmarkTest*' -i`).
 * Numbers are for comparing versions of the engine against each other on the same machine, not absolute phone times.
 */
class SuggestionBenchmarkTest {
    private val assets = File("src/main/assets")
    private val source = WordSuggestionEngine.Source { path -> File(assets, path).takeIf { it.exists() }?.inputStream() }

    private fun time(runs: Int = 30, warm: Int = 5, body: () -> Unit): String {
        repeat(warm) { body() }
        val t = LongArray(runs) { val s = System.nanoTime(); body(); System.nanoTime() - s }
        t.sort()
        fun ms(n: Long) = "%.2f".format(n / 1e6)
        return "median ${ms(t[runs / 2])} ms   p95 ${ms(t[(runs * 95 / 100).coerceAtMost(runs - 1)])} ms   max ${ms(t[runs - 1])} ms"
    }

    @Test
    fun benchmark() {
        val out = StringBuilder("\n=== SUGGESTION BENCHMARK ===\n")
        fun line(label: String, text: String) { out.append("%-44s %s\n".format(label, text)); println("BENCH %-44s %s".format(label, text)) }

        for ((name, cyr) in listOf("Latin" to false, "Cyrillic" to true)) {
            WordSuggestionEngine.configure(source, cyrillic = cyr)
            // Cold: the first lookup of a letter reads, transliterates and sorts the file.
            val first = if (cyr) "к" else "k"
            val cold = System.nanoTime(); WordSuggestionEngine.completions(first + "а".takeIf { cyr }.orEmpty(), 16); val coldMs = (System.nanoTime() - cold) / 1e6
            line("$name: cold load of the biggest letter (к)", "%.1f ms".format(coldMs))
            val prefixes = if (cyr) listOf("к", "ка", "кыр", "кыргыз", "бол", "ал") else listOf("k", "ka", "kyr", "kyrgyz", "bol", "al")
            for (p in prefixes) line("$name: completions \"$p\"", time { WordSuggestionEngine.completions(p, 16) })
            val typos = if (cyr) listOf("кыргзы", "балыкчы", "мектеб") else listOf("kyrgzy", "balıkçı", "mekteb")
            for (p in typos) line("$name: typo \"$p\"", time(runs = 15, warm = 2) { WordSuggestionEngine.completions(p, 16) })
        }

        WordSuggestionEngine.configure(source, cyrillic = true)
        val glyphs = mapOf(
            "2 glyphs (к ы)" to listOf(listOf("к", "г"), listOf("ы", "и")),
            "4 glyphs (к ы р г)" to listOf(listOf("к", "г"), listOf("ы", "и"), listOf("р"), listOf("г", "к")),
            "6 glyphs (б а л ы к ч)" to listOf(listOf("б"), listOf("а"), listOf("л"), listOf("ы", "и"), listOf("к", "г"), listOf("ч")),
            "short (а л)" to listOf(listOf("а"), listOf("л")),
        )
        for ((label, readings) in glyphs) line("Bitik: $label", time(runs = 20, warm = 3) { WordSuggestionEngine.bitikCompletions(readings, 12) })

        // Glide on the Latin keyboard: keys laid out like the phone's (90 px apart), a trace through the letters of a word.
        val rows = listOf("qşertyuiop", "asdügəjklğ", "zŋçıbnm")
        val centres = HashMap<Char, WordSuggestionEngine.GlidePoint>()
        for ((r, row) in rows.withIndex()) for ((c, ch) in row.withIndex()) centres[ch] = WordSuggestionEngine.GlidePoint(60f + c * 90f + r * 30f, 1650f + r * 170f)
        for ((label, word) in listOf("kyrgyz" to "kırğız", "balıkçı" to "balıkçı", "mektep" to "mektep")) {
            val trace = ArrayList<WordSuggestionEngine.GlidePoint>()
            for (i in 0 until word.length - 1) { val a = centres[word[i]] ?: continue; val b = centres[word[i + 1]] ?: continue; for (t in 0..8) trace.add(WordSuggestionEngine.GlidePoint(a.x + (b.x - a.x) * t / 8f, a.y + (b.y - a.y) * t / 8f)) }
            WordSuggestionEngine.configure(source, cyrillic = false)
            val cold = System.nanoTime(); WordSuggestionEngine.glideMatches(trace, centres, 90f, word[0]); val coldMs = (System.nanoTime() - cold) / 1e6
            line("Glide \"$label\" cold (files not loaded)", "%.1f ms".format(coldMs))
            line("Glide \"$label\" warm", time(runs = 10, warm = 2) { WordSuggestionEngine.glideMatches(trace, centres, 90f, word[0]) })
                        WordSuggestionEngine.glideFullScan = true
            val full = WordSuggestionEngine.glideMatches(trace, centres, 90f, word[0])
            WordSuggestionEngine.glideFullScan = false
            val narrowed = WordSuggestionEngine.glideMatches(trace, centres, 90f, word[0])
            line("Glide \"$label\" same words as a full scan", (full == narrowed).toString() + " " + narrowed.take(4))
            org.junit.Assert.assertEquals(full, narrowed)
        }

        println(out)
    }
}
