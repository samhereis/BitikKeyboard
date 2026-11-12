package com.shoktuk.shoktukkeyboard.suggestions

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

/** The building blocks every script's suggestion bar shares: a scrolling strip and the card that sits in it. */
object SuggestionViews {
    /** A horizontal strip whose cards fade out at both ends. Add cards to [Strip.row]. */
    class Strip(val scroll: HorizontalScrollView, val row: LinearLayout)

    fun strip(context: Context): Strip {
        val density = context.resources.displayMetrics.density
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        val scroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            isHorizontalFadingEdgeEnabled = true
            setFadingEdgeLength((20 * density).toInt())
            addView(row, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        return Strip(scroll, row)
    }

    /** One line of text in the keyboard's text colour, in the Bitik font when [bitik] is set. */
    fun text(context: Context, text: String, sp: Float, bitik: Boolean = false, alpha: Float = 1f) = TextView(context).apply {
        this.text = text
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        setTextColor(KeyboardTheme.getColor(2).toColorInt())
        this.alpha = alpha
        gravity = Gravity.CENTER
        maxLines = 1
        includeFontPadding = false
        if (bitik) { typeface = KeyboardTheme.bitikTypeface(context) ?: Typeface.DEFAULT_BOLD; paint.isFakeBoldText = true }
    }

    /** A suggestion: [main] with an optional small [transcription] under it. */
    fun card(context: Context, height: Int, main: View, transcription: String?, onClick: () -> Unit): LinearLayout {
        val density = context.resources.displayMetrics.density
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding((12 * density).toInt(), 0, (12 * density).toInt(), 0)
            addView(main)
            if (!transcription.isNullOrEmpty()) addView(text(context, transcription, 9f, alpha = 0.6f))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height).apply {
                gravity = Gravity.CENTER_VERTICAL
                marginEnd = (4 * density).toInt()
            }
            setOnClickListener { onClick() }
        }
    }

    /** A card holding a single word. */
    fun wordCard(context: Context, height: Int, word: String, sp: Float, bitik: Boolean = false, transcription: String? = null, onClick: () -> Unit) =
        card(context, height, text(context, word, sp, bitik), transcription, onClick)
}
