package com.shoktuk.shoktukkeyboard.keyboard

import JSTranscriber
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService.Companion.context
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.InputText_Transcribed
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.InputText_Transcribed_Alt
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.inputText_LastWord
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.text_Original
import com.shoktuk.shoktukkeyboard.project.data.Arabic_Status
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.arabicStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.kirilisaStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.writingSystem
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.project.screens.settings.loadSavedStrings
import com.shoktuk.shoktukkeyboard.ui.theme.ButtonStyle
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import java.util.WeakHashMap

class OnTypedListener {
    private val listeners = WeakHashMap<View, () -> Unit>()

    fun addOnTypedListener(view: View, listener: () -> Unit) {
        listeners[view] = listener
    }

    fun removeOnTypedListener(view: View) {
        listeners.remove(view)
    }

    fun removeAll() {
        listeners.clear()
    }

    fun invoke() {
        cleanupNulls()
        listeners.values.forEach { it.invoke() }
    }

    fun cleanupNulls() {
        val it = listeners.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (e.key == null || e.value == null) it.remove()
        }
    }
}

object TopRowBuilder_Old {
    var onTypedListener: OnTypedListener = OnTypedListener()

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
        if (context.latinStatus == Latin_Status.Off && context.kirilisaStatus == Kirilisa_Status.Off && context.arabicStatus == Arabic_Status.Off) {
            alphabetLabel = "😎"
        } else {
            if (context.writingSystem == WritingSystem.Latin) {
                alphabetLabel = "А"
            }
            if (context.writingSystem == WritingSystem.Kiril) {
                alphabetLabel = "ж"
            }
            if (context.writingSystem == WritingSystem.Arab) {
                alphabetLabel = "س"
            }
        }

        var switchLanguageView = SystemKeyBuilder.systemButton_Text(
            service = service, text = alphabetLabel, buttonHeight = buttonHeight, onClick = { onAlphabetChange() })
        rowLayout.addView(switchLanguageView)
        val jsTranscriber = JSTranscriber(service)

        if (mode == TextTranscription.On) {
            val lastWordContainer = createLastWordContainer(service, buttonHeight).apply {
                clipChildren = false
                clipToPadding = false
            }

            rowLayout.addView(lastWordContainer)

            onTypedListener.addOnTypedListener(lastWordContainer) {
                lastWordContainer.post {
                    updateLastWord(service.currentInputConnection, jsTranscriber, lastWordContainer, KeyboardTheme.getSystemButtonStyle(service))
                    lastWordContainer.requestLayout()
                    lastWordContainer.invalidate()
                }
            }
        } else {
            val scrollView = HorizontalScrollView(service).apply {
                isHorizontalScrollBarEnabled = false
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }

            val innerClipboardRow = LinearLayout(service).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val clipboardItems = loadSavedStrings(service)

            val screenWidth = service.resources.displayMetrics.widthPixels
            val maxItemWidth = (screenWidth * 0.5f).toInt()

            if (clipboardItems.isEmpty()) {
                clipboardItems.add(" ")
            }

            for (clipText in clipboardItems) {
                val preview = if (clipText.length > 20) clipText.take(19) + "…" else clipText

                val itemView = SystemKeyBuilder.systemButton_Text(
                    service = service,
                    text = " $preview ",
                    buttonHeight = buttonHeight,
                    bald = false,
                    style = KeyboardTheme.getLetterButtonStyle_Normal(innerClipboardRow.context),
                    weight = 0f,
                    onClick = {
                        onKeyPressed?.invoke(service.currentInputConnection, clipText, false)

                    })
                itemView.setPadding(25, 0, 25, 0)

                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, buttonHeight
                ).apply {
                    marginEnd = MyKeyboardService.buttonMargin
                }

                itemView.layoutParams = params
                itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(maxItemWidth, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(buttonHeight, View.MeasureSpec.EXACTLY)
                )

                innerClipboardRow.addView(itemView)
            }

            scrollView.addView(innerClipboardRow)
            rowLayout.addView(scrollView)
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
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            0, buttonHeight, 1f
        ).apply {
            marginStart = MyKeyboardService.buttonMargin
            marginEnd = MyKeyboardService.buttonMargin
        }
    }

    fun updateLastWord(inputConnection: InputConnection?, transcriber: JSTranscriber, container: LinearLayout?, buttonStyle: ButtonStyle?) {
        val extraSeparators = "·.,⸮⹁:;!?()[]{}\"'"

        text_Original = inputConnection?.getTextBeforeCursor(100, 0)?.toString().orEmpty()
        val regex = "[^\\p{L}${Regex.escape(extraSeparators)}]+".toRegex()
        inputText_LastWord = text_Original.split(regex).lastOrNull().orEmpty()

        inputText_LastWord = TranscriptionProccessor().processTranscription_bitik(inputText_LastWord, context)

        InputText_Transcribed = transcriber.getTranscription(inputText_LastWord).orEmpty().ifEmpty { inputText_LastWord }
        InputText_Transcribed_Alt = transcriber.getTranscription_Alternative(inputText_LastWord).orEmpty().ifEmpty { inputText_LastWord }

        if (container == null) {
            return
        }
        if (buttonStyle == null) {
            return
        }

        container.removeAllViews()
        if (text_Original.isEmpty()) {
            return
        }

        val fullSp = buttonStyle.textSizeSp
        val halfSp = fullSp / 2f

        TextView(container.context).apply {
            text = "   ~ "
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fullSp)
            setTextColor(KeyboardTheme.getColor(2).toColorInt())
            gravity = Gravity.CENTER
        }.also(container::addView)

        InputText_Transcribed.forEachIndexed { i, baseChar ->
            if (i < InputText_Transcribed_Alt.length && InputText_Transcribed_Alt[i] != baseChar) {
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
                    text = InputText_Transcribed_Alt[i].toString()
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
}