package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.shoktuk.shoktukkeyboard.project.data.Arabic_Status
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.arabicStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.kirilisaStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.writingSystem
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.project.screens.settings.loadSavedStrings
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

object TopRowBuilder {
    fun createTopRow(
        service: InputMethodService, buttonHeight: Int, onModeChange: (KeyboardMode) -> Unit, onAlphabetChange: () -> Unit
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
        if (MyKeyboardService.Companion.context.latinStatus == Latin_Status.Off && MyKeyboardService.Companion.context.kirilisaStatus == Kirilisa_Status.Off && MyKeyboardService.Companion.context.arabicStatus == Arabic_Status.Off) {
            alphabetLabel = "😎"
        } else {
            if (MyKeyboardService.Companion.context.writingSystem == WritingSystem.Latin) {
                alphabetLabel = "А"
            }
            if (MyKeyboardService.Companion.context.writingSystem  == WritingSystem.Kiril) {
                alphabetLabel = "ж"
            }
            if (MyKeyboardService.Companion.context.writingSystem  == WritingSystem.Arab) {
                alphabetLabel = "س"
            }
        }

        val switchLanguageView = SystemKeyBuilder.systemButton_Text(
            service = service, text = alphabetLabel, buttonHeight = buttonHeight, onClick = { onAlphabetChange() })
        rowLayout.addView(switchLanguageView)

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

        val clipboardItems = loadSavedStrings(service, MyKeyboardService.current_writingSystem)

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
                applyBitikFont = true,
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

        val imePicker = SystemKeyBuilder.systemButton_Icon(
            service = service, assetPath = KeyboardTheme.LANGUAGE_ICON_FILE, buttonHeight = buttonHeight, onClick = {
                val imm = service.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
                TopRowBuilder_Old.onTypedListener?.invoke()
            })

        rowLayout.addView(imePicker)

        return rowLayout
    }
}