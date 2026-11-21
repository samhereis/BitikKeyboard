package com.shoktuk.shoktukkeyboard.keyboard

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

/**
 * The one builder for every button of the top row (language switch, ★, emoji, resize, settings, keyboard picker):
 * the same size, the same tint and the same optical centring whether the glyph is text or an icon, and no key background.
 */
object TopRowButtons {
    private fun container(service: InputMethodService, buttonHeight: Int, onClick: (() -> Unit)?) = FrameLayout(service).apply {
        isClickable = true
        isFocusable = true
        layoutParams = LinearLayout.LayoutParams(KeyboardTheme.getSystemButtonWidth(service), buttonHeight)
        setOnClickListener { onClick?.invoke() }
    }

    private fun glyphSize(buttonHeight: Int) = (buttonHeight / 2.5f)
    private fun tint(service: InputMethodService) = KeyboardTheme.getSystemButtonStyle(service).textColor.toColorInt()

    /** A text or emoji glyph, centred on its ink (not on its font metrics), so letters and emoji sit on the same line as the icons. */
    fun text(service: InputMethodService, text: String, buttonHeight: Int, onClick: (() -> Unit)? = null, applyBitikFont: Boolean = false): View =
        container(service, buttonHeight, onClick).apply {
            addView(GlyphView(service, text, glyphSize(buttonHeight), tint(service), KeyboardTheme.typefaceFor(service, text, Typeface.DEFAULT) ?: Typeface.DEFAULT),
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        }

    fun drawable(service: InputMethodService, @DrawableRes drawableRes: Int, buttonHeight: Int, onClick: (() -> Unit)? = null): View =
        icon(service, ContextCompat.getDrawable(service, drawableRes), buttonHeight, onClick)

    fun asset(service: InputMethodService, assetPath: String, buttonHeight: Int, onClick: (() -> Unit)? = null): View =
        icon(service, KeyboardTheme.loadAssetDrawable(service, assetPath), buttonHeight, onClick)

    private fun icon(service: InputMethodService, drawable: android.graphics.drawable.Drawable?, buttonHeight: Int, onClick: (() -> Unit)?): View =
        container(service, buttonHeight, onClick).apply {
            drawable?.let { d ->
                val wrapped = DrawableCompat.wrap(d).mutate()
                DrawableCompat.setTint(wrapped, tint(service))
                val size = glyphSize(buttonHeight).toInt()
                addView(ImageView(service).apply { setImageDrawable(wrapped) },
                    FrameLayout.LayoutParams(size, size, android.view.Gravity.CENTER))
            }
        }

    class GlyphView(context: android.content.Context, private val label: String, sizePx: Float, color: Int, face: Typeface) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = sizePx; this.color = color; typeface = face }
        private val bounds = Rect()
        init { paint.getTextBounds(label, 0, label.length, bounds) }

        override fun onDraw(canvas: Canvas) {
            canvas.drawText(label, width / 2f - bounds.exactCenterX(), height / 2f - bounds.exactCenterY(), paint)
        }
    }
}
