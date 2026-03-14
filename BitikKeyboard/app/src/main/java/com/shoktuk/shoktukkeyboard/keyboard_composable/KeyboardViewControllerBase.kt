package com.shoktuk.shoktukkeyboard.keyboard

import JSTranscriber
import KeyboardViewLifecycleOwner
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.NavBarPaddingSolution
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.navBarPaddingSolution
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
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

class KeyboardViewControllerBase() : InputMethodService() {
    companion object {
        lateinit var context: KeyboardViewControllerBase

        var keyboardRowModel = KeyboardRowsModel()

        var current_bitikVariant: BitikVariant = BitikVariant.Modern
        var current_writingSystem: WritingSystem = WritingSystem.Bitik
        var current_textTranscription: TextTranscription = TextTranscription.On

        var keyboardMode: KeyboardState = KeyboardState.Main
        val showTextTranscription: Boolean get() = current_textTranscription == TextTranscription.On
        var fontScale: TextStyle = KeyboardStyle.buttonFont

        var bottomPadding: Int? = null

        var maxRowElementsCount: Double = 11.5
        var autoDisableShift: Boolean = false

        /** Reactive transcription state read by TopRowView. Pair(primary, alternative). */
        val transcriptionState = mutableStateOf("" to "")
    }

    private val keyboardViewLifecycleOwner = KeyboardViewLifecycleOwner()

    // Lazily created transcriber; re-created if null (e.g. after settings change).
    private var jsTranscriber: JSTranscriber? = null

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onWindowShown() {
        super.onWindowShown()
        keyboardViewLifecycleOwner.onResume()
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        keyboardViewLifecycleOwner.onCreate()
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this).apply {
            setContent {
                ShoktukKeyboardTheme {
                    StandardKeyboardView(
                        rowsModel = keyboardRowModel,
                        keyboardState = remember { mutableStateOf(keyboardMode) },
                        onKeyPress = { key ->
                            currentInputConnection?.let { ic ->
                                handleKeyPress(key, ic, this@KeyboardViewControllerBase)
                            }
                            updateTranscription()
                        }
                    )
                }
            }
        }
        applyInsetsNowAndOnChange(composeView)
        window?.window?.decorView?.let { keyboardViewLifecycleOwner.attachToDecorView(it) }
        return composeView
    }

    private fun applyInsetsNowAndOnChange(view: ComposeView) {
        if (context.navBarPaddingSolution == NavBarPaddingSolution.Solution_Enable_Off) {
            return
        }

        if (context.navBarPaddingSolution == NavBarPaddingSolution.Solution_AllEnabled || context.navBarPaddingSolution == NavBarPaddingSolution.Solution_Enable_1) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                if (bottomPadding == null) bottomPadding = nav.bottom
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottomPadding!!)
                insets
            }
        }

        if (context.navBarPaddingSolution == NavBarPaddingSolution.Solution_AllEnabled || context.navBarPaddingSolution == NavBarPaddingSolution.Solution_Enable_2) {
            view.doOnAttach {
                val rootInsets = ViewCompat.getRootWindowInsets(it) ?: return@doOnAttach
                val nav = rootInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
                if (bottomPadding == null) bottomPadding = nav.bottom
                it.setPadding(it.paddingLeft, it.paddingTop, it.paddingRight, bottomPadding!!)
            }
        }

        ViewCompat.requestApplyInsets(view)
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        keyboardViewLifecycleOwner.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        jsTranscriber?.close()
        jsTranscriber = null
        keyboardViewLifecycleOwner.onDestroy()
    }

    private fun updateTranscription() {
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
            val primary = transcriber.getTranscription(lastWord).ifEmpty { lastWord }
            val alt = transcriber.getTranscription_Alternative(lastWord).ifEmpty { lastWord }

            onKeyPressed.InputText_Transcribed = primary
            onKeyPressed.InputText_Transcribed_Alt = alt

            transcriptionState.value = primary to alt
        } catch (_: Throwable) {
            transcriptionState.value = "" to ""
        }
    }

    fun handleKeyPress(
        key: String, ic: InputConnection, service: InputMethodService
    ) {
        val alwaysSys = setOf("?", "⸮", "!")
        val isSys = key.startsWith("sys") || key in alwaysSys
        var toPaste = key.removePrefix("sys")

        if (KeyboardViewControllerBase.current_writingSystem == WritingSystem.Bitik && isSys) {
            toPaste =
                toPaste.replace("  ", " ").replace("?", "⸮ ").replace("!", "! ").replace(".", "·").replace(",", "⹁")

            if (toPaste == " ") {
                toPaste = when (KeyboardViewControllerBase.context.wordSeparator) {
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

            onKeyPressed.invoke(ic, "", false)
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
                    ic.deleteSurroundingText(1, 0)
                    onKeyPressed.invoke(ic, "", false)
                }

                "\n", "Return" -> {
                    ic.commitText("\n", 1)
                }

                else -> {
                    ic.commitText(toPaste, 1)
                    onKeyPressed.invoke(ic, "", false)
                }
            }
        }

        (service.window?.window?.decorView as? View)?.let { v ->
            v.playSoundEffect(SoundEffectConstants.CLICK)
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
}
