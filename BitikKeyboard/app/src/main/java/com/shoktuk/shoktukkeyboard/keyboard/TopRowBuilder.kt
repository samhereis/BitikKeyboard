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

        // first button
        rowLayout.addView(
            createSystemAssetButton(
                service, null, layout.languageCode, buttonHeight, margin, onClick = { onLangChange() }, buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        // middle text – now with weight=1 so it stays between the buttons
        val textView = TextView(rowLayout.context).apply {
            // 1) set the string
            text = "Сиз расмий эмес, өзгөртүлгөн, жаңыланган битик колдонуудасыз!"

            // 2) enforce multi-line properly
            isSingleLine = false
            // (optionally) limit to 2 lines if you like:
            setLines(2)
            ellipsize = null

            // 3) explicit text color (pick something high-contrast)
            //    you can also pull from your theme or KeyboardTheme if you have a hintColor there
            setTextColor(ContextCompat.getColor(service, android.R.color.white))

            // 4) use the SP unit so older devices scale correctly
            val baseSp = KeyboardTheme.getHintButtonTextSize(service).value
            setTextSize(TypedValue.COMPLEX_UNIT_SP, baseSp * 1.5f)

            // 5) center the text
            gravity = Gravity.CENTER

            // 6) transparent background (optional)
            setBackgroundColor(Color.TRANSPARENT)

            // 7) layout with weight=1 so it expands between your buttons
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply {
                marginStart = margin
                marginEnd   = margin
            }

            // 8) make absolutely sure it’s visible
            visibility = View.VISIBLE
        }
        rowLayout.addView(textView)

        // last button
        rowLayout.addView(
            createSystemAssetButton(
                service, null, "123", buttonHeight, margin, onClick = { onModeChange("symbols") }, buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        return rowLayout
    }

    private fun createSystemAssetButton(
        service: InputMethodService, assetPath: String?, textToSet: String, buttonHeight: Int, margin: Int, onClick: () -> Unit, buttonStyle: ButtonStyle
    ): Button = Button(service).apply {
        text = textToSet
        gravity = Gravity.CENTER
        textSize = KeyboardTheme.getHintButtonTextSize(service).value * 1.25f
        setTextColor(buttonStyle.textColor.toColorInt())
        background = KeyboardTheme.createDrawableFromStyle(service, buttonStyle)
        layoutParams = LinearLayout.LayoutParams(
            KeyboardTheme.getSystemButtonWidth(service), buttonHeight
        ).apply {
            marginStart = 0
            marginEnd = 0
        }
        setOnClickListener { onClick() }
        setPadding(0,0,0,0)
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