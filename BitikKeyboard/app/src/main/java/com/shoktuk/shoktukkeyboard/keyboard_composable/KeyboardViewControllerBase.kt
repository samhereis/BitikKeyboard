package com.shoktuk.shoktukkeyboard.keyboard

import JSTranscriber
import JSTranscriber_Alphabet
import KeyboardViewLifecycleOwner
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import com.shoktuk.shoktukkeyboard.project.data.AJ_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ANG_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.AS_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.Arabic_Status
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.EB_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EK_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EN_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ESH_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Variant
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import com.shoktuk.shoktukkeyboard.project.data.NavBarPaddingSolution
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ajVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.angVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.arabicStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.asVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ebVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ekVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.enVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.eshVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.kirilisaStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.letterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.navBarPaddingSolution
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.textTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.writingSystem
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

enum class KeyboardState(val id: String) {
    Main("Main"), Symbols("Symbols"), Emojis("Emojis"), SavedStrings("SavedStrings");
}

object onKeyPressed_Composable {
    var text_Original: String = ""
    var inputText_LastWord: String = ""
    var InputText_Transcribed: String = ""
    var InputText_Transcribed_Alt: String = ""

    private val listeners = mutableListOf<(InputConnection, String, Boolean) -> Unit>()

    fun addListener(listener: (InputConnection, String, Boolean) -> Unit) {
        listeners.clear()
        listeners.add(listener)
    }

    fun invoke(ic: InputConnection, key: String, isSystemKey: Boolean = false) {
        listeners.forEach { it(ic, key, isSystemKey) }
    }
}

object onSettingChanged_Composable {
    private val listeners = mutableListOf<() -> Unit>()

    fun addListener(listener: () -> Unit) {
        listeners.clear()
        listeners.add(listener)
    }

    fun invoke() {
        listeners.forEach { it() }
    }
}

class KeyboardViewControllerBase : InputMethodService() {
    companion object {
        lateinit var context: KeyboardViewControllerBase

        var keyboardRowModel = KeyboardRowsModel()

        var current_bitikVariant: BitikVariant = BitikVariant.Modern
        var current_bitikDialect: BitikDialect = BitikDialect.Altay
        var current_writingSystem: WritingSystem = WritingSystem.Bitik
        var current_letterTranscription: LetterTranscription = LetterTranscription.On
        var current_textTranscription: TextTranscription = TextTranscription.On

        // Reactive dark-theme state — updated from onWindowShown/onConfigurationChanged
        // because LocalConfiguration inside an IME ComposeView may not carry the night-mode bit.
        val isDarkTheme = mutableStateOf(false)

        // Reactive states read by composables
        val writingSystemState = mutableStateOf(WritingSystem.Bitik)
        val keyboardModeState = mutableStateOf(KeyboardState.Main)
        val alphabetLabelState = mutableStateOf("𐰌")
        val isBitikModeState = mutableStateOf(false)
        val alphabetTranscriptionState = mutableStateOf("")
        val transcriptionState = mutableStateOf("" to "")

        var keyboardMode: KeyboardState
            get() = keyboardModeState.value
            set(value) {
                keyboardModeState.value = value
            }

        var isBitikMode: Boolean
            get() = isBitikModeState.value
            set(value) {
                isBitikModeState.value = value
            }

        val showTextTranscription: Boolean get() = current_textTranscription == TextTranscription.On
        var fontScale: TextStyle = KeyboardStyle.buttonFont

        var bottomPadding: Int? = null
        val bottomPaddingState = mutableStateOf(0)
        var maxRowElementsCount: Double = 10.5
        var autoDisableShift: Boolean = false
    }

    private val keyboardViewLifecycleOwner = KeyboardViewLifecycleOwner()
    private var jsTranscriber: JSTranscriber? = null
    private var jsTranscriber_Alphabet: JSTranscriber_Alphabet? = null

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean = true

    override fun onWindowShown() {
        super.onWindowShown()
        context = this
        keyboardViewLifecycleOwner.onResume()
        isBitikMode = false
        bottomPadding = null
        updateDarkTheme()
        reloadKeyboard()
        onSettingChanged_Composable.addListener { reloadKeyboard() }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        updateDarkTheme()
    }

    private fun updateDarkTheme() {
        isDarkTheme.value = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        keyboardViewLifecycleOwner.onCreate()
    }

