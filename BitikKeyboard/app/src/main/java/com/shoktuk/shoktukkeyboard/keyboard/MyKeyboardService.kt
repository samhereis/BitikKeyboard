package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.KeyboardAlphabet
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager
import com.shoktuk.shoktukkeyboard.project.data.TamgaTranscription
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

class MyKeyboardService : InputMethodService() {
    companion object {
        var currentAlphabet: String = "bitik"
        var currentMode: String = "letters"
        var currentLanguage: String = "enesay"

        var currentVariant: KeyboardVariant = KeyboardVariant.CLASSIC
        var letterTranscription: TamgaTranscription = TamgaTranscription.On
        var textTranscription: TextTranscription = TextTranscription.On

        var isCaps: Boolean = false

        val buttonMargin: Int = KeyboardTheme.KEY_MARGIN_DP

        val isClassic: Boolean get() = currentVariant == KeyboardVariant.CLASSIC
        val isTamga: Boolean get() = currentMode == "letters"
        val showLetterTranscription: Boolean get() = letterTranscription == TamgaTranscription.On
        val showTextTranscription: Boolean get() = textTranscription == TextTranscription.On

        lateinit var context: MyKeyboardService
        var bottomPadding: Int? = null
    }

    private var currentLayout: KeyboardLayout? = null

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onWindowShown() {
        super.onWindowShown()

        currentAlphabet = SettingsManager.getBitikAlphabet(this).id
        currentVariant = SettingsManager.getKeyboardVariant(this)
        letterTranscription = SettingsManager.getLeterTranscription(this)
        textTranscription = SettingsManager.getTextTranscription(this)

        if (currentLayout != null) {
            reloadKeyboard()
        }
    }

    override fun onCreateInputView(): View {
        context = this

        currentAlphabet = SettingsManager.getBitikAlphabet(this).id
        currentVariant = SettingsManager.getKeyboardVariant(this)
        letterTranscription = SettingsManager.getLeterTranscription(this)
        textTranscription = SettingsManager.getTextTranscription(this)
        updateDialect()

        currentLayout = KeyboardLayoutLoader.loadKeyboardLayout(this, currentMode, getLanguage())

        val root = KeyboardViewBuilder.buildKeyboardView(service = this, layout = currentLayout!!, onCapsChange = { newCaps ->
            isCaps = newCaps
            reloadKeyboard()
        }, onModeChange = {
            currentMode = if (currentMode == "symbols") "letters" else "symbols"
            reloadKeyboard()
        }, onAlphabetChange = {
            if (currentAlphabet == "bitik") {
                SettingsManager.setBitikAlphabet(this, KeyboardAlphabet.Latin)
            } else {
                SettingsManager.setBitikAlphabet(this, KeyboardAlphabet.Bitik)
            }
            currentAlphabet = SettingsManager.getBitikAlphabet(this).id
            currentMode = "letter"
            reloadKeyboard()
        })

        applyInsetsNowAndOnChange(root)
        return root
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

        val root = KeyboardViewBuilder.buildKeyboardView(service = this, layout = currentLayout!!, onCapsChange = { isCaps = it; reloadKeyboard() }, onModeChange = {
            currentMode = if (currentMode == "symbols") "letters" else "symbols"
            reloadKeyboard()
        }, onAlphabetChange = {
            if (currentAlphabet == "bitik") {
                SettingsManager.setBitikAlphabet(this, KeyboardAlphabet.Latin)
            } else {
                SettingsManager.setBitikAlphabet(this, KeyboardAlphabet.Bitik)
            }
            currentAlphabet = SettingsManager.getBitikAlphabet(this).id
            currentMode = "letter"
            reloadKeyboard()
        })

        applyInsetsNowAndOnChange(root)
        setInputView(root)
    }

    private fun applyInsetsNowAndOnChange(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            if (bottomPadding == null) {
                bottomPadding = nav.bottom + 25
            }

            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottomPadding!!)
            insets // don't consume
        }

        view.doOnAttach {
            val rootInsets = ViewCompat.getRootWindowInsets(it) ?: return@doOnAttach
            val nav = rootInsets.getInsets(WindowInsetsCompat.Type.navigationBars())

            if (bottomPadding == null) {
                bottomPadding = nav.bottom + 25
            }

            it.setPadding(it.paddingLeft, it.paddingTop, it.paddingRight, bottomPadding!!)
        }

        ViewCompat.requestApplyInsets(view)
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