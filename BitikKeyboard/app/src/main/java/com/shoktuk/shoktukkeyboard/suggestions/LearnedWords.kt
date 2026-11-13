package com.shoktuk.shoktukkeyboard.suggestions

import android.content.Context
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.personalizeT9
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * The words the user writes and how many times, for "Personalize T9". Every word is kept in Cyrillic whatever script it was
 * typed in, so one list serves every keyboard. It lives on the device only (a small file in the app's private storage).
 */
object LearnedWords {
    private const val FILE = "learned_words.tsv"
    private var appContext: Context? = null
    private var counts: ConcurrentHashMap<String, Int>? = null
    @Volatile private var saveScheduled = false
    private val saver = Executors.newSingleThreadScheduledExecutor()

    /** Bumped on every change, for caches built from the list. */
    var version = 0
        private set

    fun attach(context: Context) {
        if (appContext == null) appContext = context.applicationContext
    }

    private fun isEnabled(): Boolean = appContext?.personalizeT9 ?: false

    @Synchronized
    private fun load(): ConcurrentHashMap<String, Int> {
        counts?.let { return it }
        val map = ConcurrentHashMap<String, Int>()
        appContext?.let { ctx ->
            try {
                val file = File(ctx.filesDir, FILE)
                if (file.exists()) file.forEachLine { line ->
                    val tab = line.indexOf('\t')
                    if (tab > 0 && normalized(line.substring(0, tab)) != null) line.substring(tab + 1).toIntOrNull()?.let { map[line.substring(0, tab)] = it }
                }
            } catch (_: Exception) {
            }
        }
        counts = map
        return map
    }

    /**
     * A word as it is stored: lower case Cyrillic letters only, at least two. Anything with a digit, symbol, hyphen or
     * apostrophe (passwords, e-mails, codes, compound or abbreviated forms) is never stored.
     */
    fun normalized(word: String): String? {
        val w = word.lowercase()
        return if (w.length in 2..40 && w.all { it in 'а'..'ӿ' && it.isLetter() }) w else null
    }

    /** Counts one more use of a word (its Cyrillic spelling). */
    fun learn(cyrillic: String) {
        if (!isEnabled()) return
        val w = normalized(cyrillic) ?: return
        val map = load()
        map[w] = (map[w] ?: 0) + 1
        version++
        scheduleSave()
    }

    /** Every learned word with its count; empty while Personalize T9 is off. */
    fun snapshot(): Map<String, Int> = if (isEnabled()) load() else emptyMap()

    /** The whole list, also while Personalize T9 is off (for the page that shows it). */
    fun all(context: Context): Map<String, Int> {
        attach(context)
        return load()
    }

    /** Forgets one word. */
    fun remove(context: Context, word: String) {
        attach(context)
        if (load().remove(word) != null) { version++; scheduleSave() }
    }

    /** Forgets everything, in memory and on disk. */
    fun clear(context: Context) {
        attach(context)
        counts = ConcurrentHashMap()
        version++
        try { File(context.filesDir, FILE).delete() } catch (_: Exception) {}
    }

    private fun scheduleSave() {
        if (saveScheduled) return
        saveScheduled = true
        // Written on its own thread a few seconds later, so neither typing nor a lookup waits for the disk.
        saver.schedule({ flush() }, 3, TimeUnit.SECONDS)
    }

    /** Writes the list to disk now (also when the keyboard goes away). */
    fun flush() {
        saveScheduled = false
        val ctx = appContext ?: return
        val map = counts ?: return
        try {
            val text = map.entries.joinToString("\n") { "${it.key}\t${it.value}" }
            File(ctx.filesDir, FILE).writeText(text)
        } catch (_: Exception) {
        }
    }
}
