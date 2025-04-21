package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager

/**
 * The actual IME service used at runtime.
 * It builds and reloads the keyboard view whenever the user toggles caps, mode, or language.
 */
class MyKeyboardService : InputMethodService() {

    // Tracks whether caps lock is engaged
    var isCaps: Boolean = false

    // Tracks the current input mode ("letters", "numbers", etc.)
    var currentMode: String = "letters"

    // Tracks the current language key ("enesay" or "orhon")
    private var currentLanguage: String = "enesay"

    // Cache of the loaded layout so we can rebuild quickly
    private var currentLayout: KeyboardLayout? = null

    /**
     * Called by the system to create your keyboard UI.
     */
    override fun onCreateInputView(): View {
        // Load the layout based on mode + language
        currentLayout = KeyboardLayoutLoader
            .loadKeyboardLayout(this, currentMode, getLanguage())

        // Build and return the View
        return KeyboardViewBuilder.buildKeyboardView(
            service       = this,
            layout        = currentLayout!!,
            isCaps        = isCaps,
            onCapsChange  = { newCaps ->
                isCaps = newCaps
                reloadKeyboard()
            },
            onModeChange  = { newMode ->
                currentMode = newMode
                reloadKeyboard()
            },
            onLangChange  = {
                changeLanguage()
            }
        )
    }

    /**
     * Rebuilds the keyboard view when state changes.
     */
    fun reloadKeyboard() {
        currentLayout = KeyboardLayoutLoader
            .loadKeyboardLayout(this, currentMode, getLanguage())

        setInputView(
            KeyboardViewBuilder.buildKeyboardView(
                service       = this,
                layout        = currentLayout!!,
                isCaps        = isCaps,
                onCapsChange  = { isCaps = it; reloadKeyboard() },
                onModeChange  = { newMode -> currentMode = newMode; reloadKeyboard() },
                onLangChange  = { changeLanguage() }
            )
        )
    }

    /**
     * Toggles between "enesay" and "orhon", then reloads.
     */
    fun changeLanguage() {
        currentLanguage = if (currentLanguage == "enesay") {
            "orhon"
        } else {
            "enesay"
        }
        reloadKeyboard()
    }

    /**
     * Returns the language code, appending "_old" if the user has chosen the classic variant.
     */
    fun getLanguage(): String {
        return if (SettingsManager.getKeyboardVariant(this) == KeyboardVariant.CLASSIC) {
            "${currentLanguage}_old"
        } else {
            currentLanguage
        }
    }
}
