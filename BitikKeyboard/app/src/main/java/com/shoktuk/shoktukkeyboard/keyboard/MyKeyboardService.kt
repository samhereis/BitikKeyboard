package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager

class MyKeyboardService : InputMethodService() {

    var isCaps: Boolean = false
    var currentMode: String = "letters"
    private var currentLanguage: String = "enesay"
    private var currentLayout: KeyboardLayout? = null

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onCreateInputView(): View {
        currentLayout = KeyboardLayoutLoader.loadKeyboardLayout(this, currentMode, getLanguage())

        return KeyboardViewBuilder.buildKeyboardView(service = this, layout = currentLayout!!, isCaps = isCaps, currentMode == "letters", onCapsChange = { newCaps ->
            isCaps = newCaps
            reloadKeyboard()
        }, onModeChange = { newMode ->
            if (currentMode == "symbols") {
                currentMode = "letters"
            } else {
                currentMode = "symbols"
            };
            reloadKeyboard()
        })
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        TopRowBuilder_Old.onTypedListener?.invoke()
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd
        )
        TopRowBuilder_Old.onTypedListener?.invoke()
    }

    fun reloadKeyboard() {
        updateDialect()

        currentLayout = KeyboardLayoutLoader.loadKeyboardLayout(this, currentMode, getLanguage())

        setInputView(
            KeyboardViewBuilder.buildKeyboardView(
                service = this,
                layout = currentLayout!!,
                isCaps = isCaps,
                currentMode == "letters",
                onCapsChange = { isCaps = it; reloadKeyboard() },
                onModeChange = { newMode ->
                    if (currentMode == "symbols") {
                        currentMode = "letters"
                    } else {
                        currentMode = "symbols"
                    };
                    reloadKeyboard()
                })
        )
    }

    fun getLanguage(): String {
        return if (SettingsManager.getKeyboardVariant(this) == KeyboardVariant.CLASSIC) {
            "${currentLanguage}_old"
        } else {
            currentLanguage
        }
    }

    fun updateDialect() {
        if (SettingsManager.getBitikDialect(this) == BitikDialect.Orkon) {
            currentLanguage = "orhon"
        } else {
            currentLanguage = "enesay"
        }
    }
}