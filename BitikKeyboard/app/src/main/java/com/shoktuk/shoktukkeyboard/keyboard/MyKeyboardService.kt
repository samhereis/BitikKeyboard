package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager

/**
 * The actual IME service used at runtime.
 * It builds and reloads the keyboard view whenever the user toggles caps, mode, or language.
 */
class MyKeyboardService : InputMethodService() {

    // Tracks whether caps lock is engaged
    var isCaps: Boolean = false

    // Tracks the current input mode ("letters", "symbols", etc.)
    var currentMode: String = "letters"

    // Tracks the current language key ("enesay" or "orhon")
    private var currentLanguage: String = "enesay"

    // Cache of the loaded layout for fast rebuilds
    private var currentLayout: KeyboardLayout? = null

    /** Prevent full-screen extract mode on older Android versions */
    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    /** Create and return the keyboard view hierarchy */
    override fun onCreateInputView(): View {
        currentLayout = KeyboardLayoutLoader
            .loadKeyboardLayout(this, currentMode, getLanguage())

        return KeyboardViewBuilder.buildKeyboardView(
            service      = this,
            layout       = currentLayout!!,
            isCaps       = isCaps,
            onCapsChange = { newCaps ->
                isCaps = newCaps
                reloadKeyboard()
            },
            onModeChange = { newMode ->
                currentMode = newMode
                reloadKeyboard()
            },
            onLangChange = {
                changeLanguage()
            }
        )
    }

    /**
     * Called right after the input view is shown. Ensures the top row
     * is rendered once the view is attached.
     */
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        TopRowBuilder_Old.onTypedListener?.invoke()
    }

    /**
     * Called on every selection or cursor movement. Triggers an update
     * of the top row so it stays in sync with all text changes.
     */
    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd,
            newSelStart, newSelEnd,
            candidatesStart, candidatesEnd
        )
        TopRowBuilder_Old.onTypedListener?.invoke()
    }

    /** Reloads the keyboard view with the current mode/language/caps state */
    fun reloadKeyboard() {
        currentLayout = KeyboardLayoutLoader
            .loadKeyboardLayout(this, currentMode, getLanguage())

        setInputView(
            KeyboardViewBuilder.buildKeyboardView(
                service      = this,
                layout       = currentLayout!!,
                isCaps       = isCaps,
                onCapsChange = { isCaps = it; reloadKeyboard() },
                onModeChange = { newMode -> currentMode = newMode; reloadKeyboard() },
                onLangChange = { changeLanguage() }
            )
        )
    }

    /** Toggles between "enesay" and "orhon", then refreshes */
    fun changeLanguage() {
        currentLanguage = if (currentLanguage == "enesay") {
            "orhon"
        } else {
            "enesay"
        }
        reloadKeyboard()
    }

    /**
     * Returns the language code, appending "_old" if the user has
     * chosen the classic variant in settings.
     */
    fun getLanguage(): String {
        return if (SettingsManager.getKeyboardVariant(this) == KeyboardVariant.CLASSIC) {
            "${currentLanguage}_old"
        } else {
            currentLanguage
        }
    }
}