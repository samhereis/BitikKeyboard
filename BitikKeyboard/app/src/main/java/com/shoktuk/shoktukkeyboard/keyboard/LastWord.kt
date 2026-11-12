package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.inputmethod.InputConnection
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.inputText_LastWord
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.text_Original
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

/** The word before the cursor, shared by the top-row builders and the suggestion strip. */
object LastWord {
    private const val EXTRA_SEPARATORS = "·.,⸮⹁:;!?()[]{}\"'"
    private val regex = "[^\\p{L}${Regex.escape(EXTRA_SEPARATORS)}]+".toRegex()

    /** Reads the text before the cursor into [text_Original] and [inputText_LastWord]; returns the raw last word. */
    fun extract(ic: InputConnection?): String {
        text_Original = ic?.getTextBeforeCursor(100, 0)?.toString().orEmpty()
        inputText_LastWord = text_Original.split(regex).lastOrNull().orEmpty()
        return inputText_LastWord
    }

    /** A centred Bitik-font label in the system-button colour. */
    fun bitikLabel(context: Context, label: String, sizeSp: Float): TextView = TextView(context).apply {
        text = label
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        setTextColor(KeyboardTheme.getColor(2).toColorInt())
        typeface = KeyboardTheme.bitikTypeface(context)
        gravity = Gravity.CENTER
    }
}
