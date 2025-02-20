package com.shoktuk.bitikkeyboard

import android.inputmethodservice.InputMethodService
import android.view.View

class MyKeyboardService : InputMethodService() {
    var isCaps: Boolean = false
    // Modes: "letters" or "symbols"
    private var currentMode: String = "letters"
    private var currentLayout: KeyboardLayout? = null

    override fun onCreateInputView(): View {
        currentLayout = KeyboardLayoutLoader.loadKeyboardLayout(this, currentMode)
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
            }
        )
    }

    private fun reloadKeyboard() {
        currentLayout = KeyboardLayoutLoader.loadKeyboardLayout(this, currentMode)
        setInputView(
            KeyboardViewBuilder.buildKeyboardView(
                service = this,
                layout = currentLayout!!,
                isCaps = isCaps,
                onCapsChange = { isCaps = it; reloadKeyboard() },
                onModeChange = { newMode -> currentMode = newMode; reloadKeyboard() }
            )
        )
    }
}
