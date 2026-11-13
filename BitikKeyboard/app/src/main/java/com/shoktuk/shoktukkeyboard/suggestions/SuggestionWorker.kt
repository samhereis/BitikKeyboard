package com.shoktuk.shoktukkeyboard.suggestions

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

/**
 * The one background thread every suggestion lookup, dictionary load and learned-word save runs on, so a key press never
 * waits for a search. The newest request wins: a lookup still queued when the next key arrives never starts, and one
 * that is running stops at its next [isStale] check. All dictionary state is touched from this thread only.
 */
object SuggestionWorker {
    private val main by lazy { Handler(Looper.getMainLooper()) }
    private val executor by lazy { Executors.newSingleThreadExecutor { r -> Thread(r, "suggestions").apply { priority = Thread.NORM_PRIORITY - 1 } } }

    @Volatile private var latest = 0L
    @Volatile private var running = 0L

    /** True once a newer lookup has been submitted since the running one started. Cheap: call it every few hundred words. */
    fun isStale(): Boolean = running != latest

    /**
     * Runs [work] on the worker and hands its result to [deliver] on the main thread, unless a newer [submit] came in
     * meanwhile (then neither runs / is delivered). [work] returns null when it stopped because it went stale.
     */
    fun <T> submit(work: () -> T, deliver: (T) -> Unit) {
        val mine = ++latest
        executor.execute {
            if (mine != latest) return@execute
            running = mine
            val result = try { work() } catch (e: Exception) { return@execute }
            if (mine != latest) return@execute
            main.post { if (mine == latest) deliver(result) }
        }
    }

    /** Drops any lookup still waiting or running (the screen was updated without one). */
    fun cancel() { latest++ }

    /** Runs [work] on the worker with no result; it never counts as "newest" and is never skipped. */
    fun background(work: () -> Unit) {
        executor.execute { try { work() } catch (_: Exception) {} }
    }
}
