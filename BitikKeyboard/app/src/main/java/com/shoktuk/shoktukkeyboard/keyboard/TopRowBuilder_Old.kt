package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.systems.JSTranscriber
import com.shoktuk.shoktukkeyboard.ui.theme.ButtonStyle
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

object TopRowBuilder_Old {
    var onTypedListener: (() -> Unit)? = null

    fun createTopRow(
        service: InputMethodService, buttonHeight: Int, onModeChange: (String) -> Unit, onAlphabetChange: () -> Unit
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
                service = service, text = alphabetLabel, buttonHeight = buttonHeight, onClick = { onAlphabetChange() })
        )

        if (MyKeyboardService.textTranscription == TextTranscription.On) {
            val lastWordContainer = createLastWordContainer(service, buttonHeight).apply {
                clipChildren = false
                clipToPadding = false
            }

            rowLayout.addView(lastWordContainer)

            val jsTranscriber = JSTranscriber(service)
            onTypedListener = {
                lastWordContainer.post {
                    updateLastWord(
                        service, service.currentInputConnection, jsTranscriber, lastWordContainer, KeyboardTheme.getSystemButtonStyle(service)
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
            SystemKeyBuilder.systemButton_Icon(
                service, KeyboardTheme.LANGUAGE_ICON_FILE, buttonHeight, onClick = {
                    val imm = service.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showInputMethodPicker()
                    TopRowBuilder_Old.onTypedListener?.invoke()
                })
        )

        return rowLayout
    }

    private fun createLastWordContainer(
        service: InputMethodService, buttonHeight: Int,
    ): LinearLayout = LinearLayout(service).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            0, buttonHeight, 1f
        ).apply {
            marginStart = MyKeyboardService.buttonMargin
            marginEnd = MyKeyboardService.buttonMargin
        }
    }

    fun updateLastWord(
        service: InputMethodService, inputConnection: InputConnection?, transcriber: JSTranscriber, container: LinearLayout?, buttonStyle: ButtonStyle?
    ) {
        val before = inputConnection?.getTextBeforeCursor(100, 0)?.toString().orEmpty()

        val lastWord = before.split("[^\\p{L}\\p{N}]+".toRegex()).lastOrNull().orEmpty()
        val topText = transcriber.getTranscription_Alternative(lastWord).orEmpty().ifEmpty { lastWord }
        val baseText = transcriber.getTranscription(lastWord).orEmpty().ifEmpty { lastWord }

        if (container == null) {
            return
        }
        if (buttonStyle == null) {
            return
        }

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
                    setTextColor(KeyboardTheme.getColor(2).toColorInt())
                    gravity = Gravity.CENTER
                    setPadding(0, halfSp.toInt() + 12, 0, 0)
                }.also(stack::addView)

                TextView(container.context).apply {
                    text = topText[i].toString()
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, halfSp)
                    setTextColor(KeyboardTheme.getColor(2).toColorInt())
                    gravity = Gravity.CENTER
                    setPadding(0, 0, 0, halfSp.toInt() + 12)
                }.also(stack::addView)

                container.addView(stack)
            } else {
                TextView(container.context).apply {
                    text = baseChar.toString()
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, fullSp)
                    setTextColor(KeyboardTheme.getColor(2).toColorInt())
                    gravity = Gravity.CENTER
                }.also(container::addView)
            }
        }


        TextView(container.context).apply {
            text = " ~   "
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fullSp)
            setTextColor(KeyboardTheme.getColor(2).toColorInt())
            gravity = Gravity.CENTER
        }.also(container::addView)
    }

    fun getPlaceholderText(service: InputMethodService, rowLayout: LinearLayout): TextView {
        val textView = TextView(rowLayout.context).apply {
            text = if (MyKeyboardService.showTextTranscription) "𐰖𐰕𐰃𐰬𐰕" else "𐱅𐰭𐰼𐰃 𐰅𐰠𐰢𐰚𐰁 𐰌𐰝𐰢𐰓𐰢"

            isSingleLine = false
            setLines(2)
            ellipsize = null

            setTextColor(KeyboardTheme.getColor(2).toColorInt())

            val baseSp = KeyboardTheme.getHintButtonTextSize(service).value
            setTextSize(TypedValue.COMPLEX_UNIT_SP, baseSp * 1.35f)

            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER

            setBackgroundColor(Color.TRANSPARENT)

            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply {
                marginStart = MyKeyboardService.buttonMargin
                marginEnd = MyKeyboardService.buttonMargin
            }

            visibility = View.VISIBLE
        }

        return textView
    }
}