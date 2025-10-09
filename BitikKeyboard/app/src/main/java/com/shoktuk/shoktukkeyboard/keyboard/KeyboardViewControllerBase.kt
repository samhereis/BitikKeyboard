package com.shoktuk.shoktukkeyboard.keyboard

import KeyboardViewLifecycleOwner
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem

enum class KeyboardState(val id: String) {
    Main("Main"), Symbols("Symbols"), Emojis("Emojis"), SavedStrings("SavedStrings");
}

object onKeyPressed {
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

class KeyboardViewControllerBase() : InputMethodService() {
    companion object {
        lateinit var context: KeyboardViewControllerBase

        var current_bitikVariant: BitikVariant = BitikVariant.Modern
        var current_writingSystem: WritingSystem = WritingSystem.Bitik
        var current_textTranscription: TextTranscription = TextTranscription.On

        var keyboardMode: KeyboardState = KeyboardState.Main
        val showTextTranscription: Boolean get() = current_textTranscription == TextTranscription.On
        var fontScale: TextStyle = KeyboardStyle.buttonFont

        var maxRowElementsCount: Double = 10.25
        var autoDisableShift: Boolean = false
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onWindowShown() {
        super.onWindowShown()

        context = this
        keyboardViewLifecycleOwner.onResume()
    }

    private val keyboardViewLifecycleOwner = KeyboardViewLifecycleOwner()

    override fun onCreate() {
        super.onCreate()
        keyboardViewLifecycleOwner.onCreate()
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this).apply {
            setContent {
                StandardKeyboardView(
                    rowsModel = KeyboardRowsModel(), keyboardState = remember { mutableStateOf(keyboardMode) }, onKeyPress = {

                    }, modifier = Modifier
                )
            }
        }

        // Attach the LifecycleOwner to the decor view of the keyboard window
        window?.window?.decorView?.let { decorView ->
            keyboardViewLifecycleOwner.attachToDecorView(decorView)
        }

        return composeView
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        keyboardViewLifecycleOwner.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardViewLifecycleOwner.onDestroy()
    }
}