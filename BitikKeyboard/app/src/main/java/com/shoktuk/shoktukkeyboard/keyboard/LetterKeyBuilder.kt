package com.shoktuk.shoktukkeyboard.keyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.R

object LetterKeyBuilder {
    fun createLetterKey(
        service: InputMethodService, key: KeyEntry, layout: KeyboardLayout, buttonHeight: Int, margin: Int, isCaps: Boolean
    ): View {
        var style = KeyboardTheme.getLetterButtonStyle(service)

        if (key.upperCaseColor != null && isCaps) {
            if (key.upperCaseColor >= 0) {
                style = style.copy(
                    fillColor = KeyboardTheme.colorIndexes[key.upperCaseColor]
                )
            }
        } else if (key.lowerCaseColor != null && !isCaps) {
            if (key.lowerCaseColor >= 0) {
                style = style.copy(fillColor = KeyboardTheme.colorIndexes[key.lowerCaseColor])
            }
        } else {
            style = KeyboardTheme.getLetterButtonStyle(service)
        }

        val keyView = LayoutInflater.from(service).inflate(R.layout.keyboard_key, null, false)
        // Create the background from letterButtonStyle
        keyView.background = KeyboardTheme.createDrawableFromStyle(service, style)

        val mainTextView = keyView.findViewById<TextView>(R.id.mainText)
        val subTextView = keyView.findViewById<TextView>(R.id.subText)

        mainTextView.text = if (isCaps) key.uppercase else key.lowercase
        mainTextView.setTextColor(style.textColor.toColorInt())
        mainTextView.textSize = style.textSizeSp

        subTextView.text = if (isCaps) key.upperCaseHint ?: "" else key.lowerCaseHint ?: ""
        subTextView.setTextColor(Color.WHITE)
        subTextView.textSize = KeyboardTheme.getHintButtonTextSize(service) // or style.textSizeSp if you want subtext the same size

        keyView.setOnClickListener {
            val letter = if (isCaps) key.uppercase else key.lowercase
            val inputConnection = service.currentInputConnection
            if (inputConnection != null) {
                if (ensureRTLContext(service)) {
                    inputConnection.commitText("\u202B", 1)
                }
                inputConnection.commitText(letter, 1)
            }
        }

        keyView.layoutParams = LinearLayout.LayoutParams(KeyboardTheme.getLetterButtonWidth(service), buttonHeight).apply {
            marginStart = margin
            marginEnd = margin
        }
        return keyView
    }

    private fun ensureRTLContext(service: InputMethodService): Boolean {
        val inputConnection = service.currentInputConnection ?: return false
        val textBefore = inputConnection.getTextBeforeCursor(1, 0)
        return textBefore.isNullOrEmpty() || textBefore.last() == '\n'
    }
}
