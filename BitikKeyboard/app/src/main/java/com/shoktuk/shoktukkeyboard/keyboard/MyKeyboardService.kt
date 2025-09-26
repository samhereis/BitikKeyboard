package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import com.shoktuk.shoktukkeyboard.emoji.EmojisData
import com.shoktuk.shoktukkeyboard.emoji.EmojisViewBuilder
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.kirilisaStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.letterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.textTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.writingSystem
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

enum class KeyboardMode(val id: String) {
    Main("Main"), Symbols("Symbols"), Emojis("Emojis"), SavedStrings("SavedStrings");
}

class MyKeyboardService : InputMethodService() {
    companion object {
        lateinit var context: MyKeyboardService

        var current_bitikVariant: BitikVariant = BitikVariant.CLASSIC
        var current_bitikDialect: BitikDialect = BitikDialect.Altay
        var current_writingSystem: WritingSystem = WritingSystem.Bitik
        var current_letterTranscription: LetterTranscription = LetterTranscription.On
        var current_textTranscription: TextTranscription = TextTranscription.On

        var keyboardMode: KeyboardMode = KeyboardMode.Main

        var isCaps: Boolean = false

        val buttonMargin: Int = KeyboardTheme.KEY_MARGIN_DP

        val isClassic: Boolean get() = current_bitikVariant == BitikVariant.CLASSIC
        val isTamga: Boolean get() = keyboardMode == KeyboardMode.Main
        val showLetterTranscription: Boolean get() = current_letterTranscription == LetterTranscription.On
        val showTextTranscription: Boolean get() = current_textTranscription == TextTranscription.On

        var bottomPadding: Int? = null
        var maxButtonInOneRow: Int = 10
    }

    private var currentLayout: KeyboardLayout? = null

    var root: LinearLayout? = null
    var root_ShiftOn: LinearLayout? = null
    var root_ShiftOff: LinearLayout? = null
    var root_Symbols: LinearLayout? = null
    var root_Emojis: LinearLayout? = null
    var topBar: View? = null
    var topBar_modern: View? = null
    var topBar_old: View? = null
    var topBar_noTR: View? = null
    var container: LinearLayout? = null

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onWindowShown() {
        super.onWindowShown()

        context = this
        isCaps = false
        keyboardMode = KeyboardMode.Main

        if (currentLayout != null) {
            reloadKeyboard()
        }
    }