    override fun onCreateInputView(): View {
        context = this
        val composeView = ComposeView(this).apply {
            setContent {
                ShoktukKeyboardTheme(darkTheme = isDarkTheme.value) {
                    StandardKeyboardView(rowsModel = keyboardRowModel, onKeyPress = { key ->
                        currentInputConnection?.let { ic ->
                            handleKeyPress(key, ic, this@KeyboardViewControllerBase)
                        }
                        updateTranscription()
                    }, onAlphabetChange = { onAlphabetChange() }, onModeChange = { mode -> keyboardMode = mode })
                }
            }
        }
        applyInsetsNowAndOnChange(composeView)
        window?.window?.decorView?.let { keyboardViewLifecycleOwner.attachToDecorView(it) }
        return composeView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        updateTranscription()
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        updateTranscription()
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        keyboardViewLifecycleOwner.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        jsTranscriber?.close()
        jsTranscriber = null
        try {
            jsTranscriber_Alphabet?.close()
        } catch (_: Exception) {
        }
        jsTranscriber_Alphabet = null
        keyboardViewLifecycleOwner.onDestroy()
    }

    fun reloadKeyboard() {
        val ctx = context
        current_bitikVariant = ctx.keyboardVariant
        current_bitikDialect = ctx.bitikDialect
        current_writingSystem = ctx.writingSystem
        current_letterTranscription = ctx.letterTranscription
        current_textTranscription = ctx.textTranscription

        writingSystemState.value = current_writingSystem
        autoDisableShift = current_writingSystem != WritingSystem.Bitik

        maxRowElementsCount = when {
            current_writingSystem == WritingSystem.Kiril -> 12.25
            current_writingSystem == WritingSystem.Latin && ctx.latinVariant == Latin_Variant.full -> 13.75
            else -> 11.25
        }

        updateAlphabetLabel()

        val language = getLanguage()
        val layout = try {
            KeyboardLayoutLoader.loadKeyboardLayout(ctx, KeyboardMode.Main, language)
        } catch (_: Exception) {
            return
        }

        keyboardRowModel.row1.clear()
        keyboardRowModel.row2.clear()
        keyboardRowModel.row3.clear()

        layout.rows.getOrNull(0)?.filter { it.name != "Shift" && it.name != "Del" && it.name != "kgKey" }?.map { processKey(keyEntryToKeyboardKey(it)) }?.let { keyboardRowModel.row1.addAll(it) }

        layout.rows.getOrNull(1)?.filter { it.name != "Shift" && it.name != "Del" && it.name != "kgKey" }?.map { processKey(keyEntryToKeyboardKey(it)) }?.let { keyboardRowModel.row2.addAll(it) }

        layout.rows.getOrNull(2)?.filter { it.name != "Shift" && it.name != "Del" && it.name != "kgKey" }?.map { processKey(keyEntryToKeyboardKey(it)) }?.let { keyboardRowModel.row3.addAll(it) }
    }

    private fun updateAlphabetLabel() {
        val ctx = context
        alphabetLabelState.value = when {
            ctx.latinStatus == Latin_Status.Off && ctx.kirilisaStatus == Kirilisa_Status.Off && ctx.arabicStatus == Arabic_Status.Off -> "😎"
            ctx.writingSystem == WritingSystem.Latin -> "А"
            ctx.writingSystem == WritingSystem.Kiril -> "ж"
            ctx.writingSystem == WritingSystem.Arab -> "س"
            else -> "𐰌"
        }
    }

    fun onAlphabetChange() {
        val ctx = context
        if (ctx.latinStatus == Latin_Status.Off && ctx.kirilisaStatus == Kirilisa_Status.Off && ctx.arabicStatus == Arabic_Status.Off) {
            ctx.writingSystem = WritingSystem.Bitik
            keyboardMode = KeyboardState.Main
        } else {
            val available = mutableListOf(WritingSystem.Bitik)
            if (ctx.latinStatus == Latin_Status.On) available.add(WritingSystem.Latin)
            if (ctx.arabicStatus == Arabic_Status.On) available.add(WritingSystem.Arab)
            if (ctx.kirilisaStatus == Kirilisa_Status.On) available.add(WritingSystem.Kiril)

            var idx = available.indexOf(ctx.writingSystem)
            idx = (idx + 1) % available.size
            ctx.writingSystem = available[idx]
            keyboardMode = KeyboardState.Main
        }
        reloadKeyboard()
    }

