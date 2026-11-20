package com.shoktuk.shoktukkeyboard.bench

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.shoktuk.shoktukkeyboard.keyboard.KeyView
import com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService

/**
 * Typing benchmark for the keyboard service. Does nothing in a release build.
 *
 * Everything is reported to logcat under the tag "TypingBench":
 *  - every key press section that took longer than [SLOW_MS] is logged as it happens ("SLOW ..."),
 *  - every main-thread frame gap longer than [STALL_MS] is logged with the last key and the GC count since the previous
 *    stall ("STALL ...") — this is the "it freezes for a second" you can feel, wherever it comes from,
 *  - a summary (count, p50, p95, p99, max per section) is printed every [SUMMARY_EVERY] keys and at the end of a run.
 *
 * Pass real text with `--es b64 <base64 of a UTF-8 text>` (bench_typing.sh does it from a file); without it a short sample is typed.
 *
 * Run a reproducible fast-typing session without touching the screen (focus any text field first):
 *   adb shell am broadcast -a com.shoktuk.shoktukkeyboard.BENCH --ei count 400 --ei interval 35 -p com.shoktuk.shoktukkeyboard
 * Or just type by hand and read:  adb logcat -s TypingBench
 */
object TypingBench {
    private const val TAG = "TypingBench"
    private const val ACTION = "com.shoktuk.shoktukkeyboard.BENCH"
    private const val SLOW_MS = 8.0
    private const val STALL_MS = 50.0
    private const val SUMMARY_EVERY = 100

    private class Stat { val ms = ArrayList<Double>(1024) }
    private val stats = LinkedHashMap<String, Stat>()
    private var enabled = false
    private var keys = 0
    private var lastKey = ""
    private var lastGc = 0L
    private var lastFrameNs = 0L
    private val main by lazy { Handler(Looper.getMainLooper()) }
    private var receiver: BroadcastReceiver? = null

