package com.shoktuk.shoktukkeyboard.keyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.project.systems.JSTranscriber

object TopRowBuilder {
    var onTypedListener: (() -> Unit)? = null

    fun createTopRow(
        service: InputMethodService,
        layout: KeyboardLayout,
        buttonHeight: Int,
        margin: Int,
        onModeChange: (String) -> Unit,
        onLangChange: () -> Unit
    ): LinearLayout {
        val jsTranscriber = JSTranscriber(service)

        // Row container
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

        // Language button
        rowLayout.addView(
            createSystemAssetButton(
                service, null, layout.languageCode,
                buttonHeight, margin,
                onClick = { onLangChange() },
                buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        // Container for per-letter stacks; disable clipping here
        val lastWordContainer = createLastWordContainer(service, buttonHeight, margin).apply {
            clipChildren = false
            clipToPadding = false
        }
        rowLayout.addView(lastWordContainer)

        // Update on each keystroke
        onTypedListener = {
            updateLastWord(
                service,
                service.currentInputConnection,
                lastWordContainer,
                jsTranscriber
            )
        }

        // Symbols/mode button
        rowLayout.addView(
            createSystemAssetButton(
                service, null, "123",
                buttonHeight, margin,
                onClick = { onModeChange("symbols") },
                buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        // Initial render
        updateLastWord(service, service.currentInputConnection, lastWordContainer, jsTranscriber)
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

    /**
     * Rebuilds the container: for each character, either:
     *  • If the alt (top) text differs, stack top & base in a FrameLayout;
     *  • Otherwise, show a single full‑size TextView.
     */
    private fun updateLastWord(
        service: InputMethodService,
        inputConnection: InputConnection?,
        container: LinearLayout,
        transcriber: JSTranscriber
    ) {
        inputConnection?.getTextBeforeCursor(100, 0)?.let { textBefore ->
            val lastWord = textBefore.split("\\s+".toRegex()).lastOrNull() ?: ""
            val topText  = transcriber.getTranscription_Alternative(lastWord) ?: lastWord
            val baseText = transcriber.getTranscription(lastWord) ?: lastWord

            // Clear old views
            container.removeAllViews()

            // Define sizes in sp
            val FULL_SP = 40f
            val HALF_SP = 22f

            for (i in baseText.indices) {
                if (i < topText.length && topText[i] != baseText[i]) {
                    // Stack differing letters
                    val stack = FrameLayout(container.context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        clipChildren = false
                        clipToPadding = false
                        setPadding(0,38,0,0)
                    }

                    // Base letter (bottom)
                    val baseLetter = TextView(container.context).apply {
                        text = baseText[i].toString()
                        textSize = HALF_SP
                        gravity = Gravity.CENTER
                        setBackgroundColor(Color.TRANSPARENT)
                        setPadding(0,0,0,8)
                    }
                    stack.addView(baseLetter)

                    // Top letter (superscript)
                    val topLetter = TextView(container.context).apply {
                        text = topText[i].toString()
                        textSize = HALF_SP
                        gravity = Gravity.CENTER
                        setBackgroundColor(Color.TRANSPARENT)
                        translationY = -this.textSize
                        setPadding(0,8,0,0)
                    }
                    stack.addView(topLetter)

                    container.addView(stack)
                } else {
                    // Matching letter: full size
                    val letter = TextView(container.context).apply {
                        text = baseText[i].toString()
                        textSize = FULL_SP
                        gravity = Gravity.CENTER
                        setBackgroundColor(Color.TRANSPARENT)
                    }
                    container.addView(letter)
                }
            }
        }
    }
}