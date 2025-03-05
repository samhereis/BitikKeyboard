package com.shoktuk.bitikkeyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

object LetterKeyBuilder {
    fun createLetterKey(
        service: InputMethodService,
        key: KeyEntry,
        layout: KeyboardLayout,
        buttonHeight: Int,
        margin: Int,
        isCaps: Boolean,
        onCapsChange: (Boolean) -> Unit
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
        mainTextView.setTextColor(Color.parseColor(style.textColor))
        mainTextView.textSize = style.textSizeSp

        subTextView.text = if (isCaps) key.upperCaseHint ?: "" else key.lowerCaseHint ?: ""
        subTextView.setTextColor(Color.WHITE)
        subTextView.textSize = KeyboardTheme.getHintButtonTextSize(service) // or style.textSizeSp if you want subtext the same size

        keyView.setOnClickListener {
            val letter = if (isCaps) key.uppercase else key.lowercase
            val textToCommit = if (layout.directionality == 1) letter else letter
            service.currentInputConnection?.commitText(textToCommit, 1)
        }

        keyView.layoutParams = LinearLayout.LayoutParams(KeyboardTheme.getLetterButtonWidth(service), buttonHeight).apply {
            marginStart = margin
            marginEnd = margin
        }
        return keyView
    }
}
