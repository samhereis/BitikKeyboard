package com.shoktuk.bitikkeyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

object LetterKeyBuilder {

    val colorIndexes: List<String> = listOf("#004cb5", "#777500")

    fun createLetterKey(
        service: InputMethodService,
        key: KeyEntry,
        layout: KeyboardLayout,
        buttonHeight: Int,
        margin: Int,
        isCaps: Boolean,
        onCapsChange: (Boolean) -> Unit
    ): View {
        // Letter keys are narrower
        val letterKeyWidthPx = KeyboardTheme.dpToPx(service, KeyboardTheme.LETTER_BUTTON_WIDTH_DP)
        var style = KeyboardTheme.letterButtonStyle

        if (key.upperCaseColor != null && isCaps) {
            if (key.upperCaseColor >= 0) {
                style = style.copy(
                    fillColor = colorIndexes[key.upperCaseColor]
                )
            }
        } else if (key.lowerCaseColor != null && !isCaps) {
            if (key.lowerCaseColor >= 0) {
                style = style.copy(fillColor = colorIndexes[key.lowerCaseColor])
            }
        } else {
            style = KeyboardTheme.letterButtonStyle
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
        subTextView.textSize = 12f // or style.textSizeSp if you want subtext the same size

        keyView.setOnClickListener {
            val letter = if (isCaps) key.uppercase else key.lowercase
            val textToCommit = if (layout.directionality == 1) "\u202B$letter\u202C" else letter
            service.currentInputConnection?.commitText(textToCommit, 1)
            if (isCaps) onCapsChange(false)
        }

        keyView.layoutParams = LinearLayout.LayoutParams(letterKeyWidthPx, buttonHeight).apply {
            marginStart = margin
            marginEnd = margin
        }
        return keyView
    }
}
