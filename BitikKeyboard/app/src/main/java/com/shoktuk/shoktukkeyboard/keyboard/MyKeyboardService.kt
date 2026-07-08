package com.shoktuk.shoktukkeyboard.keyboard

import Haptics
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import com.shoktuk.shoktukkeyboard.emoji.EmojisData
import com.shoktuk.shoktukkeyboard.emoji.EmojisViewBuilder
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.InputText_Transcribed
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.inputText_LastWord
import com.shoktuk.shoktukkeyboard.project.data.Arabic_Status
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Variant
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import com.shoktuk.shoktukkeyboard.project.data.NavBarPaddingSolution
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.arabicStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bottomOffset
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.kirilisaStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.letterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.navBarPaddingSolution
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.textTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.writingSystem
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import com.whl.quickjs.android.QuickJSLoader

enum class KeyboardMode(val id: String) {
    Main("Main"), Symbols("Symbols"), Emojis("Emojis"), SavedStrings("SavedStrings");
}

object onKeyPressed {
    var text_Original: String = ""
    var inputText_LastWord: String = ""
    var InputText_Transcribed: String = ""
    var InputText_Transcribed_Alt: String = ""

    private val listeners = mutableListOf<(InputConnection, String, Boolean) -> Unit>()

    fun addListener(listener: (InputConnection, String, Boolean) -> Unit) {
        listeners.clear()
        listeners.add(listener)
    }

    fun invoke(ic: InputConnection, key: String, isSystemKey: Boolean = false) {
        listeners.forEach { it(ic, key, isSystemKey) }
    }
}

object onSettingChanged {
    private val listeners = mutableListOf<() -> Unit>()

    fun addListener(listener: () -> Unit) {
        listeners.clear()
        listeners.add(listener)
    }

    fun invoke() {
        MyKeyboardService.bottomPadding = null
        listeners.forEach { it() }
    }
}

class MyKeyboardService : InputMethodService() {
    companion object {
        lateinit var context: MyKeyboardService

        var current_bitikVariant: BitikVariant = BitikVariant.Modern
        var current_bitikDialect: BitikDialect = BitikDialect.Altay
        var current_writingSystem: WritingSystem = WritingSystem.Bitik
        var current_letterTranscription: LetterTranscription = LetterTranscription.On
        var current_textTranscription: TextTranscription = TextTranscription.On

        var keyboardMode: KeyboardMode = KeyboardMode.Main

        var isCaps: Boolean = false
        var isAutoWriteBitikMode: Boolean = false

        val buttonMargin: Int = KeyboardTheme.KEY_MARGIN_DP

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

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean = true

    override fun onWindowShown() {
        super.onWindowShown()

        context = this
        isCaps = false
        keyboardMode = KeyboardMode.Main
        MyKeyboardService.bottomPadding = null

        if (currentLayout != null) {
            reloadKeyboard()
        }

        onSettingChanged.addListener {
            reloadKeyboard()
        }
    }

    override fun onCreateInputView(): View? {
        context = this
        isCaps = false
        keyboardMode = KeyboardMode.Main
        MyKeyboardService.bottomPadding = null

        reloadKeyboard()

        if (root != null) {
            applyInsetsNowAndOnChange(root!!)
        }

        QuickJSLoader.init()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        TopRowBuilder_Old.onTypedListener?.invoke()
        onKeyPressed.addListener { ic, key, isSystemKey ->
            handleKeyPress(ic, key, isSystemKey)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        KeyboardTheme.invalidateColorCache()
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd
        )
        TopRowBuilder_Old.onTypedListener?.invoke()
    }

    override fun onWindowHidden() {
        MyKeyboardService.bottomPadding = null
        super.onWindowHidden()
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
        } else if (current_writingSystem == WritingSystem.Latin) {
        }

        fillRoots()
        applyKeyboard()
    }

    private fun onModeChange(mode: KeyboardMode) {
        keyboardMode = mode
        applyKeyboard()
    }

