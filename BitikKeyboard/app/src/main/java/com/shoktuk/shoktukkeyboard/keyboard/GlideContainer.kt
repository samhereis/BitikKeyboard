package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
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

    private fun enabled(): Boolean = isMainLetters &&
        (MyKeyboardService.current_writingSystem == WritingSystem.Latin || MyKeyboardService.current_writingSystem == WritingSystem.Kiril)

    /** Every letter key with its centre in screen coordinates. */
    private fun keyCentres(): Map<Char, PointF> {
        val out = HashMap<Char, PointF>()
        val loc = IntArray(2)
        fun walk(v: View) {
            if (v is KeyView) {
                val lower = v.key.lowercase
                if (lower.length == 1 && v.width > 0) {
                    v.getLocationOnScreen(loc)
                    out[lower[0]] = PointF(loc[0] + v.width / 2f, loc[1] + v.height / 2f)
                }
            } else if (v is ViewGroup) for (i in 0 until v.childCount) walk(v.getChildAt(i))
        }
        walk(this)
        return out
    }

    private fun keyWidth(centres: Map<Char, PointF>): Float {
        // Distance between the two nearest keys on the same row is one key width.
        var best = Float.MAX_VALUE
        val list = centres.values.toList()
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
                val centres = keyCentres()
                val kw = keyWidth(centres)
                downOnLetter = centres.values.any { hypot(it.x - ev.rawX, it.y - ev.rawY) < kw * 0.9f }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!downOnLetter || gliding) return gliding
                trace.add(PointF(ev.rawX, ev.rawY))
                if (hypot(ev.rawX - startX, ev.rawY - startY) > keyWidth(keyCentres()) * 1.5f) {
                    gliding = true
                    return true  // the keys receive ACTION_CANCEL
                }
            }
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!gliding) return super.onTouchEvent(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_MOVE -> trace.add(PointF(ev.rawX, ev.rawY))
            MotionEvent.ACTION_UP -> {
                trace.add(PointF(ev.rawX, ev.rawY))
                gliding = false
                finish()
            }
            MotionEvent.ACTION_CANCEL -> gliding = false
        }
        return true
    }

    private fun finish() {
        val service = MyKeyboardService.context
        val ic = service.currentInputConnection ?: return
        val centres = keyCentres()
        if (centres.isEmpty() || trace.size < 3) return
        WordSuggestionEngine.configureFor(service)
        val begin = trace.first()
        val startLetter = centres.minByOrNull { hypot(it.value.x - begin.x, it.value.y - begin.y) }?.key ?: return
        val best = WordSuggestionEngine.glideMatches(trace, centres, keyWidth(centres), startLetter).firstOrNull() ?: return
        val word = if (MyKeyboardService.isCaps) best.replaceFirstChar { it.uppercase() } else best
        ic.commitText("$word ", 1)
        if (MyKeyboardService.isCaps) {
            MyKeyboardService.isCaps = false
            service.applyKeyboard()
        }
        TopRowBuilder_Old.onTypedListener.invoke()
    }
}
