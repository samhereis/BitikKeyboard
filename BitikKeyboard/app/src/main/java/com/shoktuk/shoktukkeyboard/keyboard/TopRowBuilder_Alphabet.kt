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
import com.shoktuk.shoktukkeyboard.suggestions.SuggestionViews
import com.shoktuk.shoktukkeyboard.suggestions.WordSuggestionEngine
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.arabicStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.kirilisaStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.writingSystem
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionsEnabled
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionTranscription
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

    /** The Auto Bitik glow: round the whole centre slot when there is no suggestion strip, otherwise round the Bitik card only. */
    private var slotGlow: NewGlowDrawable? = null
    private val cardGlow = NewGlowDrawable().apply {
        cornerRadius = 20f
        innerGlowWidth = 4f
        innerGlowBlur = 14
        refreshFilters()
    }
    private var glowOnStrip = false
    private var cardShown = false

    private fun applyGlow(animate: Boolean) {
        val on = MyKeyboardService.isAutoWriteBitikMode
        fun drive(g: NewGlowDrawable?, show: Boolean) {
            g ?: return
            if (show) {
                g.startRotating()
                if (animate) g.animateAlpha(1f, 400L, null) else g.setVisibleAlpha(1f)
            } else if (animate) g.animateAlpha(0f, 400L) { g.stopRotating() } else { g.setVisibleAlpha(0f); g.stopRotating() }
        }
        drive(slotGlow, on && !glowOnStrip)
        drive(cardGlow, on && glowOnStrip && cardShown)
    }

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
        slotGlow?.stopRotating()
        slotGlow = newGlow
        cardGlow.stopRotating()
        glowOnStrip = false
        applyGlow(false)

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
            rowLayout.post { applyGlow(true) }
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
        val configure = WordSuggestionEngine.configurator(context)
        // A lookup still running for an older state of the text must not draw over this one.
        com.shoktuk.shoktukkeyboard.suggestions.SuggestionWorker.cancel()

        LastWord.extract(inputConnection)
        InputText_Transcribed = transcriber.getTranscription(inputText_LastWord).orEmpty().ifEmpty { inputText_LastWord }

        InputText_Transcribed = TranscriptionProccessor().processTranscription_alphabet(InputText_Transcribed, context)

        if (container == null) {
            return
        }
        if (buttonStyle == null) {
            return
        }

        val fullSp = buttonStyle.textSizeSp - 2f

        val textView = TextView(container.context).apply {
            text = InputText_Transcribed
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fullSp)
            setTextColor(KeyboardTheme.getColor(2).toColorInt())

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

        // Password, e-mail and similar fields get nothing here at all (not even the Bitik read-out of what is typed).
        if (!MyKeyboardService.canSuggest()) { container.removeAllViews(); showSideButtons(true); glowOnStrip = false; applyGlow(false); return }
        val suggests = (context.writingSystem == WritingSystem.Latin || context.writingSystem == WritingSystem.Kiril || context.writingSystem == WritingSystem.Arab) && context.suggestionsEnabled(context.writingSystem)
        if (!suggests) {
            container.removeAllViews()
            showSideButtons(true)
            glowOnStrip = false
            applyGlow(false)
            container.addView(textView)
            return
        }

        // A scrollable strip: first the Bitik transcription (if switched on), then completions.
        val bitikOn = context.bitikTranscriptionAlphabets == BitikTranscriptionAlphabets.On
        val cardHeight = (container.height * 0.94f).toInt().coerceAtLeast(1)
        val last = inputText_LastWord
        val bitikWord = InputText_Transcribed
        val writingSystem = context.writingSystem
        val showCyrillic = writingSystem != WritingSystem.Kiril && context.suggestionTranscription(writingSystem)

        // Rebuilds the whole strip in one go: the Bitik card, then the completions.
        fun build(words: List<WordSuggestionEngine.Suggestion>, typed: String) {
            container.removeAllViews()
            cardShown = false
            val strip = SuggestionViews.strip(container.context)
            container.clipChildren = true
            container.clipToPadding = true
            (container.parent as? ViewGroup)?.apply { clipChildren = true }
            container.addView(strip.scroll, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            if (bitikOn && bitikWord.isNotEmpty() && last.isNotBlank()) {
                strip.row.addView(SuggestionViews.wordCard(container.context, cardHeight, bitikWord, fullSp, bitik = true) { commitBitik(inputConnection, last.length, bitikWord, false) }
                    .also { it.foreground = cardGlow; cardShown = true })
            }
            // The aurora belongs to the Bitik card alone, not to the whole strip.
            glowOnStrip = true
            applyGlow(false)
            val firstCards = strip.row.childCount
            for (suggestion in words) {
                val word = suggestion.word
                // Latin words show their Cyrillic spelling, Arabic ones their Latin spelling.
                val cyrillic = if (showCyrillic) (if (writingSystem == WritingSystem.Arab) suggestion.latin else suggestion.cyr) else null
                strip.row.addView(SuggestionViews.wordCard(container.context, cardHeight, word, fullSp, transcription = cyrillic) {
                    com.shoktuk.shoktukkeyboard.suggestions.LearnedWords.learn(suggestion.cyr)
                    commitBitik(inputConnection, typed.length, word, true)
                })
            }
            showSideButtons(firstCards + words.size <= 3)
        }

        // Only the trailing letters are a word being typed ("." and "," stay in the text).
        val typed = inputText_LastWord.takeLastWhile { it.isLetter() || it == '-' }
        if (typed.isEmpty() || inputConnection == null) { build(emptyList(), typed); return }
        val neighbours = SuggestionNeighbours.pairs
        // The search runs on the suggestion thread. The strip is rebuilt in one go when it is done (a newer key press drops
        // it), so what is on screen, side buttons included, stays as it is meanwhile instead of flashing empty.
        // Building a chip costs a view each, so only the best few are shown (the strip scrolls).
        com.shoktuk.shoktukkeyboard.suggestions.SuggestionWorker.submit({
            configure()
            WordSuggestionEngine.completions(typed, 16, neighbours)
        }) { words -> build(words, typed) }
    }

    /** Replaces the last [typedLength] letters with [text] (plus a space when [space]) and refreshes the bar. */
    private fun commitBitik(ic: InputConnection?, typedLength: Int, text: String, space: Boolean) {
        ic ?: return
        ic.beginBatchEdit()
        ic.deleteSurroundingText(MyKeyboardService.safeWordLength(ic, typedLength), 0)
        ic.commitText(if (space) "$text " else text, 1)
        ic.endBatchEdit()
        if (space) MyKeyboardService.markAutoSpace(ic)
        TopRowBuilder_Old.onTypedListener.invoke()
    }
}
