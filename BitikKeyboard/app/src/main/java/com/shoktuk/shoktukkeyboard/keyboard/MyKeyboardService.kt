package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.KeyboardAlphabet
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager

class MyKeyboardService : InputMethodService() {
    companion object {
        var currentAlphabet: String = "bitik"
        var currentMode: String = "letters"
        var currentLanguage: String = "enesay"
        var isCaps: Boolean = false
    }

    private var currentLayout: KeyboardLayout? = null

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onCreateInputView(): View {
        currentAlphabet = SettingsManager.getBitikAlphabet(this).id

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
        }, onAlphabetChange = { ->
            if (currentAlphabet == "bitik") {
                SettingsManager.setBitikAlphabet(this, KeyboardAlphabet.Latin)
            } else {
                SettingsManager.setBitikAlphabet(this, KeyboardAlphabet.Bitik)
            };
            currentAlphabet = SettingsManager.getBitikAlphabet(this).id
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
                },
                onAlphabetChange = { ->
                    if (currentAlphabet == "bitik") {
                        SettingsManager.setBitikAlphabet(this, KeyboardAlphabet.Latin)
                    } else {
                        SettingsManager.setBitikAlphabet(this, KeyboardAlphabet.Bitik)
                    };
                    currentAlphabet = SettingsManager.getBitikAlphabet(this).id
                    reloadKeyboard()
                })
        )
    }

    fun getLanguage(): String {
        if (currentAlphabet != "bitik") {
            return currentAlphabet
        }

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