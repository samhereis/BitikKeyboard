package com.shoktuk.shoktukkeyboard.keyboard

import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.text.InputType
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.widget.LinearLayout
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme.dpToPx

object BottomRowBuilder {
    private var selectionMode = false
    private var anchor = 0

    private fun extracted(service: InputMethodService): ExtractedText? = service.currentInputConnection?.getExtractedText(ExtractedTextRequest(), 0)

    private fun currentCursor(service: InputMethodService): Int = extracted(service)?.selectionEnd ?: 0

    private fun setSel(service: InputMethodService, start: Int, end: Int) {
        service.currentInputConnection?.setSelection(start, end)
    }

    fun createBottomRow(
        service: InputMethodService, isCaps: Boolean, mode: KeyboardMode, buttonHeight: Int, onModeChange: (KeyboardMode) -> Unit,
    ): LinearLayout {
        var isBitik = MyKeyboardService.current_writingSystem == WritingSystem.Bitik
        var comma = if (isBitik) "⹁" else ","
        var comma_alt = if (isBitik) "," else "⹁"
        var dot = if (isBitik) "·" else "."
        var dot_alt = if (isBitik) "." else "·"

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

        val targetHeight = (buttonHeight * 0.85f).toInt() // shrink height to 75%

        var returnView = SystemKeyBuilder.systemButton_Text(service, if (mode == KeyboardMode.Main) "⓬😀" else "🅰😀", buttonHeight, onClick = {
            if (MyKeyboardService.keyboardMode == KeyboardMode.Main) {
                onModeChange(KeyboardMode.Symbols)
            } else {
                onModeChange(KeyboardMode.Main)
            }
        }, onLongClick = {
            onModeChange(KeyboardMode.Emojis)
        }).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(context, 50).toFloat()
                setColor(KeyboardTheme.getSystemButtonStyle(context).fillColor.toColorInt())
            }

            if (Build.VERSION.SDK_INT >= 21) {
                clipToOutline = true
            }

            layoutParams = LinearLayout.LayoutParams(
                KeyboardTheme.getSystemButtonWidth(context), targetHeight, 0.1f
            ).apply {
                marginStart = dpToPx(context, 4)
                marginEnd = dpToPx(context, 4)
            }
        }

        bottomRow.addView(returnView)

        bottomRow.addView(
            SystemKeyBuilder.systemButton_Text(service, dot, buttonHeight, style = KeyboardTheme.getLetterButtonStyle_Normal(service), onClick = {
                service.currentInputConnection?.commitText(dot, 1)
                TopRowBuilder_Old.onTypedListener?.invoke()
            }, onLongClick = {
                service.currentInputConnection?.commitText(dot_alt, 1)
                TopRowBuilder_Old.onTypedListener?.invoke()
            })
        )

        val spaceBtn = SystemKeyBuilder.expandableSystemButton_Icon(
            service, KeyboardTheme.SPACE_ICON_FILE, " ", buttonHeight, onLongClick = {
                // long-press toggles selection mode; next drags will select
                selectionMode = true
                anchor = currentCursor(service)
                true
            })

        spaceBtn.setOnTouchListener(object : View.OnTouchListener {
            var downX = 0f
            var lastStep = 0
            val stepPx = spaceBtn.resources.displayMetrics.density * 10 // ~10dp per char
            var dragging = false

            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = e.x
                        lastStep = 0
                        dragging = false
                        if (!selectionMode) anchor = currentCursor(service)
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = e.x - downX
                        val step = (dx / stepPx).toInt()
                        if (step != lastStep) {
                            val delta = step - lastStep
                            lastStep = step
                            dragging = true
                            val et = extracted(service) ?: return true
                            val curStart = et.selectionStart
                            val curEnd = et.selectionEnd
                            val cur = if (selectionMode) curEnd else curEnd
                            val newPos = (cur + delta).coerceIn(0, (et.text?.length ?: 0))
                            if (selectionMode) {
                                val a = anchor.coerceIn(0, (et.text?.length ?: 0))
                                setSel(service, minOf(a, newPos), maxOf(a, newPos))
                            } else {
                                setSel(service, newPos, newPos)
                            }
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (!dragging && !selectionMode) {
                            service.currentInputConnection?.commitText(" ", 1)
                            TopRowBuilder_Old.onTypedListener?.invoke()
                        }
                        dragging = false
                        selectionMode = false
                        v.parent?.requestDisallowInterceptTouchEvent(false)
                        return true
                    }
                }
                return false
            }
        })

        bottomRow.addView(spaceBtn)

        if (isBitik && MyKeyboardService.context.wordSeparator != WordSeparator.Off) {
            var wp = MyKeyboardService.context.wordSeparator.id.replace("{", "").replace("}", "").reversed()

            bottomRow.addView(
                SystemKeyBuilder.expandableSystemButton_Text(
                    service, "⁚", buttonHeight, wp, onClick = {
                        service.currentInputConnection?.commitText(":", 1)
                        TopRowBuilder_Old.onTypedListener?.invoke()
                    })
            )
        }

        bottomRow.addView(
            SystemKeyBuilder.systemButton_Text(service, comma, buttonHeight, style = KeyboardTheme.getLetterButtonStyle_Normal(service), onClick = {
                service.currentInputConnection?.commitText(comma, 1)
                TopRowBuilder_Old.onTypedListener?.invoke()
            }, onLongClick = {
                service.currentInputConnection?.commitText(comma_alt, 1)
                TopRowBuilder_Old.onTypedListener?.invoke()
            })
        )

        returnView = SystemKeyBuilder.systemButton_Icon(
            service, if (MyKeyboardService.keyboardMode == KeyboardMode.SavedStrings) KeyboardTheme.DELETE_ICON_FILE else enterIcon, buttonHeight, onClick = {
                if (MyKeyboardService.keyboardMode == KeyboardMode.SavedStrings) {
                    SystemKeyBuilder.performDelete(MyKeyboardService.context)
                } else {
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
                }
            }).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(context, 50).toFloat()
                setColor(KeyboardTheme.getSystemButtonStyle(context).fillColor.toColorInt())
            }

            if (Build.VERSION.SDK_INT >= 21) {
                clipToOutline = true
            }

            layoutParams = LinearLayout.LayoutParams(
                KeyboardTheme.getSystemButtonWidth(context), targetHeight, 0.1f
            ).apply {
                marginStart = dpToPx(context, 4)
                marginEnd = dpToPx(context, 4)
            }
        }

        if (Build.VERSION.SDK_INT >= 21) returnView.clipToOutline = true
        bottomRow.addView(returnView)
        return bottomRow
    }
}