    fun getLanguage(): String {
        if (current_writingSystem != WritingSystem.Bitik) {
            if (current_writingSystem == WritingSystem.Latin && context.latinVariant == Latin_Variant.full) {
                return "latin_full"
            }
            return current_writingSystem.id
        }
        return when (context.keyboardVariant) {
            BitikVariant.CLASSIC -> "enesay_old"
            BitikVariant.Modern -> "enesay_modern"
            else -> "enesay"
        }
    }

    private fun keyEntryToKeyboardKey(entry: KeyEntry): KeyboardKey {
        return KeyboardKey(
            name = entry.name,
            lowercase = entry.lowercase,
            lowerCaseRomanization = entry.lowerCaseRomanization?.ifEmpty { null },
            lowerCaseRomanization2 = entry.lowerCaseRomanization_Alt?.ifEmpty { null },
            lowerCaseHold = entry.lowerCaseHold?.ifEmpty { null },
            lowerCaseHoldHint = entry.lowerCaseHoldHint?.ifEmpty { null },
            backgroundColorIndexLowercase = entry.backgroundColorIndex_lowercase ?: 1,
            backgroundColorIndexLowercaseHold = entry.backgroundColorIndex_lowercase_Hold ?: 1,
            uppercase = entry.uppercase?.ifEmpty { entry.lowercase } ?: entry.lowercase,
            upperCaseRomanization = entry.upperCaseRomanization?.ifEmpty { null },
            upperCaseRomanization2 = entry.upperCaseRomanization_Alt?.ifEmpty { null },
            upperCaseHold = entry.upperCaseHold?.ifEmpty { null },
            upperCaseHoldHint = entry.upperCaseHoldHint?.ifEmpty { null },
            backgroundColorIndexUppercase = entry.backgroundColorIndex_uppercase ?: 1,
            backgroundColorIndexUppercaseHold = entry.backgroundColorIndex_uppercase_Hold ?: 1
        )
    }

    private fun processKey(key: KeyboardKey): KeyboardKey {
        val ctx = context

        if (current_writingSystem == WritingSystem.Kiril || current_writingSystem == WritingSystem.Latin) {
            if (key.name == "⸮" || key.name == "?") {
                return key.copy(
                    lowercase = "?", lowerCaseHold = "⸮", lowerCaseRomanization = "⸮", uppercase = "?", upperCaseHold = "⸮", upperCaseRomanization = "⸮"
                )
            }
        }

        if (current_writingSystem != WritingSystem.Bitik) return key

        var result = key

        if (ctx.keyboardVariant != BitikVariant.CLASSIC) {
            if (key.name == "y" && ctx.ajVariant != AJ_Letter_Variant.Ach) {
                return result.copy(lowercase = "𐰗", lowerCaseHold = null)
            }
            if (key.name == "j" && ctx.ajVariant != AJ_Letter_Variant.Ach) {
                return result.copy(lowercase = "𐰖", lowerCaseHold = "𐰳")
            }
        }

        if (key.name == "b" && ctx.ebVariant != EB_Letter_Variant.Default) {
            return result.copy(uppercase = "𐰋", upperCaseHold = "𐰌")
        }
        if (key.name == "n" && ctx.enVariant != EN_Letter_Variant.Default) {
            return result.copy(uppercase = "𐰥", upperCaseHold = "𐰤")
        }

        if (current_bitikDialect == BitikDialect.Altay) {
            if (key.name == "s" && ctx.asVariant == AS_Letter_Variant.Default) {
                return result.copy(lowercase = "𐰽", lowerCaseHold = "𐱂")
            }
            if (key.name == "k" && ctx.ekVariant == EK_Letter_Variant.Second) {
                return result.copy(uppercase = "𐰛", upperCaseHold = "𐰚")
            }
            if (ctx.keyboardVariant != BitikVariant.CLASSIC) {
                if (key.name == "ş" && ctx.eshVariant != ESH_Letter_Variant.Default) {
                    result = result.copy(uppercase = "𐰿", upperCaseHold = "𐱁")
                }
                return result
            }
        }

        if (current_bitikDialect == BitikDialect.Orkon) {
            if (key.name == "ñ" && ctx.angVariant == ANG_Letter_Variant.Off) {
                return result.copy(
                    lowercase = "𐰭", lowerCaseRomanization = "Ñ", lowerCaseRomanization2 = "ң", upperCaseRomanization = "Ñ", upperCaseRomanization2 = "ң"
                )
            }
            if (key.name == "s") return result.copy(lowercase = "𐰽", lowerCaseHold = "𐱂")
            if (key.name == "ş") {
                return result.copy(
                    lowercase = "𐱁",
                    lowerCaseHold = "𐱀",
                    lowerCaseRomanization = "Ş",
                    lowerCaseRomanization2 = "ш",
                    uppercase = "𐱁",
                    upperCaseHold = "𐰿",
                    upperCaseRomanization = "Ş",
                    upperCaseRomanization2 = "ш"
                )
            }
            if (key.name == "t") return result.copy(lowercase = "𐱃", lowerCaseHold = "𐱄")
            if (ctx.keyboardVariant == BitikVariant.CLASSIC && key.name == "oq, uq") {
                return result.copy(uppercase = "𐰰", upperCaseHold = "𐰝")
            }
        }

        return result
    }

