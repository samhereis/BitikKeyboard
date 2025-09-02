package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

object TopRowBuilder {
    fun createTopRow(
        service: InputMethodService, layout: KeyboardLayout, buttonHeight: Int, onAlphabetChange: () -> Unit
    ): LinearLayout {
        val rowLayout = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = MyKeyboardService.buttonMargin
                bottomMargin = MyKeyboardService.buttonMargin
            }
        }

        var alphabetLabel = "A"
        if (MyKeyboardService.currentAlphabet == "latin") {
            alphabetLabel = "𐰌"
        }

        rowLayout.addView(
            SystemKeyBuilder.systemButton_Text(
                service, alphabetLabel, buttonHeight, onClick = { onAlphabetChange() }
            )
        )

        var topLabel = "Расмий эмес, жаңыланган битик колдонуудасыз!"
        if (MyKeyboardService.currentAlphabet == "latin") {
            topLabel = "𐱅𐰭𐰼𐰃 𐰅𐰠𐰢𐰚𐰁 𐰌𐰝𐰢𐰓𐰢"
        }

        val textView = TextView(rowLayout.context).apply {
            text = topLabel

            isSingleLine = false
            setLines(2)
            ellipsize = null

            setTextColor(KeyboardTheme.getColor(2).toColorInt())

            val baseSp = KeyboardTheme.getHintButtonTextSize(service).value
            setTextSize(TypedValue.COMPLEX_UNIT_SP, baseSp * 1.35f)

            gravity = Gravity.CENTER

            setBackgroundColor(Color.TRANSPARENT)

            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply {
                marginStart = MyKeyboardService.buttonMargin
                marginEnd = MyKeyboardService.buttonMargin
            }

            visibility = View.VISIBLE
        }

        rowLayout.addView(textView)

        rowLayout.addView(
            SystemKeyBuilder.systemButton_Icon(
                service, KeyboardTheme.LANGUAGE_ICON_FILE, buttonHeight,
                onClick = {
                    val imm = service.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showInputMethodPicker()
                    TopRowBuilder_Old.onTypedListener?.invoke()
                }
            )
        )

        return rowLayout
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