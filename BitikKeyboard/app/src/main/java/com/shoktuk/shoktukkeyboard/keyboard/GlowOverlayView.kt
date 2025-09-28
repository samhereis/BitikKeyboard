import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.SweepGradient
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat

class GlowOverlayView(context: Context) : View(context) {

    var cornerRadius = 32f
    var innerGlowWidth = 6f
    var innerGlowBlur = 18f
    var innerGlowOpacity = 0.92f
    var outerGlowWidth = 5f
    var outerGlowBlur = 16f
    var insetForBlur = 24f
    var degreesPerSecond = 45f

    @ColorInt
    var colors: IntArray = intArrayOf(
        Color.RED, 0xFFFF7F00.toInt(), Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, 0xFF800080.toInt(), 0xFFFF1493.toInt(), Color.RED
    )

    var rotationDeg: Float = 0f
        set(value) { field = value % 360f; invalidate() }

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

    private var outerMask1: BlurMaskFilter? = null
    private var outerMask2: BlurMaskFilter? = null
    private var innerMask1: BlurMaskFilter? = null
    private var innerMask2: BlurMaskFilter? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        if (Build.VERSION.SDK_INT >= 29) {
            outerPaint.blendMode = BlendMode.PLUS
            innerPaint.blendMode = BlendMode.PLUS
        } else {
            @Suppress("DEPRECATION") outerPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
            @Suppress("DEPRECATION") innerPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
        }
        refreshFilters()
    }

    fun startRotating(durationMs: Long = 0L) {
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
        ViewCompat.postOnAnimation(this) { tickFrame() }
    }

    private fun tickFrame() {
        if (!frameRunning) return
        val now = System.nanoTime()
        val dt = (now - lastFrameNs).coerceAtLeast(0L) / 1_000_000_000f
        lastFrameNs = now
        rotationDeg = (rotationDeg + degreesPerSecond * dt) % 360f
        ViewCompat.postInvalidateOnAnimation(this)
        ViewCompat.postOnAnimation(this) { tickFrame() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (activeRequested && !frameRunning) startFrameLoop()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        frameRunning = false
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) {
            if (activeRequested && !frameRunning) startFrameLoop()
        } else {
            frameRunning = false
        }
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) {
            if (activeRequested && !frameRunning) startFrameLoop()
        } else {
            frameRunning = false
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sweep = SweepGradient(w / 2f, h / 2f, colors, null)
    }

    fun refreshFilters() {
        outerMask1 = if (outerGlowBlur > 0f) BlurMaskFilter(outerGlowBlur, BlurMaskFilter.Blur.NORMAL) else null
        outerMask2 = if (outerGlowBlur > 0f) BlurMaskFilter(outerGlowBlur * 1.8f, BlurMaskFilter.Blur.NORMAL) else null
        innerMask1 = if (innerGlowBlur > 0f) BlurMaskFilter(innerGlowBlur * 1.4f, BlurMaskFilter.Blur.NORMAL) else null
        innerMask2 = if (innerGlowBlur > 0f) BlurMaskFilter(innerGlowBlur * 2.6f, BlurMaskFilter.Blur.NORMAL) else null
    }

    override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) return
        val cx = width / 2f
        val cy = height / 2f
        sweep?.let {
            matrix.reset()
            matrix.postRotate(rotationDeg, cx, cy)
            it.setLocalMatrix(matrix)
        }
        val left = insetForBlur
        val top = insetForBlur
        val right = width - insetForBlur
        val bottom = height - insetForBlur

        rect.set(left + outerGlowWidth / 2f, top + outerGlowWidth / 2f, right - outerGlowWidth / 2f, bottom - outerGlowWidth / 2f)
        outerPaint.shader = sweep
        outerPaint.strokeWidth = outerGlowWidth
        outerPaint.alpha = (255 * 0.55f).toInt()
        outerPaint.maskFilter = outerMask1
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, outerPaint)

        outerPaint.alpha = (255 * 0.28f).toInt()
        outerPaint.strokeWidth = outerGlowWidth + 6f
        outerPaint.maskFilter = outerMask2
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, outerPaint)

        val save = canvas.saveLayer(null, null)

        rect.set(left + innerGlowWidth / 2f, top + innerGlowWidth / 2f, right - innerGlowWidth / 2f, bottom - innerGlowWidth / 2f)
        innerPaint.shader = sweep
        innerPaint.strokeWidth = innerGlowWidth
        innerPaint.alpha = (255 * innerGlowOpacity).toInt().coerceIn(0, 255)
        innerPaint.maskFilter = innerMask1
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, innerPaint)

        innerPaint.alpha = (255 * (innerGlowOpacity * 0.45f)).toInt().coerceIn(0, 255)
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
        else @Suppress("DEPRECATION") { keepInsidePaint.xfermode = null }

        canvas.restoreToCount(save)

        borderPaint.shader = sweep
        borderPaint.strokeWidth = 2f
        rect.set(left + 1f, top + 1f, right - 1f, bottom - 1f)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
    }
}