    fun replaceWithBitikTranscription() {
        val ic = currentInputConnection ?: return
        val rawText = ic.getTextBeforeCursor(100, 0)?.toString().orEmpty()
        val extraSeparators = "·.,⸮⹁:;!?()[]{}\"'"
        val regex = "[^\\p{L}${Regex.escape(extraSeparators)}]+".toRegex()
        val lastWord = rawText.split(regex).lastOrNull().orEmpty()
        val transcribed = alphabetTranscriptionState.value
        if (lastWord.isNotEmpty() && transcribed.isNotEmpty()) {
            ic.deleteSurroundingText(lastWord.length, 0)
            ic.commitText(transcribed, 1)
            alphabetTranscriptionState.value = ""
        }
    }

    fun moveCursor(delta: Int) {
        val ic = currentInputConnection ?: return
        val extracted = ic.getExtractedText(ExtractedTextRequest(), 0) ?: return
        val cur = extracted.selectionEnd
        val newPos = (cur + delta).coerceIn(0, extracted.text?.length ?: 0)
        ic.setSelection(newPos, newPos)
    }

    private fun updateTranscription() {
        if (current_writingSystem != WritingSystem.Bitik) {
            updateAlphabetTranscription()
            transcriptionState.value = "" to ""
            return
        }

        if (!showTextTranscription) {
            transcriptionState.value = "" to ""
            return
        }

        val ic = currentInputConnection ?: run {
            transcriptionState.value = "" to ""
            return
        }

        try {
            val extraSeparators = "·.,⸮⹁:;!?()[]{}\"'"
            val rawText = ic.getTextBeforeCursor(100, 0)?.toString().orEmpty()
            val regex = "[^\\p{L}${Regex.escape(extraSeparators)}]+".toRegex()
            var lastWord = rawText.split(regex).lastOrNull().orEmpty()
            lastWord = TranscriptionProccessor().processTranscription_bitik(lastWord, this)

            onKeyPressed.text_Original = rawText
            onKeyPressed.inputText_LastWord = lastWord

            if (lastWord.isEmpty()) {
                onKeyPressed.InputText_Transcribed = ""
                onKeyPressed.InputText_Transcribed_Alt = ""
                transcriptionState.value = "" to ""
                return
            }

            val transcriber = jsTranscriber ?: JSTranscriber(this).also { jsTranscriber = it }
            val primary = transcriber.getTranscription(lastWord)
            val alt = transcriber.getTranscription_Alternative(lastWord)

            // If transcription failed, safeCall returns the original Bitik text unchanged.
            // Bitik (Old Turkic) codepoints are in the supplementary plane (U+10C00+),
            // encoded as surrogate pairs in UTF-16. Surrogate chars in the result mean
            // the text was never transcribed — suppress display to avoid rendering lone surrogates as "?".
            if (primary.isEmpty() || primary.any { it.isSurrogate() }) {
                transcriptionState.value = "" to ""
                return
            }

            onKeyPressed.InputText_Transcribed = primary
            onKeyPressed.InputText_Transcribed_Alt = alt

            transcriptionState.value = primary to alt
        } catch (_: Throwable) {
            transcriptionState.value = "" to ""
        }
    }

