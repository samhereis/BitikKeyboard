package com.shoktuk.shoktukkeyboard.suggestions

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/** к/г exceptions to the harmony rule, loaded from `words/latin_exceptions.txt` (same format as on iOS). */
class HarmonyExceptions {
    private val exact = HashMap<String, BooleanArray?>()
    private val stems = ArrayList<Pair<String, BooleanArray?>>()

    companion object {
        fun load(source: WordSuggestionEngine.Source): HarmonyExceptions {
            val result = HarmonyExceptions()
            try {
                (source.open("words/latin_exceptions.txt") ?: return result).bufferedReader().useLines { lines ->
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