    fun install(service: MyKeyboardService) {
        if (service.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) return
        enabled = true
        lastGc = gcCount()
        Choreographer.getInstance().postFrameCallback(frame)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) {
                run(service, i.getIntExtra("count", 300), i.getIntExtra("interval", 40).toLong(), bodyOf(i), i.getBooleanExtra("touch", false), i.getBooleanExtra("bitik", false), i.getIntExtra("tap", 45).toLong(), i.getIntExtra("drift", 0))
            }
        }
        val filter = IntentFilter(ACTION)
        if (android.os.Build.VERSION.SDK_INT >= 33) service.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED) else service.registerReceiver(receiver, filter)
        Log.i(TAG, "ready: adb shell am broadcast -a $ACTION --ei count 400 --ei interval 35 -p ${service.packageName}")
    }

    fun uninstall(service: MyKeyboardService) {
        if (!enabled) return
        enabled = false
        Choreographer.getInstance().removeFrameCallback(frame)
        receiver?.let { try { service.unregisterReceiver(it) } catch (_: Exception) {} }
    }

    /** Times [block] under [name]; logs it when it was slow. */
    inline fun <T> measure(name: String, block: () -> T): T {
        if (!isEnabled) return block()
        val t0 = System.nanoTime()
        try { return block() } finally { record(name, (System.nanoTime() - t0) / 1e6) }
    }

    val isEnabled: Boolean get() = enabled

    fun record(name: String, ms: Double) {
        stats.getOrPut(name) { Stat() }.ms.add(ms)
        if (ms >= SLOW_MS) Log.w(TAG, "SLOW %-16s %6.1f ms  (key #%d '%s')".format(name, ms, keys, lastKey))
        if (name == "key total") {
            keys++
            if (keys % SUMMARY_EVERY == 0) summary()
        }
    }

    private val counts = HashMap<String, Int>()
    fun count(name: String) { if (enabled) counts[name] = (counts[name] ?: 0) + 1 }

    fun note(message: String) { if (enabled) Log.w(TAG, "NOTE $message") }

    fun noteKey(key: String) { if (enabled) lastKey = key }

    private val frame = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!enabled) return
            if (lastFrameNs != 0L) {
                val gap = (frameTimeNanos - lastFrameNs) / 1e6
                if (gap >= STALL_MS) {
                    val gc = gcCount()
                    val rt = Runtime.getRuntime()
                    Log.e(TAG, "STALL main thread froze %.0f ms after key #%d '%s'; GCs since last stall: %d; heap %d/%d MB".format(
                        gap, keys, lastKey, gc - lastGc, (rt.totalMemory() - rt.freeMemory()) shr 20, rt.maxMemory() shr 20))
                    lastGc = gc
                    stats.getOrPut("frame stall") { Stat() }.ms.add(gap)
                }
            }
            lastFrameNs = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    private fun gcCount(): Long = Debug.getRuntimeStat("art.gc.gc-count")?.toLongOrNull() ?: 0L

    fun summary() {
        val sb = StringBuilder("summary after $keys keys\n")
        sb.append("%-16s %6s %8s %8s %8s %8s\n".format("section", "n", "p50", "p95", "p99", "max"))
        for ((name, s) in stats) {
            val v = s.ms.sorted()
            fun p(q: Double) = v[((v.size - 1) * q).toInt()]
            sb.append("%-16s %6d %7.2fms %7.2fms %7.2fms %7.2fms\n".format(name, v.size, p(0.5), p(0.95), p(0.99), v.last()))
        }
        Log.i(TAG, sb.toString())
    }

    /** The centre of the on-screen key that types [ch], or null when there is none. */
    private fun keyCentre(root: View, ch: String): Pair<Float, Float>? {
        val loc = IntArray(2)
        fun walk(v: View): Pair<Float, Float>? {
            if (v is KeyView) {
                if (v.key.lowercase == ch.lowercase() && v.width > 0 && v.isShown) {
                    v.getLocationOnScreen(loc)
                    return Pair(loc[0] + v.width / 2f, loc[1] + v.height / 2f)
                }
            } else if (v is ViewGroup) for (c in 0 until v.childCount) walk(v.getChildAt(c))?.let { return it }
            return null
        }
        return walk(root)
    }

    /** Sends a finger-down and, [TAP_MS] later, a finger-up at the key's screen position through the window, exactly where a thumb would. */
    private fun tap(service: MyKeyboardService, ch: String): Boolean {
        val decor = service.window?.window?.decorView ?: return false
        val (x, y) = keyCentre(decor, ch) ?: return false
        val at = IntArray(2); decor.getLocationOnScreen(at)
        fun event(action: Int, t0: Long, now: Long) = MotionEvent.obtain(t0, now, action, x, y, 0).also { it.offsetLocation(-at[0].toFloat(), -at[1].toFloat()) }
        val t0 = SystemClock.uptimeMillis()
        val down = event(MotionEvent.ACTION_DOWN, t0, t0)
        measure("key total") { decor.dispatchTouchEvent(down) }
        down.recycle()
        // A thumb slides a little while it taps: a move halfway, then the lift a little off where it came down.
        val dx = if (tapDrift > 0) (random.nextInt(2 * tapDrift + 1) - tapDrift).toFloat() else 0f
        val dy = if (tapDrift > 0) (random.nextInt(2 * tapDrift + 1) - tapDrift).toFloat() else 0f
        fun at(action: Int, t1: Long, ox: Float, oy: Float) = MotionEvent.obtain(t0, t1, action, x + ox, y + oy, 0).also { it.offsetLocation(-at[0].toFloat(), -at[1].toFloat()) }
        if (tapDrift > 0) main.postDelayed({
            val move = at(MotionEvent.ACTION_MOVE, SystemClock.uptimeMillis(), dx / 2, dy / 2)
            decor.dispatchTouchEvent(move); move.recycle()
        }, tapDuration / 2)
        main.postDelayed({
            val up = at(MotionEvent.ACTION_UP, SystemClock.uptimeMillis(), dx, dy)
            measure("key up") { decor.dispatchTouchEvent(up) }
            up.recycle()
        }, tapDuration)
        nextDownNotBefore = SystemClock.uptimeMillis() + tapDuration + 2  // a finger is never put down again before the last one has lifted, however far the main thread has fallen behind
        return true
    }

    private var nextDownNotBefore = 0L
    private var tapDuration = 45L
    private var tapDrift = 0
    private val sentLetters = StringBuilder()
    private val random = java.util.Random(7)

    /** The Bitik glyph keys that spell the Cyrillic [text], found by asking each key on screen for the Cyrillic letters it can stand for. Other characters are kept. */
    private fun bitikKeys(service: MyKeyboardService, text: String): List<String> {
        val decor = service.window?.window?.decorView ?: return listOf(" ")
        val byLetter = HashMap<Char, Pair<String, Int>>()  // letter -> (glyph, how many readings it has: fewer = more exact)
        fun walk(v: View) {
            if (v is KeyView) {
                val g = v.key.lowercase
                if (g.isNotEmpty() && g.codePointCount(0, g.length) == 1 && v.width > 0) {
                    val readings = com.shoktuk.shoktukkeyboard.keyboard.BitikReadings.of(service, g)
                    for (r in readings) if (r.length == 1 && (byLetter[r[0]]?.second ?: Int.MAX_VALUE) > readings.size) byLetter[r[0]] = g to readings.size
                }
            } else if (v is ViewGroup) for (c in 0 until v.childCount) walk(v.getChildAt(c))
        }
        walk(decor)
        Log.i(TAG, "bitik: ${byLetter.size} letters mapped to glyph keys")
        return text.lowercase().mapNotNull { c -> if (c == ' ') " " else byLetter[c]?.first }.ifEmpty { listOf(" ") }
    }

    private fun bodyOf(i: Intent): String =
        i.getStringExtra("b64")?.let { String(android.util.Base64.decode(it, android.util.Base64.DEFAULT), Charsets.UTF_8) }
            ?: "abcdefghij klmnopqrst. "

    /** Types [text] over and over, [count] keys, one every [intervalMs], through the same path a real key takes. */
    private fun run(service: MyKeyboardService, count: Int, intervalMs: Long, text: String, touch: Boolean, bitik: Boolean, tapMs: Long, drift: Int) {
        tapDuration = tapMs; tapDrift = drift
        sentLetters.clear()
        val before = service.currentInputConnection?.getTextBeforeCursor(100000, 0)?.toString().orEmpty()
        val tokens = if (bitik) bitikKeys(service, text) else text.codePoints().toArray().map { String(Character.toChars(it)) }
        stats.clear(); keys = 0; counts.clear()
        Log.i(TAG, "run: $count keys, one every $intervalMs ms" + if (touch) ", as real touches on the key views" else ", straight into the key handler")
        val start = SystemClock.uptimeMillis()
        var i = 0
        val tick = object : Runnable {
            override fun run() {
                val ic = service.currentInputConnection
                if (ic == null) { Log.e(TAG, "no input connection: focus a text field first"); return }
                if (i >= count) {
                    main.postDelayed({
                        summary()
                        val after = ic.getTextBeforeCursor(100000, 0)?.toString().orEmpty()
                        val got = after.drop(before.length).filter { it.isLetter() }.lowercase()
                        val sent = sentLetters.toString().lowercase()
                        Log.i(TAG, "KEYS sent ${sent.length} letters, the field received ${got.length}: ${sent.length - got.length} lost (${"%.1f".format(100.0 * (sent.length - got.length) / maxOf(sent.length, 1))}%)")
                        Log.i(TAG, "COUNTS $counts")
                        Log.i(TAG, "SENT ${sent.take(3000)}")
                        Log.i(TAG, "GOT  ${got.take(3000)}")
                        Log.i(TAG, "run finished in ${SystemClock.uptimeMillis() - start} ms")
                    }, 1500)
                    return
                }
                val ch = tokens[i % tokens.size]
                noteKey(ch)
                if (touch && tap(service, ch)) { /* a real touch on the key view */ }
                else measure("key total") { service.handleKeyPress(ic, ch, ch == " " || ch == "." || ch == ",") }
                if (ch.length == 1 && ch[0].isLetter()) sentLetters.append(ch)
                i++
                main.postAtTime(this, maxOf(start + i * intervalMs, nextDownNotBefore))
            }
        }
        main.post(tick)
    }
}