    private fun updateAlphabetTranscription() {
        if (!isBitikMode) {
            alphabetTranscriptionState.value = ""
            return
        }
        val ic = currentInputConnection ?: run {
            alphabetTranscriptionState.value = ""
            return
        }
        try {
            val extraSeparators = "·.,⸮⹁:;!?()[]{}\"'"
            val rawText = ic.getTextBeforeCursor(100, 0)?.toString().orEmpty()
            val regex = "[^\\p{L}${Regex.escape(extraSeparators)}]+".toRegex()
            val lastWord = rawText.split(regex).lastOrNull().orEmpty()
            if (lastWord.isEmpty()) {
                alphabetTranscriptionState.value = ""
                return
            }
            val transcriber = jsTranscriber_Alphabet ?: JSTranscriber_Alphabet(this).also { jsTranscriber_Alphabet = it }
            val transcribed = transcriber.getTranscription(lastWord).ifEmpty { "" }
            alphabetTranscriptionState.value = TranscriptionProccessor().processTranscription_alphabet(transcribed, this)
        } catch (_: Throwable) {
            alphabetTranscriptionState.value = ""
        }
    }

    private fun applyInsetsNowAndOnChange(view: ComposeView) {
        val solution = context.navBarPaddingSolution
        if (solution == NavBarPaddingSolution.Solution_Enable_Off) return

        if (solution == NavBarPaddingSolution.Solution_AllEnabled || solution == NavBarPaddingSolution.Solution_Enable_1) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
                val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                if (bottomPadding == null) bottomPadding = nav.bottom + 25
                bottomPaddingState.value = bottomPadding!!
                insets
            }
        }

        view.doOnAttach {
            // Request insets after attach so the listener above fires with real values
            ViewCompat.requestApplyInsets(it)

            if (solution == NavBarPaddingSolution.Solution_AllEnabled || solution == NavBarPaddingSolution.Solution_Enable_2) {
                val rootInsets = ViewCompat.getRootWindowInsets(it) ?: return@doOnAttach
                val nav = rootInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
                if (bottomPadding == null) bottomPadding = nav.bottom + 25
                bottomPaddingState.value = bottomPadding!!
            }
        }
    }

    fun handleKeyPress(key: String, ic: InputConnection, service: InputMethodService) {
        val alwaysSys = setOf("?", "⸮", "!")
        val isSys = key.startsWith("sys") || key in alwaysSys
        var toPaste = key.removePrefix("sys")

        if (current_writingSystem == WritingSystem.Bitik && isSys) {
            toPaste = toPaste.replace("  ", " ").replace("?", "⸮ ").replace("!", "! ").replace(".", "·").replace(",", "⹁")

            if (toPaste == " ") {
                toPaste = when (context.wordSeparator) {
                    WordSeparator.NoSpace -> "⁚"
                    WordSeparator.SpaceBefore -> "⁚ "
                    WordSeparator.ArroundSpace -> " ⁚ "
                    WordSeparator.Off -> " "
                }
            }

            val original = onKeyPressed.text_Original
            val transcribed = onKeyPressed.InputText_Transcribed
            if (original.isNotEmpty()) {
                repeat(original.length) { ic.deleteSurroundingText(1, 0) }
            }
            ic.commitText(transcribed + toPaste, 1)

            onKeyPressed.InputText_Transcribed = ""
            onKeyPressed.text_Original = ""
            onKeyPressed.InputText_Transcribed_Alt = ""
        } else {
            when (key) {
                "language" -> {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            service.switchToNextInputMethod(false)
                        } else {
                            val imm = service.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showInputMethodPicker()
                        }
                    } catch (ex: Throwable) {
                        print(ex)
                    }
                }

                "delete" -> {
                    val before = ic.getTextBeforeCursor(2, 0)
                    val deleteCount = when {
                        before.isNullOrEmpty() -> 1
                        before.length >= 2 && Character.isSurrogatePair(before[before.length - 2], before[before.length - 1]) -> 2
                        else -> 1
                    }
                    ic.deleteSurroundingText(deleteCount, 0)
                    // Also clean up any orphaned surrogate left after cursor
                    val after = ic.getTextAfterCursor(1, 0)
                    if (!after.isNullOrEmpty() && after[0].isSurrogate()) {
                        ic.deleteSurroundingText(1, 0)
                    }
                }

                "\n", "Return" -> {
                    ic.commitText("\n", 1)
                }

                else -> {
                    ic.commitText(toPaste, 1)
                }
            }
        }

        (service.window?.window?.decorView as? View)?.let { v ->
            v.playSoundEffect(SoundEffectConstants.CLICK)
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
}
