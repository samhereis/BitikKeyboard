package com.shoktuk.shoktukkeyboard.keyboard

import Haptics
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.view.HapticFeedbackConstants
import KeyboardViewLifecycleOwner
import LocalizationManager
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import com.shoktuk.shoktukkeyboard.emoji.EmojisData
import com.shoktuk.shoktukkeyboard.emoji.EmojisViewBuilder
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.InputText_Transcribed
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.inputText_LastWord
import com.shoktuk.shoktukkeyboard.project.data.Arabic_Status
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Variant
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import com.shoktuk.shoktukkeyboard.project.data.NavBarPaddingSolution
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.arabicStatus
import com.shoktuk.shoktukkeyboard.project.data.BitikTranscriptionAlphabets
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikTranscriptionAlphabets
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bottomOffset
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
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import com.whl.quickjs.android.QuickJSLoader

enum class KeyboardMode(val id: String) {
    Main("Main"), Symbols("Symbols"), Emojis("Emojis"), SavedStrings("SavedStrings"), Settings("Settings"), Resize("Resize");
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

object onSettingChanged {
    private val listeners = mutableListOf<() -> Unit>()

    fun addListener(listener: () -> Unit) {
        listeners.clear()
        listeners.add(listener)
    }

    fun invoke() {
        MyKeyboardService.bottomPadding = null
        listeners.forEach { it() }
    }

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val runListeners = Runnable { listeners.forEach { it() } }

    /**
     * For a slider, which reports every small move: the keyboard is rebuilt once, a moment after the value stops changing,
     * instead of on every move (a rebuild on the same thread that drags the slider made it stutter).
     */
    fun invokeDebounced(delayMs: Long = 150) {
        MyKeyboardService.bottomPadding = null
        handler.removeCallbacks(runListeners)
        handler.postDelayed(runListeners, delayMs)
    }
}
