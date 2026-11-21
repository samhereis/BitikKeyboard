package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * The keyboard's resize mode. It shows the keyboard as plain keys (no labels) with these buttons on top:
 *  - top arrows: drag to change the keyboard height, the bottom edge stays where it is;
 *  - bottom arrows: drag to change the height and the bottom offset together, the top edge stays where it is;
 *  - centre arrows: drag to move the whole keyboard up or down (the bottom offset);
 *  - the check mark: done.
 *
 * The standard size is drawn as dashed lines and the edges snap to it when they come close.
 *
 * This view fills the whole keyboard window, including the gap under the keyboard, so it can draw the standard bottom line wherever it falls.
 * Height is the per-key height setting and offset is the bottom offset setting, both in pixels as everywhere else.
 * [onChange] is told the new values while a finger is dragging; the owner stores them and resizes this view to [windowHeight].
 */
class ResizeOverlayView(
    context: Context,
    startHeight: Int,
    startOffset: Int,
    private val defaultHeight: Int,
    private val defaultOffset: Int,
    /** The space under the keyboard before the offset is added (the navigation bar). */
    private val basePadding: Int,
    private val onChange: (keyHeight: Int, bottomOffset: Int) -> Unit,
    /** The finger lifted after the height changed: the owner can draw the keyboard again at the new size, see [setLook]. */
    private val onReleased: () -> Unit,
    private val onDone: () -> Unit
) : View(context) {

    companion object {
        const val MIN_KEY_HEIGHT = 100
        const val MAX_KEY_HEIGHT = 300
        const val MIN_OFFSET = -200
        const val MAX_OFFSET = 200

        /** A keyboard is four key rows and a top bar a third of a row shorter: total = key height * (4 + 1/1.5). */
        const val TOTAL_FACTOR = 4f + 1f / 1.5f
    }

    private enum class Handle { Top, Bottom, Center, Done }

    private var keyHeight = startHeight
    private var offset = startOffset

    private val density = resources.displayMetrics.density
    private fun dp(v: Float) = v * density

    private val bg = KeyboardTheme.getColor(0).toColorInt()
    private val dark = ColorUtils.calculateLuminance(bg) < 0.5
    private val accent = if (dark) 0xFF9DB0FF.toInt() else 0xFF3F5BD9.toInt()
    private val onAccent = if (dark) 0xFF0E1A4A.toInt() else 0xFFFFFFFF.toInt()
    private val neutral = if (dark) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
    private val keyFill = KeyboardTheme.getSystemButtonStyle(context).fillColor.toColorInt()

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ColorUtils.setAlphaComponent(keyFill, 120) }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
        pathEffect = DashPathEffect(floatArrayOf(dp(7f), dp(6f)), 0f)
    }
    private val rect = RectF()
    private val path = Path()

    /** The real keyboard with its labels hidden (the look of the cursor-drag mode). It is stretched while a finger drags, and drawn again on release. */
    private var look: Bitmap? = null
    private val lookPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val lookSrc = Rect()

    fun setLook(bitmap: Bitmap?) {
        look = bitmap
        invalidate()
    }

    private var pressed: Handle? = null
    private var downRawY = 0f
    private var downHeight = 0
    private var downOffset = 0
    private var wasAtDefaultHeight = startHeight == defaultHeight
    private var wasAtDefaultOffset = startOffset == defaultOffset

    /** The height of the keyboard itself for a given key height; the same sum the real keyboard uses. */
    fun totalFor(keyHeight: Int): Int = (keyHeight * TOTAL_FACTOR).toInt()

    /**
     * The height of the keyboard window: the keyboard and the gap under it. A negative offset eats into the keyboard, so the window
     * is shorter than the keyboard and its bottom rows are cut off; the real keyboard does just that, so this view does too.
     */
    fun windowHeight(): Int = max(dp(150f).toInt(), totalFor(keyHeight) + basePadding + offset)

    private val keyboardHeight get() = totalFor(keyHeight).toFloat()

    /** The part of the keyboard that can be seen. */
    private val visibleHeight get() = min(keyboardHeight, windowHeight().toFloat())

    // Pill buttons sit on the visible keyboard area, the check on the right.
    private fun centre(h: Handle): Pair<Float, Float> = when (h) {
        Handle.Top -> width / 2f to dp(30f)
        Handle.Bottom -> width / 2f to visibleHeight - dp(30f)
        Handle.Center -> width / 2f to visibleHeight / 2f
        Handle.Done -> width * 0.84f to visibleHeight / 2f
    }

    private fun halfSize(h: Handle): Pair<Float, Float> = if (h == Handle.Done) dp(32f) to dp(32f) else dp(34f) to dp(20f)

    private fun hit(x: Float, y: Float): Handle? {
        // The touch area is a little bigger than what is drawn.
        for (h in listOf(Handle.Done, Handle.Center, Handle.Top, Handle.Bottom)) {
            val (cx, cy) = centre(h)
            val (hw, hh) = halfSize(h)
            if (abs(x - cx) <= hw + dp(10f) && abs(y - cy) <= hh + dp(10f)) return h
        }
        return null
    }

    /** Pulls [value] to [target] when it is within a few dp of it. */
    private fun snap(value: Float, target: Float, unit: Float = 1f): Float = if (abs(value - target) * unit <= dp(9f)) target else value

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                pressed = hit(ev.x, ev.y) ?: return true
                downRawY = ev.rawY
                downHeight = keyHeight
                downOffset = offset
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                val h = pressed ?: return true
                if (h == Handle.Done) return true
                // The finger's move on the screen, down positive. The view moves with the finger, so this is measured from where the touch began.
                val d = ev.rawY - downRawY
                val startTotal = totalFor(downHeight).toFloat()
                var newOffset = offset
                var newHeight = keyHeight
                when (h) {
                    Handle.Top -> {
                        val keys = snap((startTotal - d) / TOTAL_FACTOR, defaultHeight.toFloat(), TOTAL_FACTOR)
                        newHeight = Math.round(keys).coerceIn(MIN_KEY_HEIGHT, MAX_KEY_HEIGHT)
                    }

                    Handle.Bottom -> {
                        newOffset = Math.round(snap(downOffset - d, defaultOffset.toFloat())).coerceIn(MIN_OFFSET, MAX_OFFSET)
                        // The top edge stays: what the bottom edge gives up in offset, the keyboard gains in height.
                        newHeight = Math.round((startTotal + (downOffset - newOffset)) / TOTAL_FACTOR).coerceIn(MIN_KEY_HEIGHT, MAX_KEY_HEIGHT)
                    }

                    Handle.Center -> newOffset = Math.round(snap(downOffset - d, defaultOffset.toFloat())).coerceIn(MIN_OFFSET, MAX_OFFSET)
                    Handle.Done -> {}
                }
                if (newHeight != keyHeight || newOffset != offset) {
                    keyHeight = newHeight
                    offset = newOffset
                    val atHeight = keyHeight == defaultHeight
                    val atOffset = offset == defaultOffset
                    if ((atHeight && !wasAtDefaultHeight) || (atOffset && !wasAtDefaultOffset)) performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    wasAtDefaultHeight = atHeight
                    wasAtDefaultOffset = atOffset
                    onChange(keyHeight, offset)
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                val h = pressed
                pressed = null
                invalidate()
                if ((h == Handle.Top || h == Handle.Bottom) && keyHeight != downHeight) onReleased()
                if (h == Handle.Done && hit(ev.x, ev.y) == Handle.Done) {
                    performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onDone()
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                pressed = null
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val total = keyboardHeight
        canvas.drawColor(bg)

        drawKeys(canvas, w, total)
        drawStandardLines(canvas, w, total)

        // The two edges that move.
        linePaint.color = ColorUtils.setAlphaComponent(accent, 200)
        linePaint.strokeWidth = dp(2f)
        linePaint.pathEffect = null
        canvas.drawLine(0f, dp(1f), w, dp(1f), linePaint)
        canvas.drawLine(0f, total - dp(1f), w, total - dp(1f), linePaint)

        drawPill(canvas, Handle.Top)
        drawPill(canvas, Handle.Bottom)
        drawPill(canvas, Handle.Center)
        drawDone(canvas)
    }

    /** The keyboard without labels: the real one when there is a picture of it, else a bar on top and four rows of plain keys. */
    private fun drawKeys(canvas: Canvas, w: Float, total: Float) {
        look?.let {
            lookSrc.set(0, 0, it.width, it.height)
            rect.set(0f, 0f, w, total)
            canvas.drawBitmap(it, lookSrc, rect, lookPaint)
            return
        }
        val gap = dp(4f)
        val barHeight = total / TOTAL_FACTOR / 1.5f
        val rowHeight = (total - barHeight) / 4f
        val columns = 10
        val keyWidth = (w - gap * (columns + 1)) / columns
        for (row in 0 until 4) {
            val top = barHeight + row * rowHeight + gap / 2
            for (c in 0 until columns) {
                val left = gap + c * (keyWidth + gap)
                rect.set(left, top, left + keyWidth, top + rowHeight - gap)
                canvas.drawRoundRect(rect, dp(6f), dp(6f), keyPaint)
            }
        }
    }

    /** Where the keyboard's edges would be at the standard size; they light up while the real edge sits on them. */
    private fun drawStandardLines(canvas: Canvas, w: Float, total: Float) {
        val standardBottom = total + (offset - defaultOffset)
        val standardTop = standardBottom - totalFor(defaultHeight)
        for ((y, on) in listOf(standardTop to (keyHeight == defaultHeight), standardBottom to (offset == defaultOffset))) {
            if (y < 0f || y > height) continue
            dashPaint.color = if (on) accent else ColorUtils.setAlphaComponent(neutral, 110)
            dashPaint.strokeWidth = if (on) dp(2.5f) else dp(1.5f)
            canvas.drawLine(0f, y, w, y, dashPaint)
        }
    }

    private fun drawPill(canvas: Canvas, handle: Handle) {
        val (cx, cy) = centre(handle)
        val (hw, hh) = halfSize(handle)
        val grow = if (pressed == handle) 1.08f else 1f
        rect.set(cx - hw * grow, cy - hh * grow, cx + hw * grow, cy + hh * grow)
        fillPaint.color = ColorUtils.compositeColors(ColorUtils.setAlphaComponent(bg, 235), keyFill)
        canvas.drawRoundRect(rect, rect.height() / 2, rect.height() / 2, fillPaint)
        linePaint.pathEffect = null
        linePaint.color = accent
        linePaint.strokeWidth = dp(1.5f)
        linePaint.style = Paint.Style.STROKE
        canvas.drawRoundRect(rect, rect.height() / 2, rect.height() / 2, linePaint)

        // Up and down chevrons. The top and bottom buttons mark the edge they move with a bar; the middle one, which moves the whole keyboard, has none.
        linePaint.strokeWidth = dp(2.4f)
        val s = dp(6f)
        val gapY = dp(4.5f)
        path.reset()
        path.moveTo(cx - s, cy - gapY)
        path.lineTo(cx, cy - gapY - s * 0.9f)
        path.lineTo(cx + s, cy - gapY)
        path.moveTo(cx - s, cy + gapY)
        path.lineTo(cx, cy + gapY + s * 0.9f)
        path.lineTo(cx + s, cy + gapY)
        canvas.drawPath(path, linePaint)
        when (handle) {
            Handle.Top -> canvas.drawLine(cx - s * 1.5f, cy - hh + dp(5f), cx + s * 1.5f, cy - hh + dp(5f), linePaint)
            Handle.Bottom -> canvas.drawLine(cx - s * 1.5f, cy + hh - dp(5f), cx + s * 1.5f, cy + hh - dp(5f), linePaint)
            else -> {}
        }
    }

    private fun drawDone(canvas: Canvas) {
        val (cx, cy) = centre(Handle.Done)
        val r = dp(32f) * if (pressed == Handle.Done) 1.08f else 1f
        fillPaint.color = accent
        canvas.drawCircle(cx, cy, r, fillPaint)
        linePaint.color = onAccent
        linePaint.strokeWidth = dp(4f)
        linePaint.pathEffect = null
        path.reset()
        path.moveTo(cx - dp(11f), cy + dp(1f))
        path.lineTo(cx - dp(3.5f), cy + dp(9f))
        path.lineTo(cx + dp(12f), cy - dp(8f))
        canvas.drawPath(path, linePaint)
    }
}