    private fun onAlphabetChange() {
        if (context.latinStatus == Latin_Status.Off && context.kirilisaStatus == Kirilisa_Status.Off && context.arabicStatus == Arabic_Status.Off) {
            context.writingSystem = WritingSystem.Bitik
            keyboardMode = KeyboardMode.Emojis
        } else {
            val availableLanguages = mutableListOf<WritingSystem>(WritingSystem.Bitik)
            if (context.latinStatus == Latin_Status.On) availableLanguages.add(WritingSystem.Latin)
            if (context.arabicStatus == Arabic_Status.On) availableLanguages.add(WritingSystem.Arab)
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
                topBar_old = TopRowBuilder_Old.createTopRow(this, systemKeybHeight, current_textTranscription, onModeChange = { onModeChange(it) }, onAlphabetChange = { onAlphabetChange() })
                topBar = topBar_old
            } else {
                topBar_modern = TopRowBuilder.createTopRow(this, systemKeybHeight, onModeChange = { onModeChange(it) }, onAlphabetChange = { onAlphabetChange() })
                topBar = topBar_modern
            }
        } else {
            topBar_noTR = TopRowBuilder_Alphabet.createTopRow(this, systemKeybHeight, TextTranscription.Off, onModeChange = { onModeChange(it) }, onAlphabetChange = { onAlphabetChange() })
            topBar = topBar_noTR
        }

        var currentTamgaTranscription = current_letterTranscription
        var currentMode = keyboardMode
        keyboardMode = KeyboardMode.Symbols

        current_letterTranscription = LetterTranscription.On
        root_Symbols = KeyboardViewBuilder.buildKeyboardView(
            service = this,
            layout = KeyboardLayoutLoader.loadKeyboardLayout(this, keyboardMode, getLanguage()),
            false,
            keyboardMode,
            maxKeyCount = 10,
            onCapsChange = { isCaps = it; applyKeyboard() },
            onModeChange = { onModeChange(it) })
        current_letterTranscription = currentTamgaTranscription
        keyboardMode = currentMode

        root_ShiftOn = KeyboardViewBuilder.buildKeyboardView(
            service = this,
            layout = currentLayout!!,
            true,
            keyboardMode,
            maxButtonInOneRow,
            onCapsChange = { isCaps = it; applyKeyboard() },
            onModeChange = { onModeChange(it) })

        root_ShiftOff = KeyboardViewBuilder.buildKeyboardView(
            service = this,
            layout = currentLayout!!,
            false,
            keyboardMode,
            maxButtonInOneRow,
            onCapsChange = { isCaps = it; applyKeyboard() },
            onModeChange = { onModeChange(it) })

