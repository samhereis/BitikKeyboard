package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

object BottomRowBuilder {
    fun createBottomRow(
        service: InputMethodService, isCaps: Boolean, mode: KeyboardMode, buttonHeight: Int, onModeChange: (KeyboardMode) -> Unit,
    ): LinearLayout {
        var isBitik = MyKeyboardService.current_writingSystem == WritingSystem.Bitik
        var comma = if (isBitik) "⹁" else ","
        var dot = if (isBitik) "·" else "."

        val action = service.currentInputEditorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
        val isMultiline = (service.currentInputEditorInfo.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
        val enterIcon = when {
            action == EditorInfo.IME_ACTION_SEARCH -> KeyboardTheme.ENTER_SEARCH_ICON_FILE
            action == EditorInfo.IME_ACTION_GO -> KeyboardTheme.ENTER_GO_ICON_FILE
            action == EditorInfo.IME_ACTION_NEXT -> KeyboardTheme.ENTER_NEXT_ICON_FILE
            isMultiline -> KeyboardTheme.ENTER_ICON_FILE
            else -> KeyboardTheme.ENTER_ICON_FILE
        }

        val bottomRow = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = MyKeyboardService.buttonMargin
                bottomMargin = MyKeyboardService.buttonMargin
            }
        }

        bottomRow.addView(
            SystemKeyBuilder.systemButton_Text(service, if (mode == KeyboardMode.Main) "⓬😀" else "🅰😀", buttonHeight, onClick = {
                if (MyKeyboardService.keyboardMode == KeyboardMode.Main) {
                    onModeChange(KeyboardMode.Symbols)
                } else {
                    onModeChange(KeyboardMode.Main)
                }
            }, onLongClick = {
                onModeChange(KeyboardMode.Emojis)
            })
        )

        bottomRow.addView(
            SystemKeyBuilder.systemButton_Text(
                service, dot, buttonHeight, onClick = {
                    service.currentInputConnection?.commitText(dot, 1)
                    TopRowBuilder_Old.onTypedListener?.invoke()
                })
        )

        bottomRow.addView(
            SystemKeyBuilder.expandableSystemButton_Icon(
                service, KeyboardTheme.SPACE_ICON_FILE, " ", buttonHeight, onLongClick = {
                    service.currentInputConnection?.commitText("⁚", 1)
                    TopRowBuilder_Old.onTypedListener?.invoke()
                })
        )

        if (isBitik && MyKeyboardService.context.wordSeparator != WordSeparator.Off) {
            var wp = MyKeyboardService.context.wordSeparator.id.replace("{", "").replace("}", "").reversed()

            bottomRow.addView(
                SystemKeyBuilder.expandableSystemButton_Text(
                    service, "⁚", buttonHeight, wp
                )
            )
        }

        bottomRow.addView(
            SystemKeyBuilder.systemButton_Text(
                service, comma, buttonHeight, onClick = {
                    service.currentInputConnection?.commitText(comma, 1)
                    TopRowBuilder_Old.onTypedListener?.invoke()
                })
        )

        bottomRow.addView(
            SystemKeyBuilder.systemButton_Icon(
                service, enterIcon, buttonHeight, onClick = {
                    val ic = service.currentInputConnection
                    val editorInfo = service.currentInputEditorInfo

                    if (ic != null && editorInfo != null) {
                        val action = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
                        val isMultiline = (editorInfo.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0

                        when {
                            action == EditorInfo.IME_ACTION_SEARCH || action == EditorInfo.IME_ACTION_GO || action == EditorInfo.IME_ACTION_NEXT -> {
                                ic.performEditorAction(action)
                            }

                            isMultiline -> {
                                ic.commitText("\n", 1)
                                TopRowBuilder_Old.onTypedListener?.invoke()
                            }

                            else -> {
                                ic.commitText("\n", 1)
                                TopRowBuilder_Old.onTypedListener?.invoke()
                            }
                        }
                    }
                })
        )
        return bottomRow
    }
}
