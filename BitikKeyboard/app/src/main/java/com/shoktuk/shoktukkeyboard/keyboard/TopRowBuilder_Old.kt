package com.shoktuk.shoktukkeyboard.keyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.systems.JSTranscriber

object TopRowBuilder_Old {
    var onTypedListener: (() -> Unit)? = null
    var margin = 10;
    var textTranscription = true

    fun createTopRow(
        service: InputMethodService, buttonHeight: Int, textTranscription: TextTranscription, margin: Int, onModeChange: (String) -> Unit, onAlphabetChange: () -> Unit
    ): LinearLayout {
        this.margin = margin;
        this.textTranscription = textTranscription == TextTranscription.On

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
                service = service,
                assetPath = null,
                textToSet = alphabetLabel,
                buttonHeight = buttonHeight,
                margin = margin,
                onClick = { onAlphabetChange() },
                buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        if (textTranscription == TextTranscription.On) {
            val lastWordContainer = createLastWordContainer(service, buttonHeight, margin).apply {
                clipChildren = false
                clipToPadding = false
            }

            rowLayout.addView(lastWordContainer)

            val jsTranscriber = JSTranscriber(service)
            onTypedListener = {
                lastWordContainer.post {
                    updateLastWord(
                        service, service.currentInputConnection, jsTranscriber,lastWordContainer,  KeyboardTheme.getSystemButtonStyle(service)
                    )
                    lastWordContainer.requestLayout()
                    lastWordContainer.invalidate()
                }
            }

            lastWordContainer.addView(getPlaceholderText(service, lastWordContainer))
        } else {
            rowLayout.addView(getPlaceholderText(service, rowLayout))
        }

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
        layoutParams = LinearLayout.LayoutParams(
            0, buttonHeight, 1f
        ).apply {
            marginStart = margin
            marginEnd = margin
        }
    }

    fun updateLastWord(
        service: InputMethodService, inputConnection: InputConnection?,transcriber: JSTranscriber, container: LinearLayout?,  buttonStyle: ButtonStyle?
    ) {
        val before = inputConnection?.getTextBeforeCursor(100, 0)?.toString().orEmpty()

        val lastWord = before.split("[^\\p{L}\\p{N}]+".toRegex()).lastOrNull().orEmpty()
        val topText = transcriber.getTranscription_Alternative(lastWord).orEmpty().ifEmpty { lastWord }
        val baseText = transcriber.getTranscription(lastWord).orEmpty().ifEmpty { lastWord }

        if(container == null) {return}
        if(buttonStyle == null) {return}

        container.removeAllViews()
        if (before.isEmpty()) {
            container.addView(getPlaceholderText(service, container))
            return
        }

        val fullSp = buttonStyle.textSizeSp.value
        val halfSp = fullSp / 2f

        TextView(container.context).apply {
            text = "   ~ "
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fullSp)
            setTextColor(ContextCompat.getColor(service, android.R.color.white))
            gravity = Gravity.CENTER
        }.also(container::addView)

        baseText.forEachIndexed { i, baseChar ->
            if (i < topText.length && topText[i] != baseChar) {
                val stack = FrameLayout(container.context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    clipChildren = false
                    clipToPadding = false
                    setPadding(0, halfSp.toInt(), 0, 5)
                }

                TextView(container.context).apply {
                    text = baseChar.toString()
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, halfSp)
                    setTextColor(ContextCompat.getColor(service, android.R.color.white))
                    gravity = Gravity.CENTER
                    setPadding(0, halfSp.toInt() + 12, 0, 0)
                }.also(stack::addView)

                TextView(container.context).apply {
                    text = topText[i].toString()
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, halfSp)
                    setTextColor(ContextCompat.getColor(service, android.R.color.white))
                    gravity = Gravity.CENTER
                    setPadding(0, 0, 0, halfSp.toInt() + 12)
                }.also(stack::addView)

                container.addView(stack)
            } else {
                TextView(container.context).apply {
                    text = baseChar.toString()
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, fullSp)
                    setTextColor(ContextCompat.getColor(service, android.R.color.white))
                    gravity = Gravity.CENTER
                }.also(container::addView)
            }
        }


        TextView(container.context).apply {
            text = " ~   "
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fullSp)
            setTextColor(ContextCompat.getColor(service, android.R.color.white))
            gravity = Gravity.CENTER
        }.also(container::addView)
    }

    fun getPlaceholderText(service: InputMethodService, rowLayout: LinearLayout): TextView {
        val textView = TextView(rowLayout.context).apply {
            text = if (textTranscription) "𐰖𐰕𐰃𐰬𐰕" else "𐱅𐰭𐰼𐰃 𐰅𐰠𐰢𐰚𐰁 𐰌𐰝𐰢𐰓𐰢"

            isSingleLine = false
            setLines(2)
            ellipsize = null

            setTextColor(ContextCompat.getColor(service, android.R.color.white))

            val baseSp = KeyboardTheme.getHintButtonTextSize(service).value
            setTextSize(TypedValue.COMPLEX_UNIT_SP, baseSp * 1.35f)

            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER

            setBackgroundColor(Color.TRANSPARENT)

            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply {
                marginStart = margin
                marginEnd = margin
            }

            visibility = View.VISIBLE
        }

        return textView
    }
}