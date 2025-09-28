package com.shoktuk.shoktukkeyboard.keyboard

import GlowOverlayView
import JSTranscriber_Alphabet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService.Companion.context
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.kirilisaStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinStatus
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.ui.theme.ButtonStyle
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

object onBitikModeChanged {
    private val listeners = mutableListOf<(Boolean) -> Unit>()

    fun addListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
    }

    fun invoke(isActive: Boolean) {
        listeners.forEach { it(isActive) }
    }
}

object TopRowBuilder_Alphabet {
    var text_Original: String = ""
    var inputText_LastWord: String = ""
    var InputText_Transcribed: String = ""

    fun createTopRow(
        service: InputMethodService, buttonHeight: Int, mode: TextTranscription, onModeChange: (KeyboardMode) -> Unit, onAlphabetChange: () -> Unit
    ): LinearLayout {
        val alphabetLabel = if (context.latinStatus == Latin_Status.Off && context.kirilisaStatus == Kirilisa_Status.Off) "😎" else "А"
        val jsTranscriber = JSTranscriber_Alphabet(service)

        val rowLayout = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            clipToPadding = false
            clipChildren = false
            minimumHeight = buttonHeight
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = MyKeyboardService.buttonMargin
                bottomMargin = MyKeyboardService.buttonMargin
            }
        }

        val switchLanguageView = SystemKeyBuilder.systemButton_Text(
            service = service, text = alphabetLabel, buttonHeight = buttonHeight, onClick = { onAlphabetChange() })

        val centerSlot = FrameLayout(service).apply {
            layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f)
            clipToPadding = false
            clipChildren = false
            isClickable = true
            isFocusable = true
        }

        val lastWordContainer = createLastWordContainer(service, buttonHeight).apply {
            clipChildren = false
            clipToPadding = false
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val hintIcon = TextView(service).apply {
            text = "👆"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            alpha = if (MyKeyboardService.isBitikMode) 0f else 1f
            isClickable = false
            isFocusable = false
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            setPadding(25, 0, 0, 0)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                val m = (6 * resources.displayMetrics.density).toInt()
                marginStart = m
            }

        }

        val glowView = GlowOverlayView(service).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
            )

            isClickable = false
            isFocusable = false
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO


            alpha = if (MyKeyboardService.isBitikMode) 0.75f else 0f
            if (MyKeyboardService.isBitikMode) startRotating(4500L) else stopRotating()
            invalidate()
        }

        centerSlot.addView(glowView, 0)
        centerSlot.addView(hintIcon, 1)
        centerSlot.addView(lastWordContainer)

        centerSlot.setOnClickListener {
            MyKeyboardService.isBitikMode = !MyKeyboardService.isBitikMode
            onBitikModeChanged.invoke(MyKeyboardService.isBitikMode)
        }

        rowLayout.addView(switchLanguageView)
        rowLayout.addView(centerSlot)
        rowLayout.addView(
            SystemKeyBuilder.systemButton_Icon(
                service, KeyboardTheme.LANGUAGE_ICON_FILE, buttonHeight, onClick = {
                    val imm = service.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showInputMethodPicker()
                    TopRowBuilder_Old.onTypedListener.invoke()
                })
        )

        TopRowBuilder_Old.onTypedListener.addOnTypedListener(lastWordContainer) {
            lastWordContainer.post {
                updateLastWord(
                    service, service.currentInputConnection, jsTranscriber, lastWordContainer, buttonHeight, KeyboardTheme.getSystemButtonStyle(service)
                )
                lastWordContainer.requestLayout()
                lastWordContainer.invalidate()
            }
        }

        onBitikModeChanged.addListener { active ->
            centerSlot.post {
                if (active) {
                    glowView.animate().alpha(0.75f).setDuration(400).withStartAction { glowView.startRotating(4500L) }.start()
                } else {
                    glowView.animate().alpha(0f).setDuration(400).withEndAction { glowView.stopRotating() }.start()
                }
            }

            if (active) {
                hintIcon.animate().alpha(0f).setDuration(300).setStartDelay(100).start()
            } else {
                hintIcon.animate().alpha(1f).setDuration(300).setStartDelay(0).start()
            }
        }
        glowView.startRotating(4500L)
        glowView.invalidate()
        return rowLayout
    }

    private fun createLastWordContainer(
        service: InputMethodService,
        buttonHeight: Int,
    ): LinearLayout = LinearLayout(service).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        layoutDirection = View.LAYOUT_DIRECTION_LTR
        textDirection = View.TEXT_DIRECTION_LTR
        clipToPadding = false
        clipChildren = false
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

                val base = KeyboardTheme.getColor(6).toColorInt()
                val halfAlpha = ColorUtils.setAlphaComponent(base, if (MyKeyboardService.isBitikMode) 100 else 255)

                setColor(halfAlpha)
            }

            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            textDirection = View.TEXT_DIRECTION_LTR
            maxLines = 1
            isSingleLine = true
            setPadding(32, 0, 32, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )

            typeface = Typeface.DEFAULT_BOLD
            paint.isFakeBoldText = true
            paint.strokeWidth = 0.5f
            paint.style = Paint.Style.FILL_AND_STROKE

            onBitikModeChanged.addListener { active ->
                val startAlpha = if (active) 255 else 100   // where we are
                val endAlpha = if (active) 100 else 255   // where we’re going

                ValueAnimator.ofInt(startAlpha, endAlpha).apply {
                    duration = 400
                    addUpdateListener { animator ->
                        val alpha = animator.animatedValue as Int
                        background = android.graphics.drawable.GradientDrawable().apply {
                            cornerRadius = 48f
                            val base = KeyboardTheme.getColor(6).toColorInt()
                            val color = ColorUtils.setAlphaComponent(base, alpha)
                            setColor(color)
                        }
                    }
                    start()
                }
            }

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