    override fun onCreateInputView(): View? {
        context = this
        isCaps = false
        keyboardMode = KeyboardMode.Main

        reloadKeyboard()

        if (root != null) {
            applyInsetsNowAndOnChange(root!!)
        }

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

    fun applyKeyboard() {
        context = this

        if (keyboardMode == KeyboardMode.Symbols) {
            root = root_Symbols
        } else if (keyboardMode == KeyboardMode.Emojis) {
            root = makeEmojiView()
        } else if (keyboardMode == KeyboardMode.SavedStrings) {
            root = makeSavablesView()
        } else {
            if (isCaps) {
                root = root_ShiftOn
            } else {
                root = root_ShiftOff
            }

            if (keyboardMode == KeyboardMode.Main) {
            }
        }

        ensureDetached(topBar)
        root?.addView(topBar, 0)

        root?.let {
            applyInsetsNowAndOnChange(it)
            setInputView(it)
        }
    }

    private fun ensureDetached(view: View?) {
        val parent = view?.parent as? ViewGroup
        parent?.removeView(view)
    }

    fun reloadKeyboard() {
        current_bitikVariant = context.keyboardVariant
        current_bitikDialect = context.bitikDialect
        current_writingSystem = context.writingSystem
        current_letterTranscription = context.letterTranscription
        current_textTranscription = context.textTranscription

        maxButtonInOneRow = 10
        if (current_writingSystem == WritingSystem.Kiril) {
            maxButtonInOneRow = 11
            current_letterTranscription = LetterTranscription.On
        } else if (current_writingSystem == WritingSystem.Latin) {
            current_letterTranscription = LetterTranscription.On
        }

        fillRoots()
        applyKeyboard()
    }

    private fun onModeChange(mode: KeyboardMode) {
        if (mode == KeyboardMode.Emojis) {
            keyboardMode = KeyboardMode.Emojis
        } else {
            keyboardMode = mode
        }
        applyKeyboard()
    }

    private fun onAlphabetChange() {
        if (context.latinStatus == Latin_Status.Off && context.kirilisaStatus == Kirilisa_Status.Off) {
            context.writingSystem = WritingSystem.Bitik
            keyboardMode = KeyboardMode.Emojis
        } else {
            val availableLanguages = mutableListOf<WritingSystem>(WritingSystem.Bitik)
            if (context.latinStatus == Latin_Status.On) availableLanguages.add(WritingSystem.Latin)
            if (context.kirilisaStatus == Kirilisa_Status.On) availableLanguages.add(WritingSystem.Kiril)

            var currentLanguageIndex = availableLanguages.indexOf(context.writingSystem)

            currentLanguageIndex++
            if (currentLanguageIndex >= availableLanguages.size) {
                currentLanguageIndex = 0
            }
            context.writingSystem = availableLanguages[currentLanguageIndex]

            keyboardMode = KeyboardMode.Main
        }
        reloadKeyboard()
    }

    private fun fillRoots() {
        currentLayout = KeyboardLayoutLoader.loadKeyboardLayout(this, keyboardMode, getLanguage())
        var systemKeybHeight = (KeyboardTheme.getButtonHeight() / 1.5f).toInt()

        if (MyKeyboardService.current_writingSystem == WritingSystem.Bitik) {
            if (context.keyboardVariant != BitikVariant.SAMAGAN) {
                if (topBar_old == null) {
                    topBar_old = TopRowBuilder_Old.createTopRow(this, systemKeybHeight, current_textTranscription, onModeChange = { onModeChange(it) }, onAlphabetChange = { onAlphabetChange() })
                }
                topBar = topBar_old
            } else {
                if (topBar_modern == null) {
                    topBar_modern = TopRowBuilder.createTopRow(this, systemKeybHeight, onModeChange = { onModeChange(it) }, onAlphabetChange = { onAlphabetChange() })
                }
                topBar = topBar_modern
            }
        } else {
            if (topBar_noTR == null) {
                topBar_noTR = TopRowBuilder_Alphabet.createTopRow(this, systemKeybHeight, TextTranscription.Off, onModeChange = { onModeChange(it) }, onAlphabetChange = { onAlphabetChange() })
            }
            topBar = topBar_noTR
        }

        var currentTamgaTranscription = current_letterTranscription
        current_letterTranscription = LetterTranscription.On
        root_Symbols = KeyboardViewBuilder.buildKeyboardView(
            service = this,
            layout = KeyboardLayoutLoader.loadKeyboardLayout(this, KeyboardMode.Symbols, getLanguage()),
            false,
            KeyboardMode.Emojis,
            maxKeyCount = 10,
            onCapsChange = { isCaps = it; applyKeyboard() },
            onModeChange = { onModeChange(it) },
            onAlphabetChange = { onAlphabetChange() })
        current_letterTranscription = currentTamgaTranscription

        root_ShiftOn = KeyboardViewBuilder.buildKeyboardView(
            service = this,
            layout = currentLayout!!,
            true,
            keyboardMode,
            maxButtonInOneRow,
            onCapsChange = { isCaps = it; applyKeyboard() },
            onModeChange = { onModeChange(it) },
            onAlphabetChange = { onAlphabetChange() })

        root_ShiftOff = KeyboardViewBuilder.buildKeyboardView(
            service = this,
            layout = currentLayout!!,
            false,
            keyboardMode,
            maxButtonInOneRow,
            onCapsChange = { isCaps = it; applyKeyboard() },
            onModeChange = { onModeChange(it) },
            onAlphabetChange = { onAlphabetChange() })
    }

    private fun makeEmojiView(): LinearLayout {
        root_Emojis = EmojisViewBuilder.create(service = this, EmojisData.defaultCategories(), onKeyPress = {
            this.currentInputConnection?.commitText(it, 1)
        }, onABC = {
            keyboardMode = KeyboardMode.Main
            reloadKeyboard()
        }, onBackspace = {
            SystemKeyBuilder.performDelete(context)
        })

        return root_Emojis!!
    }

    private fun makeSavablesView(): LinearLayout {
        val view = SavedStringsViewBuilder.create(service = this, false, keyboardMode, onKeyPress = {
            this.currentInputConnection?.commitText(it, 1)
        }, onModeChange = {
            keyboardMode = KeyboardMode.Main
            reloadKeyboard()
        })

        return view!!
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
        if (current_writingSystem != WritingSystem.Bitik) {
            return current_writingSystem.id
        }

        var dialect = if (current_bitikDialect == BitikDialect.Altay) "enesay" else "orhon"

        return if (keyboardVariant == BitikVariant.CLASSIC) {
            "${dialect}_old"
        } else if (keyboardVariant == BitikVariant.Modern) {
            "${dialect}_modern"
        } else {
            dialect
        }
    }
}