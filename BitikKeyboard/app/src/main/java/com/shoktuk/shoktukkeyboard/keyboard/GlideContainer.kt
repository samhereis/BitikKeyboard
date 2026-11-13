package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.suggestions.WordSuggestionEngine
import kotlin.math.hypot

/**
 * Root of a letter keyboard. On Latin and Kiril, a touch that starts on a letter and then travels more than about a
 * key and a half becomes a glide: the keys get ACTION_CANCEL, the finger path is recorded, and on release the best
 * matching word is typed with a space (the typing suggestions bar is not involved).
 */
class GlideContainer(context: Context, private val isMainLetters: Boolean) : LinearLayout(context) {
    private val trace = ArrayList<PointF>()
    private var gliding = false
    private var downOnLetter = false
    private var startX = 0f
    private var startY = 0f

    // The finger trail shown while gliding (like the iOS keyboard): a soft blue line that disappears on release.
    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0x8C007AFF.toInt()
        strokeWidth = 6f * resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val trailPath = Path()
    private val origin = IntArray(2)

    init { setWillNotDraw(false) }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (!gliding || trace.size < 2) return
        getLocationOnScreen(origin)
        trailPath.rewind()
        trailPath.moveTo(trace[0].x - origin[0], trace[0].y - origin[1])
        for (i in 1 until trace.size) trailPath.lineTo(trace[i].x - origin[0], trace[i].y - origin[1])
        canvas.drawPath(trailPath, trailPaint)
    }

    private fun enabled(): Boolean = isMainLetters && MyKeyboardService.current_writingSystem.let {
        it == WritingSystem.Latin || it == WritingSystem.Kiril || it == WritingSystem.Arab || it == WritingSystem.Bitik
    }

    /** Every letter key with its centre in screen coordinates (Bitik letters are two UTF-16 units, so the key is its text). */
    private fun keyCentresByText(): Map<String, PointF> {
        val out = HashMap<String, PointF>()
        val loc = IntArray(2)
        fun walk(v: View) {
            if (v is KeyView) {
                val lower = v.key.lowercase
                if (lower.isNotEmpty() && lower.codePointCount(0, lower.length) == 1 && v.width > 0) {
                    v.getLocationOnScreen(loc)
                    out[lower] = PointF(loc[0] + v.width / 2f, loc[1] + v.height / 2f)
                }
            } else if (v is ViewGroup) for (i in 0 until v.childCount) walk(v.getChildAt(i))
        }
        walk(this)
        return out
    }

    /** The same keys for the dictionary scripts, where every letter is one Char. */
    private fun keyCentres(): Map<Char, PointF> {
        val out = HashMap<Char, PointF>()
        for ((text, p) in keyCentresByText()) if (text.length == 1) out[text[0]] = p
        return out
    }

    /**
     * The glyphs the finger clearly aimed at: the first and last key plus every key where it turned (a straight pass over
     * a key does not count). Used for Bitik, where a word is found from the keys typed rather than from the finger's shape.
     */
    private fun aimedGlyphs(points: List<PointF>, centres: Map<String, PointF>, kw: Float, passAll: Boolean = false): List<String> {
        if (centres.isEmpty() || points.size < 2) return emptyList()
        class Run(val letter: String, var closest: Float, var sample: Int)
        val samples = ArrayList<PointF>()
        val runs = ArrayList<Run>()
        fun add(p: PointF) {
            var best: String? = null; var bestD = Float.MAX_VALUE
            for ((l, c) in centres) { val d = hypot(c.x - p.x, c.y - p.y); if (d < bestD) { bestD = d; best = l } }
            samples.add(p)
            val letter = best ?: return
            val closeness = bestD / kw
            if (runs.isEmpty() || runs.last().letter != letter) runs.add(Run(letter, closeness, samples.size - 1))
            else if (closeness < runs.last().closest) { runs.last().closest = closeness; runs.last().sample = samples.size - 1 }
        }
        add(points[0])
        for (i in 1 until points.size) {
            val a = points[i - 1]; val b = points[i]
            val steps = maxOf((hypot(b.x - a.x, b.y - a.y) / 4f).toInt(), 1)
            for (s in 1..steps) { val t = s / steps.toFloat(); add(PointF(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)) }
        }
        val out = ArrayList<String>()
        val k = 3
        for ((i, run) in runs.withIndex()) {
            val isEnd = i == 0 || i == runs.size - 1
            var turned = false
            val s = run.sample
            if (!isEnd && run.closest < 0.55f && s - k >= 0 && s + k < samples.size) {
                val ax = samples[s].x - samples[s - k].x; val ay = samples[s].y - samples[s - k].y
                val bx = samples[s + k].x - samples[s].x; val by = samples[s + k].y - samples[s].y
                val la = hypot(ax, ay); val lb = hypot(bx, by)
                if (la > 0.5f && lb > 0.5f) turned = (ax * bx + ay * by) / (la * lb) < 0.8f
            }
            if (isEnd || turned || (passAll && run.closest < 0.5f)) out.add(run.letter)
        }
        return out
    }

    private fun keyWidth(centres: Map<Char, PointF>): Float = keyWidthOf(centres.values)

    private fun keyWidthOf(points: Collection<PointF>): Float {
        // Distance between the two nearest keys on the same row is one key width.
        var best = Float.MAX_VALUE
        val list = points.toList()
        for (a in list) for (b in list) if (a !== b && kotlin.math.abs(a.y - b.y) < 8f) best = minOf(best, kotlin.math.abs(a.x - b.x))
        return if (best == Float.MAX_VALUE) width / 10f else best
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!enabled()) return false
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                gliding = false
                trace.clear()
                startX = ev.rawX; startY = ev.rawY
                trace.add(PointF(ev.rawX, ev.rawY))
                val textCentres = keyCentresByText()
                val centres = keyCentres()
                val kw = keyWidthOf(textCentres.values)
                downOnLetter = textCentres.values.any { hypot(it.x - ev.rawX, it.y - ev.rawY) < kw * 0.9f }
                if (downOnLetter && MyKeyboardService.current_writingSystem != WritingSystem.Bitik) {
                    // Read the word files this glide may need now, while the finger travels, so the release doesn't wait for them.
                    val startLetter = centres.minByOrNull { hypot(it.value.x - ev.rawX, it.value.y - ev.rawY) }?.key
                    if (startLetter != null) {
                        val configure = WordSuggestionEngine.configurator(MyKeyboardService.context)
                        com.shoktuk.shoktukkeyboard.suggestions.SuggestionWorker.background {
                            configure()
                            WordSuggestionEngine.prefetchGlide(centres.mapValues { WordSuggestionEngine.GlidePoint(it.value.x, it.value.y) }, kw, startLetter, WordSuggestionEngine.GlidePoint(ev.rawX, ev.rawY))
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!downOnLetter || gliding) return gliding
                trace.add(PointF(ev.rawX, ev.rawY))
                if (hypot(ev.rawX - startX, ev.rawY - startY) > keyWidthOf(keyCentresByText().values) * 1.5f) {
                    gliding = true
                    invalidate()
                    return true  // the keys receive ACTION_CANCEL
                }
            }
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!gliding) return super.onTouchEvent(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_MOVE -> { trace.add(PointF(ev.rawX, ev.rawY)); invalidate() }
            MotionEvent.ACTION_UP -> {
                trace.add(PointF(ev.rawX, ev.rawY))
                gliding = false
                invalidate()
                finish()
            }
            MotionEvent.ACTION_CANCEL -> { gliding = false; invalidate() }
        }
        return true
    }

    private fun finishBitik() {
        val service = MyKeyboardService.context
        val ic = service.currentInputConnection ?: return
        val centres = keyCentresByText()
        if (centres.isEmpty() || trace.size < 3) return
        val points = ArrayList(trace)
        val kw = keyWidthOf(centres.values)
        val glyphs = aimedGlyphs(points, centres, kw)
        val passed = aimedGlyphs(points, centres, kw, passAll = true)
        if (glyphs.size < 2) return
        val (zh, ch, ng, o) = with(com.shoktuk.shoktukkeyboard.project.data.SettingsManager) { listOf(service.latinZH.id, service.latinCH.id, service.latinNG.id, service.latinO.id) }
        com.shoktuk.shoktukkeyboard.suggestions.SuggestionWorker.submit({
            WordSuggestionEngine.configure(service, cyrillic = true, zh = zh, ch = ch, ng = ng, o = o)
            // A word fits when its glyphs appear, in order, among the keys the finger crossed; the turns alone are the fallback.
            val passedReadings = passed.map { BitikReadings.of(service, it) }
            var best = if (passedReadings.any { it.isEmpty() }) null else WordSuggestionEngine.bitikGlideMatches(passedReadings, 8).firstOrNull()
            if (best == null) {
                val readings = glyphs.map { BitikReadings.of(service, it) }
                if (readings.any { it.isEmpty() }) return@submit null
                best = WordSuggestionEngine.bitikCompletions(readings, 8).firstOrNull()
            }
            if (best == null) return@submit null
            BitikSuggestionStrip.bitikSpelling(service, best.cyr)
        }) { word ->
            if (word == null) return@submit
            ic.commitText(word + BitikSuggestionStrip.separator(), 1)
            TopRowBuilder_Old.onTypedListener.invoke()
        }
    }

    private fun finish() {
        if (MyKeyboardService.current_writingSystem == WritingSystem.Bitik) { finishBitik(); return }
        val service = MyKeyboardService.context
        val ic = service.currentInputConnection ?: return
        val centres = keyCentres()
        if (centres.isEmpty() || trace.size < 3) return
        val configure = WordSuggestionEngine.configurator(service)
        val begin = trace.first()
        val startLetter = centres.minByOrNull { hypot(it.value.x - begin.x, it.value.y - begin.y) }?.key ?: return
        val centresForEngine = centres.mapValues { WordSuggestionEngine.GlidePoint(it.value.x, it.value.y) }
        val points = trace.map { WordSuggestionEngine.GlidePoint(it.x, it.y) }
        val width = keyWidth(centres)
        // Matching runs on the suggestion thread; the word is typed when it is found.
        com.shoktuk.shoktukkeyboard.suggestions.SuggestionWorker.submit({
            configure()
            WordSuggestionEngine.glideMatches(points, centresForEngine, width, startLetter).firstOrNull()
        }) { best ->
            if (best == null) return@submit
            val word = if (MyKeyboardService.isCaps) best.replaceFirstChar { it.uppercase() } else best
            ic.commitText("$word ", 1)
            if (MyKeyboardService.isCaps) {
                MyKeyboardService.isCaps = false
                service.applyKeyboard()
            }
            TopRowBuilder_Old.onTypedListener.invoke()
        }
    }
}
