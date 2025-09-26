package com.shoktuk.shoktukkeyboard.keyboard

import JSTranscriber_Alphabet
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService.Companion.context
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.kirilisaStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinStatus
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.ui.theme.ButtonStyle
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

object TopRowBuilder_Alphabet {
    var text_Original: String = ""
    var inputText_LastWord: String = ""
    var InputText_Transcribed: String = ""

    fun createTopRow(
        service: InputMethodService, buttonHeight: Int, mode: TextTranscription, onModeChange: (KeyboardMode) -> Unit, onAlphabetChange: () -> Unit
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

        var alphabetLabel = "𐰌"

        if (context.latinStatus == Latin_Status.Off && context.kirilisaStatus == Kirilisa_Status.Off) {
            alphabetLabel = "😎"
        } else {
            if (MyKeyboardService.current_writingSystem == WritingSystem.Latin) {
                alphabetLabel = "А"
            }
            if (MyKeyboardService.current_writingSystem == WritingSystem.Kiril) {
                alphabetLabel = "ж"
            }
        }

        var switchLanguageView = SystemKeyBuilder.systemButton_Text(
            service = service, text = alphabetLabel, buttonHeight = buttonHeight, onClick = { onAlphabetChange() })
        rowLayout.addView(switchLanguageView)
        val jsTranscriber = JSTranscriber_Alphabet(service)

        val lastWordContainer = createLastWordContainer(service, buttonHeight).apply {
            clipChildren = false
            clipToPadding = false
        }

        rowLayout.addView(lastWordContainer)

        TopRowBuilder_Old.onTypedListener.addOnTypedListener(lastWordContainer) {
            lastWordContainer.post {
                updateLastWord(
                    service, service.currentInputConnection, jsTranscriber, lastWordContainer, buttonHeight, KeyboardTheme.getSystemButtonStyle(service)
                )
                lastWordContainer.requestLayout()
                lastWordContainer.invalidate()
            }
        }

        rowLayout.addView(
            SystemKeyBuilder.systemButton_Icon(
                service, KeyboardTheme.LANGUAGE_ICON_FILE, buttonHeight, onClick = {
                    val imm = service.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showInputMethodPicker()
                    TopRowBuilder_Old.onTypedListener.invoke()
                })
        )

        return rowLayout
    }

    private fun createLastWordContainer(
        service: InputMethodService, buttonHeight: Int,
    ): LinearLayout = LinearLayout(service).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        layoutDirection = View.LAYOUT_DIRECTION_LTR
        textDirection = View.TEXT_DIRECTION_LTR
        layoutParams = LinearLayout.LayoutParams(
            0, buttonHeight, 1f
        ).apply {
            marginStart = MyKeyboardService.buttonMargin
            marginEnd = MyKeyboardService.buttonMargin
        }
    }

    fun updateLastWord(
        service: InputMethodService, inputConnection: InputConnection?, transcriber: JSTranscriber_Alphabet, container: LinearLayout?, buttonHeight: Int, buttonStyle: ButtonStyle?
    ) {
        text_Original = inputConnection?.getTextBeforeCursor(100, 0)?.toString().orEmpty()
        inputText_LastWord = text_Original.split("[^\\p{L}\\p{N}]+".toRegex()).lastOrNull().orEmpty()
        InputText_Transcribed = transcriber.getTranscription(inputText_LastWord).orEmpty().ifEmpty { inputText_LastWord }

        if (container == null) {
            return
        }
        if (buttonStyle == null) {
            return
        }

        container.removeAllViews()
        val fullSp = buttonStyle.textSizeSp.value

        val textView = TextView(container.context).apply {
            text = InputText_Transcribed
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fullSp)
            setTextColor(KeyboardTheme.getColor(2).toColorInt())
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 48f
                setColor(KeyboardTheme.getColor(1).toColorInt())
            }
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            textDirection = View.TEXT_DIRECTION_LTR
            maxLines = 1
            isSingleLine = true
            setPadding(32, 5, 32, 5)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )

            typeface = Typeface.DEFAULT_BOLD
            paint.isFakeBoldText = true
            paint.strokeWidth = 0.5f
            paint.style = Paint.Style.FILL_AND_STROKE

            setOnClickListener {
                if (inputText_LastWord.isNotBlank()) {
                    inputConnection?.apply {
                        deleteSurroundingText(inputText_LastWord.length, 0)
                        commitText(InputText_Transcribed + " ", 1)

                        InputText_Transcribed = ""
                        text = inputText_LastWord
                    }
                }
            }
        }

        container.addView(textView)
    }
}