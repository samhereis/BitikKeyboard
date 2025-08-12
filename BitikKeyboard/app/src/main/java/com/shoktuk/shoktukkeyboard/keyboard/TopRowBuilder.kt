package com.shoktuk.shoktukkeyboard.keyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt

object TopRowBuilder {
    fun createTopRow(
        service: InputMethodService, layout: KeyboardLayout, buttonHeight: Int, margin: Int, onModeChange: (String) -> Unit, onAlphabetChange: () -> Unit
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

        var alphabetLabel = "𐰌"
        if (MyKeyboardService.currentAlphabet == "latin") {
            alphabetLabel = "A"
        }

        rowLayout.addView(
            createSystemAssetButton(
                service, alphabetLabel, buttonHeight, onClick = { onAlphabetChange() }, buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        val textView = TextView(rowLayout.context).apply {
            text = "Расмий эмес, жаңыланган битик колдонуудасыз!"

            isSingleLine = false
            setLines(2)
            ellipsize = null

            setTextColor(ContextCompat.getColor(service, android.R.color.white))

            val baseSp = KeyboardTheme.getHintButtonTextSize(service).value
            setTextSize(TypedValue.COMPLEX_UNIT_SP, baseSp * 1.35f)

            gravity = Gravity.CENTER

            setBackgroundColor(Color.TRANSPARENT)

            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply {
                marginStart = margin
                marginEnd = margin
            }

            visibility = View.VISIBLE
        }
        rowLayout.addView(textView)

        rowLayout.addView(
            createSystemAssetButton(
                service, "123", buttonHeight, onClick = { onModeChange("symbols") }, buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        return rowLayout
    }

    private fun createSystemAssetButton(
        service: InputMethodService, textToSet: String, buttonHeight: Int, onClick: () -> Unit, buttonStyle: ButtonStyle
    ): Button = Button(service).apply {
        text = textToSet
        gravity = Gravity.CENTER
        textSize = KeyboardTheme.getHintButtonTextSize(service).value * 1.5f
        setTextColor(buttonStyle.textColor.toColorInt())
        background = KeyboardTheme.createDrawableFromStyle(service, buttonStyle)
        layoutParams = LinearLayout.LayoutParams(
            KeyboardTheme.getSystemButtonWidth(service), buttonHeight
        ).apply {
            marginStart = 0
            marginEnd = 0
        }
        setOnClickListener { onClick() }
        setPadding(0, 0, 0, 0)
    }

    private fun createLastWordContainer(
        service: InputMethodService, buttonHeight: Int, margin: Int
    ): LinearLayout = LinearLayout(service).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f).apply {
            marginStart = margin
            marginEnd = margin
        }
    }
}