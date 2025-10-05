package com.shoktuk.shoktukkeyboard.keyboard

import android.animation.ValueAnimator
import android.graphics.BlendMode
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.SweepGradient
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.Choreographer
import androidx.annotation.ColorInt
import androidx.core.animation.addListener

class NewGlowDrawable : Drawable(), Choreographer.FrameCallback {
    var cornerRadius = 25f
    var innerGlowWidth = 5f
    var innerGlowBlur = 250
    var innerGlowOpacity = 0.95f
    var outerGlowWidth = 0f
    var outerGlowBlur = 0f
    var insetForBlur = 0f
    var degreesPerSecond = 20f

    @ColorInt
    var colors: IntArray = intArrayOf(
        Color.RED, 0xFFFF7F00.toInt(), Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, 0xFF800080.toInt(), 0xFFFF1493.toInt(), Color.RED
    )

    var rotationDeg: Float = 0f
        set(value) {
            field = (value % 360f + 360f) % 360f; invalidateSelf()
        }

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val keepInsidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2f }

    private var sweep: SweepGradient? = null
    private val matrix = Matrix()
    private val rect = RectF()

    private var frameRunning = false
    private var activeRequested = false
    private var lastFrameNs = 0L
    private val choreographer = Choreographer.getInstance()

    private var outerMask1: BlurMaskFilter? = null
    private var outerMask2: BlurMaskFilter? = null
    private var innerMask1: BlurMaskFilter? = null
    private var innerMask2: BlurMaskFilter? = null

    private var baseAlpha = 255
    private var fade = 1f
    private var fadeAnim: ValueAnimator? = null

    init {
        if (Build.VERSION.SDK_INT >= 29) {
            outerPaint.blendMode = BlendMode.PLUS
            innerPaint.blendMode = BlendMode.PLUS
        } else {
            @Suppress("DEPRECATION") outerPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
            @Suppress("DEPRECATION") innerPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
        }
        refreshFilters()
    }

    fun refreshFilters() {
        outerMask1 = if (outerGlowBlur > 0f) BlurMaskFilter(outerGlowBlur, BlurMaskFilter.Blur.NORMAL) else null
        outerMask2 = if (outerGlowBlur > 0f) BlurMaskFilter(outerGlowBlur * 1.8f, BlurMaskFilter.Blur.NORMAL) else null
        innerMask1 = if (innerGlowBlur > 0f) BlurMaskFilter(innerGlowBlur * 1.4f, BlurMaskFilter.Blur.NORMAL) else null
        innerMask2 = if (innerGlowBlur > 0f) BlurMaskFilter(innerGlowBlur * 2.6f, BlurMaskFilter.Blur.NORMAL) else null
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        sweep = SweepGradient(bounds.exactCenterX(), bounds.exactCenterY(), colors, null)
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        val cx = b.exactCenterX()
        val cy = b.exactCenterY()
        sweep?.let {
            matrix.reset()
            matrix.postRotate(rotationDeg, cx, cy)
            it.setLocalMatrix(matrix)
        }

        val left = b.left + insetForBlur
        val top = b.top + insetForBlur
        val right = b.right - insetForBlur
        val bottom = b.bottom - insetForBlur

        val a = (baseAlpha * fade).toInt().coerceIn(0, 255)

        rect.set(left + outerGlowWidth / 2f, top + outerGlowWidth / 2f, right - outerGlowWidth / 2f, bottom - outerGlowWidth / 2f)
        outerPaint.shader = sweep
        outerPaint.strokeWidth = outerGlowWidth
        outerPaint.alpha = (a * 0.55f).toInt()
        outerPaint.maskFilter = outerMask1
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, outerPaint)

        outerPaint.alpha = (a * 0.28f).toInt()
        outerPaint.strokeWidth = outerGlowWidth + 6f
        outerPaint.maskFilter = outerMask2
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, outerPaint)

        val save = canvas.saveLayer(null, null)

        rect.set(left + innerGlowWidth / 2f, top + innerGlowWidth / 2f, right - innerGlowWidth / 2f, bottom - innerGlowWidth / 2f)
        innerPaint.shader = sweep
        innerPaint.strokeWidth = innerGlowWidth
        innerPaint.alpha = (a * innerGlowOpacity).toInt().coerceIn(0, 255)
        innerPaint.maskFilter = innerMask1
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, innerPaint)

        innerPaint.alpha = (a * (innerGlowOpacity * 0.45f)).toInt().coerceIn(0, 255)
        innerPaint.strokeWidth = innerGlowWidth + 6f
        innerPaint.maskFilter = innerMask2
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, innerPaint)

        rect.set(left + 1f, top + 1f, right - 1f, bottom - 1f)
        if (Build.VERSION.SDK_INT >= 29) {
            keepInsidePaint.blendMode = BlendMode.DST_IN
        } else {
            @Suppress("DEPRECATION") keepInsidePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        keepInsidePaint.color = Color.WHITE
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, keepInsidePaint)
        if (Build.VERSION.SDK_INT >= 29) keepInsidePaint.blendMode = null
        else @Suppress("DEPRECATION") {
            keepInsidePaint.xfermode = null
        }

        canvas.restoreToCount(save)

        borderPaint.shader = sweep
        borderPaint.alpha = a
        borderPaint.strokeWidth = 2f
        rect.set(left + 1f, top + 1f, right - 1f, bottom - 1f)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
    }

    override fun setAlpha(alpha: Int) {
        baseAlpha = alpha.coerceIn(0, 255)
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        outerPaint.colorFilter = colorFilter
        innerPaint.colorFilter = colorFilter
        keepInsidePaint.colorFilter = colorFilter
        borderPaint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    fun startRotating() {
        activeRequested = true
        if (!frameRunning) startFrameLoop()
    }

    fun stopRotating() {
        activeRequested = false
        frameRunning = false
    }

    private fun startFrameLoop() {
        frameRunning = true
        lastFrameNs = System.nanoTime()
        choreographer.postFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!frameRunning) return
        val now = System.nanoTime()
        val dt = ((now - lastFrameNs).coerceAtLeast(0L)) / 1_000_000_000f
        lastFrameNs = now
        rotationDeg = (rotationDeg + degreesPerSecond * dt) % 360f
        invalidateSelf()
        choreographer.postFrameCallback(this)
    }

    fun setVisibleAlpha(target: Float) {
        fadeAnim?.cancel()
        fade = target.coerceIn(0f, 1f)
        invalidateSelf()
    }

    fun animateAlpha(to: Float, duration: Long = 400L, onEnd: (() -> Unit)? = null) {
        val s = fade
        val e = to.coerceIn(0f, 1f)
        if (s == e) {
            onEnd?.invoke()
            return
        }
        fadeAnim?.cancel()
        fadeAnim = ValueAnimator.ofFloat(s, e).apply {
            this.duration = duration
            addUpdateListener {
                fade = it.animatedValue as Float
                invalidateSelf()
            }
            addListener(onEnd = {
                fadeAnim = null
                onEnd?.invoke()
            })
            start()
        }
    }
}

fun dpF(density: Float, v: Float): Float = v * density