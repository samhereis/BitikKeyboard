package com.shoktuk.shoktukkeyboard.keyboard

import JSTranscriber_Alphabet
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService.Companion.context
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.InputText_Transcribed
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.inputText_LastWord
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.text_Original
import com.shoktuk.shoktukkeyboard.project.data.Arabic_Status
import com.shoktuk.shoktukkeyboard.project.data.BitikTranscriptionAlphabets
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikTranscriptionAlphabets
import com.shoktuk.shoktukkeyboard.suggestions.SuggestionNeighbours
import com.shoktuk.shoktukkeyboard.suggestions.WordSuggestionEngine
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.arabicStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.kirilisaStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.writingSystem
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionsEnabled
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
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
    /** The ★ and ⚙ buttons of the current top row: hidden while the suggestion strip has more than three chips. */
    private var sideButtons: List<View> = emptyList()

    private fun squareUp(service: InputMethodService, view: View, size: Int) {
        val inset = (service.resources.displayMetrics.density * 3).toInt()
        view.layoutParams = LinearLayout.LayoutParams(size, size)
        view.background = android.graphics.drawable.InsetDrawable(
            KeyboardTheme.createDrawableFromStyle(service, KeyboardTheme.getSystemButtonStyle(service)), inset, inset, inset, inset
        )
    }

    private fun showSideButtons(show: Boolean) {
        sideButtons.forEach { it.visibility = if (show) View.VISIBLE else View.GONE }
    }

    fun createTopRow(
        service: InputMethodService, buttonHeight: Int, mode: TextTranscription, onModeChange: (KeyboardMode) -> Unit, onAlphabetChange: () -> Unit
    ): LinearLayout {
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

        val newGlow = NewGlowDrawable()
        
        if (MyKeyboardService.isAutoWriteBitikMode) {
            newGlow.setVisibleAlpha(1f)
            newGlow.startRotating()
        } else {
            newGlow.setVisibleAlpha(0f)
            newGlow.stopRotating()
        }

        val switchLanguageView = SystemKeyBuilder.systemButton_Text(
            service = service, text = alphabetLabel, buttonHeight = buttonHeight, onClick = { onAlphabetChange() }, applyBitikFont = true)

        val centerSlot = FrameLayout(service).apply {
            background = newGlow
            val gap = (service.resources.displayMetrics.density * 6).toInt()
            setPadding(gap, 0, gap, 0)
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

        centerSlot.addView(lastWordContainer)
        rowLayout.addView(switchLanguageView)
        val starButton = SystemKeyBuilder.systemButton_Text(
            service = service, text = "★", buttonHeight = buttonHeight, onClick = { onModeChange(KeyboardMode.SavedStrings) })
        rowLayout.addView(starButton)
        rowLayout.addView(centerSlot)
        val gearButton = SystemKeyBuilder.systemButton_Text(
            service = service, text = "⚙", buttonHeight = buttonHeight, onClick = { onModeChange(KeyboardMode.Settings) })
        rowLayout.addView(gearButton)
        sideButtons = listOf(starButton, gearButton)
        val languageButton = SystemKeyBuilder.systemButton_Icon(
            service, KeyboardTheme.LANGUAGE_ICON_FILE, buttonHeight, onClick = {
                val imm = service.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
                TopRowBuilder_Old.onTypedListener.invoke()
            })
        rowLayout.addView(languageButton)

        // Latin and Kiril: the four side buttons are square (1:1) and tighter, so the suggestions get the room.
        if (context.writingSystem == WritingSystem.Latin || context.writingSystem == WritingSystem.Kiril) {
            listOf(switchLanguageView, starButton, gearButton, languageButton).forEach { squareUp(service, it, buttonHeight) }
        }

        TopRowBuilder_Old.onTypedListener.addOnTypedListener(lastWordContainer) {
            lastWordContainer.post {
                updateLastWord(
                    service.currentInputConnection, jsTranscriber, lastWordContainer, KeyboardTheme.getSystemButtonStyle(service)
                )
                lastWordContainer.requestLayout()
                lastWordContainer.invalidate()
            }
        }

        onBitikModeChanged.addListener { active ->
            rowLayout.post {
                if (active) {
                    newGlow.startRotating()
                    newGlow.animateAlpha(1f, 400L, null)
                } else {
                    newGlow.animateAlpha(0f, 400L) { newGlow.stopRotating() }
                }
            }
        }

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

    fun updateLastWord(inputConnection: InputConnection?, transcriber: JSTranscriber_Alphabet, container: LinearLayout?, buttonStyle: ButtonStyle?) {
        val extraSeparators = "·.,⸮⹁:;!?()[]{}\"'"
        WordSuggestionEngine.onShardLoaded = { TopRowBuilder_Old.onTypedListener.invoke() }
        WordSuggestionEngine.configureFor(context)
        showSideButtons(true)

        text_Original = inputConnection?.getTextBeforeCursor(100, 0)?.toString().orEmpty()
        val regex = "[^\\p{L}${Regex.escape(extraSeparators)}]+".toRegex()
        inputText_LastWord = text_Original.split(regex).lastOrNull().orEmpty()
        InputText_Transcribed = transcriber.getTranscription(inputText_LastWord).orEmpty().ifEmpty { inputText_LastWord }

        InputText_Transcribed = TranscriptionProccessor().processTranscription_alphabet(InputText_Transcribed, context)

        if (container == null) {
            return
        }
        if (buttonStyle == null) {
            return
        }

        container.removeAllViews()
        val fullSp = buttonStyle.textSizeSp

        val textView = TextView(container.context).apply {
            text = InputText_Transcribed
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fullSp)
            setTextColor(KeyboardTheme.getColor(2).toColorInt())

            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 48f

                val base = KeyboardTheme.getColor(6).toColorInt()
                setColor(base)
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

            typeface = KeyboardTheme.bitikTypeface(container.context) ?: Typeface.DEFAULT_BOLD
            paint.isFakeBoldText = true
            paint.strokeWidth = 0.5f
            paint.style = Paint.Style.FILL_AND_STROKE

            setOnClickListener {
                if (inputText_LastWord.isNotBlank()) {
                    if (inputConnection != null) {
                        MyKeyboardService().replaceText(inputConnection, InputText_Transcribed)

                        InputText_Transcribed = ""
                        text = inputText_LastWord
                    }
                }
            }
        }

        val suggests = (context.writingSystem == WritingSystem.Latin || context.writingSystem == WritingSystem.Kiril) && context.suggestionsEnabled(context.writingSystem)
        if (!suggests) {
            container.addView(textView)
            return
        }

        // Latin and Kiril: a scrollable strip. First the Bitik transcription (if switched on), then completions.
        val density = container.resources.displayMetrics.density
        val bitikOn = context.bitikTranscriptionAlphabets == BitikTranscriptionAlphabets.On
        val kiril = context.writingSystem == WritingSystem.Kiril
        val strip = LinearLayout(container.context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        val scroll = android.widget.HorizontalScrollView(container.context).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            // Chips fade out into the edges like a tunnel instead of being cut off.
            isHorizontalFadingEdgeEnabled = true
            setFadingEdgeLength((26 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            addView(strip, android.widget.FrameLayout.LayoutParams(android.widget.FrameLayout.LayoutParams.WRAP_CONTENT, android.widget.FrameLayout.LayoutParams.MATCH_PARENT))
        }
        container.clipChildren = true
        container.clipToPadding = true
        (container.parent as? ViewGroup)?.apply { clipChildren = true }
        container.addView(scroll)

        val textColor = KeyboardTheme.getColor(2).toColorInt()
        val chipColor = KeyboardTheme.getColor(6).toColorInt()
        fun chipBackground() = android.graphics.drawable.GradientDrawable().apply { cornerRadius = 48f; setColor(chipColor) }
        fun label(text: String, sp: Float, bitik: Boolean = false, alpha: Float = 1f) = TextView(container.context).apply {
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
            setTextColor(textColor)
            this.alpha = alpha
            gravity = Gravity.CENTER
            maxLines = 1
            includeFontPadding = false
            if (bitik) { typeface = KeyboardTheme.bitikTypeface(container.context) ?: Typeface.DEFAULT_BOLD; paint.isFakeBoldText = true }
        }
        /** A plain chip: [text] in the Bitik font or the system font. */
        fun chip(text: String, bitik: Boolean, onClick: () -> Unit) = label(text, fullSp, bitik = bitik).apply {
            setPadding((14 * density).toInt(), 0, (14 * density).toInt(), 0)
            background = chipBackground()
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (container.height * 0.82f).toInt().coerceAtLeast(1)).apply {
                gravity = Gravity.CENTER_VERTICAL
                marginEnd = (6 * density).toInt()
            }
            setOnClickListener { onClick() }
        }

        if (bitikOn && InputText_Transcribed.isNotEmpty() && inputText_LastWord.isNotBlank()) {
            val last = inputText_LastWord
            val bitikWord = InputText_Transcribed
            strip.addView(chip(bitikWord, true) { commitBitik(inputConnection, last.length, bitikWord, false) })
        }

        // Only the trailing letters are a word being typed ("." and "," stay in the text).
        val typed = inputText_LastWord.takeLastWhile { it.isLetter() || it == '-' }
        if (typed.isEmpty() || inputConnection == null) return
        val neighbours = SuggestionNeighbours.pairs
        // Building a chip costs a view each, so only the best few are shown (the strip scrolls).
        val words = WordSuggestionEngine.completions(typed, 16, neighbours)
        showSideButtons(strip.childCount + words.size <= 3)
        for (suggestion in words) {
            val word = suggestion.word
            strip.addView(chip(word, false) { commitBitik(inputConnection, typed.length, word, true) })
        }
    }

    /** Replaces the last [typedLength] letters with [text] (plus a space when [space]) and refreshes the bar. */
    private fun commitBitik(ic: InputConnection?, typedLength: Int, text: String, space: Boolean) {
        ic ?: return
        ic.beginBatchEdit()
        ic.deleteSurroundingText(typedLength, 0)
        ic.commitText(if (space) "$text " else text, 1)
        ic.endBatchEdit()
        TopRowBuilder_Old.onTypedListener.invoke()
    }
}