        TopRowBuilder_Old.onTypedListener?.invoke()
    }

    private fun makeEmojiView(): LinearLayout {
        root_Emojis = EmojisViewBuilder.create(service = this, EmojisData.defaultCategories(), onKeyPress = {
            onKeyPressed?.invoke(this.currentInputConnection, it, false)
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
            onKeyPressed?.invoke(this.currentInputConnection, it, false)
        }, onModeChange = {
            keyboardMode = KeyboardMode.Main
            reloadKeyboard()
        })

        return view!!
    }

    // Stable navigation-bar size that ignores current visibility/animation, with a
    // system-resource fallback if insets aren't reported. Used by the Auto solution.
    private fun autoNavBarInsetPx(view: View): Int {
        val decor = window?.window?.decorView ?: view
        val ignoring = ViewCompat.getRootWindowInsets(decor)
            ?.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
        if (ignoring > 0) return ignoring
        val resId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resId > 0) resources.getDimensionPixelSize(resId) else 0
    }

    private fun applyInsetsNowAndOnChange(view: View) {
        if (context.navBarPaddingSolution == NavBarPaddingSolution.Solution_Auto) {
            view.doOnAttach {
                if (bottomPadding == null) bottomPadding = autoNavBarInsetPx(it) + context.bottomOffset
                it.setPadding(it.paddingLeft, it.paddingTop, it.paddingRight, bottomPadding!!)
            }
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                if (bottomPadding == null) bottomPadding = autoNavBarInsetPx(v) + context.bottomOffset
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottomPadding!!)
                insets
            }
            ViewCompat.requestApplyInsets(view)
            return
        }

        if (context.navBarPaddingSolution == NavBarPaddingSolution.Solution_Enable_Off) {
            view.doOnAttach {
                val rootInsets = ViewCompat.getRootWindowInsets(it) ?: return@doOnAttach
                val nav = rootInsets.getInsets(WindowInsetsCompat.Type.navigationBars())

                if (bottomPadding == null) {
                    bottomPadding = context.bottomOffset
                }

                it.setPadding(it.paddingLeft, it.paddingTop, it.paddingRight, bottomPadding!!)
            }

            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                if (bottomPadding == null) {
                    bottomPadding = context.bottomOffset
                }

                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottomPadding!!)
                insets
            }
        } else {
            if (context.navBarPaddingSolution == NavBarPaddingSolution.Solution_Enable_1) {
                view.doOnAttach {
                    val rootInsets = ViewCompat.getRootWindowInsets(it) ?: return@doOnAttach
                    val nav = rootInsets.getInsets(WindowInsetsCompat.Type.navigationBars())

                    if (bottomPadding == null) {
                        bottomPadding = nav.bottom + context.bottomOffset
                    }

                    it.setPadding(it.paddingLeft, it.paddingTop, it.paddingRight, bottomPadding!!)
                }
            }

            if (context.navBarPaddingSolution == NavBarPaddingSolution.Solution_Enable_2) {
                ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                    val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

                    if (bottomPadding == null) {
                        bottomPadding = nav.bottom + context.bottomOffset
                    }

                    v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottomPadding!!)
                    insets
                }
            }
        }

        ViewCompat.requestApplyInsets(view)
    }

    fun getLanguage(): String {
        if (current_writingSystem != WritingSystem.Bitik) {
            if (writingSystem == WritingSystem.Latin && context.latinVariant == Latin_Variant.full) {
                maxButtonInOneRow = 12
                return "latin_full"
            }

            return current_writingSystem.id
        }

        return if (keyboardVariant == BitikVariant.CLASSIC) {
            "enesay_old"
        } else if (keyboardVariant == BitikVariant.Modern) {
            "enesay_modern"
        } else {
            "enesay"
        }
    }

    val listOfAlwaysSyss: Set<String> = setOf("?", "⸮", "!")
    fun handleKeyPress(ic: InputConnection, key: String, isSystemKey: Boolean) {
        val isSys = isSystemKey || listOfAlwaysSyss.contains(key)
        var toPasteAfter = key.replace("sys", "")

        if (isAutoWriteBitikMode && isSys) {
            toPasteAfter = toPasteAfter.replace("  ", " ").replace("?", "⸮ ").replace("!", "! ").replace(".", "·").replace(",", "⹁")

            if (toPasteAfter == " ") {
                toPasteAfter = when (context.wordSeparator) {
                    WordSeparator.NoSpace -> "⁚"
                    WordSeparator.SpaceBefore -> "⁚ "
                    WordSeparator.ArroundSpace -> " ⁚ "
                    WordSeparator.Off -> " "
                }
            }

            replaceText(ic, InputText_Transcribed + toPasteAfter)
        } else {
            when {
                current_writingSystem == WritingSystem.Bitik -> {
                    ic.commitText(key, 1)
                }

                else -> {
                    ic.commitText(key, 1)

                    if (isCaps) {
                        isCaps = false
                        applyKeyboard()
                    }
                }
            }

            TopRowBuilder_Old.onTypedListener?.invoke()
        }

        try {
            // Haptics.perform gates sound (Sounds setting) and vibration (Vibrations
            // setting) independently, so play it on every key regardless of vibration.
            Haptics.perform(root as View, HapticFeedbackConstants.KEYBOARD_TAP)
        } catch (e: Exception) {
            print(e.message)
        }
    }

    fun replaceText(ic: InputConnection, toPasteAfter: String) {
        if (!inputText_LastWord.isBlank()) {
            try {
                ic.beginBatchEdit()
                ic.deleteSurroundingText(inputText_LastWord.length, 0)
            } catch (_: Exception) {
            } finally {
                ic.endBatchEdit()
            }
        }

        ic.commitText(toPasteAfter, 1)
        TopRowBuilder_Old.onTypedListener?.invoke()
    }

    private fun ensureRTLContext(service: InputMethodService): Boolean {
        val inputConnection = service.currentInputConnection ?: return false
        val textBefore = inputConnection.getTextBeforeCursor(1, 0)
        return textBefore.isNullOrEmpty() || textBefore.last() == '\n'
    }
}