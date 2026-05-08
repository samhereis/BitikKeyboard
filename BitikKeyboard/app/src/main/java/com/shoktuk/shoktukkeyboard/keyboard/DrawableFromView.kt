package com.shoktuk.shoktukkeyboard.keyboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.Choreographer
import android.view.View

fun viewToDrawable(view: View): Drawable = LiveViewDrawable(view)

private class LiveViewDrawable(private val view: View) : Drawable(), Choreographer.FrameCallback {
    private var bitmap: Bitmap? = null
    private var canvasBitmap: Canvas? = null
    private var running = false
    private val choreographer = Choreographer.getInstance()
    private var lastW = -1
    private var lastH = -1

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        ensureBitmap(bounds.width(), bounds.height())
        if (!running) {
            running = true
            choreographer.postFrameCallback(this)
        }
    }

    override fun draw(canvas: Canvas) {
        val bw = bounds.width().coerceAtLeast(1)
        val bh = bounds.height().coerceAtLeast(1)
        if (bw != lastW || bh != lastH) ensureBitmap(bw, bh)

        if (bitmap != null && canvasBitmap != null) {
            layoutViewForBounds(bw, bh)
            bitmap!!.eraseColor(Color.TRANSPARENT)
            view.draw(canvasBitmap!!)
            canvas.drawBitmap(bitmap!!, bounds.left.toFloat(), bounds.top.toFloat(), null)
        }
    }

    override fun doFrame(frameTimeNanos: Long) {
        invalidateSelf()
        if (running) choreographer.postFrameCallback(this)
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    private fun ensureBitmap(w: Int, h: Int) {
        if (w <= 0 || h <= 0) return
        if (w == lastW && h == lastH && bitmap != null) return
        bitmap?.recycle()
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvasBitmap = Canvas(bitmap!!)
        lastW = w
        lastH = h
    }

    private fun layoutViewForBounds(w: Int, h: Int) {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)
        view.measure(widthSpec, heightSpec)
        view.layout(0, 0, w, h)
    }
}