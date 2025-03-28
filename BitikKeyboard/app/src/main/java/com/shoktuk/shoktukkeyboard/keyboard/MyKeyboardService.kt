package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View

class MyKeyboardService : InputMethodService() {
    var isCaps: Boolean = false

    private var currentMode: String = "letters"
    private var currentLanguage: String = "enesay"
    private var currentLayout: KeyboardLayout? = null

    override fun onCreateInputView(): View {
        currentLayout = KeyboardLayoutLoader.loadKeyboardLayout(this, currentMode, currentLanguage)
        return KeyboardViewBuilder.buildKeyboardView(
            service = this,
            layout = currentLayout!!,
            isCaps = isCaps,
            onCapsChange = { newCaps ->
                isCaps = newCaps
                reloadKeyboard()
            },
            onModeChange = { newMode ->
                currentMode = newMode
                reloadKeyboard()
            },
            onLangChange = { changeLanguage() }
        )
    }

    private fun reloadKeyboard() {
        currentLayout = KeyboardLayoutLoader.loadKeyboardLayout(this, currentMode, currentLanguage)
        setInputView(
            KeyboardViewBuilder.buildKeyboardView(
                service = this,
                layout = currentLayout!!,
                isCaps = isCaps,
                onCapsChange = { isCaps = it; reloadKeyboard() },
                onModeChange = { newMode -> currentMode = newMode; reloadKeyboard() },
                onLangChange = { changeLanguage() }
            )
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
}
