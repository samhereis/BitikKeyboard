package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.project.systems.JSTranscriber

object TopRowBuilder_Old {
    var onTypedListener: (() -> Unit)? = null

    fun createTopRow(
        service: InputMethodService, layout: KeyboardLayout, buttonHeight: Int, margin: Int, onModeChange: (String) -> Unit, onLangChange: () -> Unit
    ): LinearLayout {
        val jsTranscriber = JSTranscriber(service)

        // 1) Row container
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

        // 2) Left: language switch button
        rowLayout.addView(
            createSystemAssetButton(
                service = service,
                assetPath = null,
                textToSet = layout.languageCode,
                buttonHeight = buttonHeight,
                margin = margin,
                onClick = onLangChange,
                buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        // 3) Center: dynamic letter-stack container
        val lastWordContainer = createLastWordContainer(service, buttonHeight, margin).apply {
            clipChildren = false
            clipToPadding = false
        }
        rowLayout.addView(lastWordContainer)

        // 4) Hook up the refresh callback (fires on each keystroke/selection)
        onTypedListener = {
            // post to ensure we're on the UI thread, then redraw
            lastWordContainer.post {
                updateLastWord(
                    service, service.currentInputConnection, lastWordContainer, jsTranscriber, KeyboardTheme.getSystemButtonStyle(service)
                )
                lastWordContainer.requestLayout()
                lastWordContainer.invalidate()
            }
        }

        // 5) Right: symbols/mode toggle button
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
        setPadding(0, 0, 0, 0)
    }

    private fun createLastWordContainer(
        service: InputMethodService, buttonHeight: Int, margin: Int
    ): LinearLayout = LinearLayout(service).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            0, buttonHeight, 1f
        ).apply {
            marginStart = margin
            marginEnd = margin
        }
    }

    private fun updateLastWord(
        service: InputMethodService,
        inputConnection: InputConnection?,
        container: LinearLayout,
        transcriber: JSTranscriber,
        buttonStyle: ButtonStyle
    ) {
        container.removeAllViews()

        val before = inputConnection?.getTextBeforeCursor(100, 0)?.toString().orEmpty()
        if (before.isEmpty()) return

        val lastWord = before.split("[^\\p{L}\\p{N}]+".toRegex()).lastOrNull().orEmpty()
        val topText = transcriber.getTranscription_Alternative(lastWord)
            .orEmpty().ifEmpty { lastWord }
        val baseText = transcriber.getTranscription(lastWord)
            .orEmpty().ifEmpty { lastWord }

        val fullSp = buttonStyle.textSizeSp.value
        val halfSp = fullSp / 2f

        baseText.forEachIndexed { i, baseChar ->
            if (i < topText.length && topText[i] != baseChar) {
                // stack with superscript
                val stack = FrameLayout(container.context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    clipChildren = false
                    clipToPadding = false
                    // vertical padding to make room for the superscript
                    setPadding(0, halfSp.toInt(), 0, 5)
                }

                // Base letter (bottom)
                TextView(container.context).apply {
                    text = baseChar.toString()
                    // SP units
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, halfSp)
                    // explicit color
                    setTextColor(ContextCompat.getColor(service, android.R.color.white))
                    gravity = Gravity.CENTER
                    setPadding(0, fullSp.toInt(), 0, 0)
                }.also(stack::addView)

                // Superscript letter (top)
                TextView(container.context).apply {
                    text = topText[i].toString()
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, halfSp)
                    setTextColor(ContextCompat.getColor(service, android.R.color.white))
                    gravity = Gravity.CENTER
                    setPadding(0, 0, 0, halfSp.toInt() + 10)
                }.also(stack::addView)

                container.addView(stack)
            } else {
                // Normal letter
                TextView(container.context).apply {
                    text = baseChar.toString()
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, fullSp)
                    setTextColor(ContextCompat.getColor(service, android.R.color.white))
                    gravity = Gravity.CENTER
                }.also(container::addView)
            }
        }
    }
}