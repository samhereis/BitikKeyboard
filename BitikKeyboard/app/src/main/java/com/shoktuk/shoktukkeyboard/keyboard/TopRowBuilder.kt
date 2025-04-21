package com.shoktuk.shoktukkeyboard.keyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.project.systems.JSTranscriber

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

        rowLayout.addView(
            createSystemAssetButton(
                service, null, layout.languageCode,
                buttonHeight, margin,
                onClick = { onLangChange() },
                buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        val lastWordContainer = createLastWordContainer(service, buttonHeight, margin).apply {
            clipChildren = false
            clipToPadding = false
        }
        rowLayout.addView(lastWordContainer)
        val stack = FrameLayout(lastWordContainer.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            clipChildren = false
            clipToPadding = false
            setPadding(0,38,0,0)
        }

        // Base letter (bottom)
        val baseLetter = TextView(lastWordContainer.context).apply {
            text = "Сиз расмий эмес, өзгөртүлгөн, жаңыланган битик колдонуудасыз!"
            textSize = 25f
            gravity = Gravity.CENTER
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(0,0,0,8)
        }
        stack.addView(baseLetter)

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

    /** Horizontal container for per-letter FrameLayouts */
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