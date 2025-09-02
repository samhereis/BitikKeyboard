package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.ViewCompat // changed: used for insets
import androidx.core.view.WindowInsetsCompat // changed: used for insets
import androidx.core.view.doOnAttach // changed: apply insets immediately after attach
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.KeyboardAlphabet
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager
import com.shoktuk.shoktukkeyboard.project.data.TamgaTranscription
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

class MyKeyboardService : InputMethodService() {
    companion object {
        var backgroundColor: Color = Color.DarkGray;
        var textColor: Color = Color.DarkGray;

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

        lateinit var context : MyKeyboardService
    }

    private var currentLayout: KeyboardLayout? = null

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        reloadKeyboard()
    }

    override fun onCreateInputView(): View {
        context = this

        currentAlphabet = SettingsManager.getBitikAlphabet(this).id
        currentLayout = KeyboardLayoutLoader.loadKeyboardLayout(this, currentMode, getLanguage())
        currentVariant = SettingsManager.getKeyboardVariant(this)
        letterTranscription = SettingsManager.getLeterTranscription(this)
        textTranscription = SettingsManager.getTextTranscription(this)

        val typedValue = TypedValue()
        val theme = this.theme

        theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        backgroundColor = Color(typedValue.data)

        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        textColor = Color(typedValue.data)

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
            reloadKeyboard()
        })

        applyInsetsNowAndOnChange(root) // changed: ensure bottom padding right away and on future inset dispatches
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
            reloadKeyboard()
        })

        applyInsetsNowAndOnChange(root) // changed: re-apply insets on the newly built view
        setInputView(root)
    }

    private fun applyInsetsNowAndOnChange(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val sys = insets.getInsets(
                WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemGestures()
            )
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, sys.bottom / 2)
            insets
        }
        view.doOnAttach {
            val rootInsets = ViewCompat.getRootWindowInsets(it)
            val sys = rootInsets?.getInsets(
                WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemGestures()
            )
            val bottom = sys?.bottom ?: 0
            it.setPadding(it.paddingLeft, it.paddingTop, it.paddingRight, bottom / 2)
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