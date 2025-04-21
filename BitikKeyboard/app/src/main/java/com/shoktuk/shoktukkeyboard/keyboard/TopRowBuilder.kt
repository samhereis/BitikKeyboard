package com.shoktuk.shoktukkeyboard.keyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt

object TopRowBuilder {
    fun createTopRow(
        service: InputMethodService,
        layout: KeyboardLayout,
        buttonHeight: Int,
        margin: Int,
        onModeChange: (String) -> Unit,
        onLangChange: () -> Unit
    ): LinearLayout {
        val rowLayout = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = margin
                bottomMargin = margin
            }
        }

        // first button
        rowLayout.addView(
            createSystemAssetButton(
                service,
                null,
                layout.languageCode,
                buttonHeight,
                margin,
                onClick = { onLangChange() },
                buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        // middle text – now with weight=1 so it stays between the buttons
        val textView = TextView(rowLayout.context).apply {
            text = "Сиз расмий эмес, өзгөртүлгөн, жаңыланган битик колдонуудасыз!"
            textSize = 20f
            setSingleLine(false)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = margin
                marginEnd = margin
            }
        }
        rowLayout.addView(textView)

        // last button
        rowLayout.addView(
            createSystemAssetButton(
                service,
                null,
                "123",
                buttonHeight,
                margin,
                onClick = { onModeChange("symbols") },
                buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        return rowLayout
    }

    private fun createSystemAssetButton(
        service: InputMethodService,
        assetPath: String?,
        textToSet: String,
        buttonHeight: Int,
        margin: Int,
        onClick: () -> Unit,
        buttonStyle: ButtonStyle
    ): Button = Button(service).apply {
        text = textToSet
        gravity = Gravity.CENTER
        textSize = buttonStyle.textSizeSp
        setTextColor(buttonStyle.textColor.toColorInt())
        background = KeyboardTheme.createDrawableFromStyle(service, buttonStyle)
        layoutParams = LinearLayout.LayoutParams(
            KeyboardTheme.getSystemButtonWidth(service),
            buttonHeight
        ).apply {
            marginStart = margin
            marginEnd = margin
        }
        setOnClickListener { onClick() }
    }

    private fun createLastWordContainer(
        service: InputMethodService,
        buttonHeight: Int,
        margin: Int
    ): LinearLayout = LinearLayout(service).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f).apply {
            marginStart = margin
            marginEnd = margin
        }
    }
}