package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt

object TopRowBuilder {
    var onTypedListener: (() -> Unit)? = null

    fun createTopRow(
        service: InputMethodService, layout: KeyboardLayout, buttonHeight: Int, margin: Int, onModeChange: (String) -> Unit, onLangChange: () -> Unit
    ): LinearLayout {
        val rowLayout = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = margin
                bottomMargin = margin
            }
        }

        rowLayout.addView(
            createSystemAssetButton(
                service = service,
                assetPath = null,
                textToSet = layout.languageCode,
                buttonHeight = buttonHeight,
                onClick = { onLangChange() },
                margin = margin,
                buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        val lastWordTextView = createLastWordTextView(service, buttonHeight, margin)
        rowLayout.addView(lastWordTextView)

        onTypedListener = { updateLastWord(service.currentInputConnection, lastWordTextView) }

        rowLayout.addView(
            createSystemAssetButton(
                service = service,
                assetPath = null,
                textToSet = "123",
                buttonHeight = buttonHeight,
                margin = margin,
                onClick = { onModeChange("symbols") },
                buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        return rowLayout
    }

    private fun createSystemAssetButton(
        service: InputMethodService, assetPath: String?, textToSet: String, buttonHeight: Int, margin: Int, onClick: () -> Unit, buttonStyle: ButtonStyle
    ): Button {
        return Button(service).apply {
            text = textToSet
            gravity = Gravity.CENTER
            textSize = buttonStyle.textSizeSp
            setTextColor(buttonStyle.textColor.toColorInt())
            background = KeyboardTheme.createDrawableFromStyle(service, buttonStyle)
            layoutParams = LinearLayout.LayoutParams(
                KeyboardTheme.getSystemButtonWidth(service), buttonHeight
            ).apply {
                marginStart = margin
                marginEnd = margin
            }
            setOnClickListener { onClick() }
        }
    }

    private fun createLastWordTextView(
        service: InputMethodService, buttonHeight: Int, margin: Int
    ): TextView {
        return TextView(service).apply {
            gravity = Gravity.CENTER
            textSize = KeyboardTheme.getSystemButtonStyle(service).textSizeSp
            setTextColor(KeyboardTheme.getSystemButtonStyle(service).textColor.toColorInt())
            background = KeyboardTheme.createDrawableFromStyle(
                service, KeyboardTheme.getLetterButtonStyle(service)
            )
            layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f).apply {
                marginStart = margin
                marginEnd = margin
            }
        }
    }

    private fun updateLastWord(inputConnection: InputConnection?, textView: TextView) {
        inputConnection?.getTextBeforeCursor(100, 0)?.let { textBeforeCursor ->
            // Split by space, colon, period, and comma
            val lastWord = textBeforeCursor.split("[\\s:.,]+".toRegex()).lastOrNull() ?: ""
            textView.text = lastWord
        }
    }
}
