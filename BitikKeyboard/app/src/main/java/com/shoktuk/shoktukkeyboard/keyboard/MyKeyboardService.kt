package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager

class MyKeyboardService : InputMethodService() {
    var isCaps: Boolean = false

    private var currentMode: String = "letters"
    private var currentLanguage: String = "enesay"
    private var currentLayout: KeyboardLayout? = null

    override fun onCreateInputView(): View {
        currentLayout = KeyboardLayoutLoader.loadKeyboardLayout(this, currentMode, getLanguage())
        return KeyboardViewBuilder.buildKeyboardView(service = this, layout = currentLayout!!, isCaps = isCaps, onCapsChange = { newCaps ->
            isCaps = newCaps
            reloadKeyboard()
        }, onModeChange = { newMode ->
            currentMode = newMode
            reloadKeyboard()
        }, onLangChange = { changeLanguage() })
    }

    private fun reloadKeyboard() {
        currentLayout = KeyboardLayoutLoader.loadKeyboardLayout(this, currentMode, getLanguage())
        setInputView(
            KeyboardViewBuilder.buildKeyboardView(
                service = this,
                layout = currentLayout!!,
                isCaps = isCaps,
                onCapsChange = { isCaps = it; reloadKeyboard() },
                onModeChange = { newMode -> currentMode = newMode; reloadKeyboard() },
                onLangChange = { changeLanguage() })
        )
    }

    private fun changeLanguage() {
        if (currentLanguage == "enesay") {
            currentLanguage = "orhon"
        } else {
            currentLanguage = "enesay"
        }

        reloadKeyboard();
    }


    private fun getLanguage(): String {
        var currentLanguageFromSettings = currentLanguage;
        if (SettingsManager.getKeyboardVariant(this) == KeyboardVariant.CLASSIC) {
            return currentLanguageFromSettings + "_old"
        }

        return currentLanguageFromSettings
    }
}
