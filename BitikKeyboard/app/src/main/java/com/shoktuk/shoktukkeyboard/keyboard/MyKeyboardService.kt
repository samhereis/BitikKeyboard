package com.shoktuk.shoktukkeyboard.keyboard

import Haptics
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.view.HapticFeedbackConstants
import KeyboardViewLifecycleOwner
import LocalizationManager
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import com.shoktuk.shoktukkeyboard.bench.TypingBench
import com.shoktuk.shoktukkeyboard.emoji.EmojisData
import com.shoktuk.shoktukkeyboard.emoji.EmojisViewBuilder
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.InputText_Transcribed
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.inputText_LastWord
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.text_Original
import com.shoktuk.shoktukkeyboard.project.data.Arabic_Status
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Variant
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import com.shoktuk.shoktukkeyboard.project.data.NavBarPaddingSolution
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.arabicStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.autoCapitalize
import com.shoktuk.shoktukkeyboard.project.data.BitikTranscriptionAlphabets
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikTranscriptionAlphabets
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bottomOffset
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager
import com.shoktuk.shoktukkeyboard.project.data.ResizeSync
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardHeight
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

open class MyKeyboardService : InputMethodService() {
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

        /** False while the field is a password, e-mail, web address, number or other field where suggestions must not show or be learned. */
        var fieldAllowsSuggestions: Boolean = true
            private set

        /** Whether suggestions may show / be learned in the current field. */
        fun canSuggest(): Boolean = fieldAllowsSuggestions

        /** The shift key is on because of auto-capitalization (so it can go off again when the sentence start is gone). */
        private var autoCapped = false
        /** True while backspace is held down. */
        @JvmStatic var deleteHeldAt = 0L
        /** Backspace counts as held only while it keeps ticking, so a lost key-up can never leave auto-capital switched off. */
        @JvmStatic var deleteHeld: Boolean = false
            get() = field && android.os.SystemClock.uptimeMillis() - deleteHeldAt < 700
            set(v) { field = v; if (v) deleteHeldAt = android.os.SystemClock.uptimeMillis() }
        private var lastCapWanted = false

        /** True when the next letter starts a text or a sentence: nothing typed yet, a new line, or a sentence end followed by a space. */
        fun wantsCapital(before: String): Boolean {
            var sawSpace = false
            for (ch in before.reversed()) {
                if (ch == '\n') return true
                if (ch.isWhitespace()) { sawSpace = true; continue }
                return sawSpace && ".!?⸮…".indexOf(ch) >= 0
            }
            return true
        }

        /** The tail of the text after the keyboard added a trailing space itself; a space the user typed never matches it. */
        private var autoSpaceTail: String? = null

        fun markAutoSpace(ic: InputConnection?) {
            val before = ic?.getTextBeforeCursor(30, 0)?.toString() ?: ""
            autoSpaceTail = if (before.endsWith(" ")) before else null
        }

        /** At most [wanted] units, and never more than the word (letters, Bitik glyphs) really before the cursor. */
        fun safeWordLength(ic: InputConnection, wanted: Int): Int {
            val before = ic.getTextBeforeCursor(wanted, 0)?.toString() ?: return 0
            var n = 0
            for (c in before.reversed()) { if (c.isLetter() || Character.isSurrogate(c) || Character.getType(c) == Character.NON_SPACING_MARK.toInt()) n++ else break }
            return n
        }

        fun ownsTrailingSpace(before: String): Boolean {
            val tail = autoSpaceTail ?: return false
            return before.endsWith(" ") && before.takeLast(30) == tail
        }

        fun allowsSuggestions(info: EditorInfo?): Boolean {
            val type = info?.inputType ?: return true
            if (type == 0) return true
            if (type and android.text.InputType.TYPE_MASK_CLASS != android.text.InputType.TYPE_CLASS_TEXT) return false
            if (type and android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0) return false
            return when (type and android.text.InputType.TYPE_MASK_VARIATION) {
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD, android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD, android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                android.text.InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS, android.text.InputType.TYPE_TEXT_VARIATION_URI -> false
                else -> true
            }
        }

        val buttonMargin: Int = KeyboardTheme.KEY_MARGIN_DP

        val isTamga: Boolean get() = keyboardMode == KeyboardMode.Main
        val showLetterTranscription: Boolean get() = current_letterTranscription == LetterTranscription.On
        val showTextTranscription: Boolean get() = current_textTranscription == TextTranscription.On

        var bottomPadding: Int? = null
        var maxButtonInOneRow: Int = 10
    }

    private var currentLayout: KeyboardLayout? = null

    val lifecycleOwner = KeyboardViewLifecycleOwner()

    override fun onCreate() {
        super.onCreate()
        SettingsManager.migrateKeyboardHeight(this)
        LocalizationManager.init(applicationContext)
        lifecycleOwner.onCreate()
        lifecycleOwner.onResume()
        TypingBench.install(this)
    }

    override fun onDestroy() {
        TypingBench.uninstall(this)
        lifecycleOwner.onDestroy()
        super.onDestroy()
    }

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
        // The resets above wipe whatever onStartInputView decided, so decide again.
        lastCapWanted = false
        autoCapped = false
        refreshAutoCaps()

        onSettingChanged.addListener {
            if (keyboardMode != KeyboardMode.Settings) reloadKeyboard()
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

    /** Latin and Cyrillic keyboards: lets the shift key follow [wantsCapital] (setting on, field takes normal text). Acts only when it changes. */
    private var lastCapContext = ""

    fun refreshAutoCaps() {
        if (deleteHeld) return
        val ws = current_writingSystem
        var wanted = false
        var ctx = ""
        if (autoCapitalize && (ws == WritingSystem.Latin || ws == WritingSystem.Kiril) && fieldAllowsSuggestions && keyboardMode == KeyboardMode.Main) {
            val before = currentInputConnection?.getTextBeforeCursor(100, 0)?.toString() ?: return
            ctx = before
            wanted = wantsCapital(before)
        }
        // The text changed and a capital is wanted but shift is off (e.g. everything was erased after a stale read): switch it on again.
        val stale = wanted && lastCapWanted && !isCaps && ctx != lastCapContext
        lastCapContext = ctx
        if (wanted == lastCapWanted && !stale) return
        lastCapWanted = wanted
        if (wanted) {
            if (!isCaps) { isCaps = true; autoCapped = true; applyKeyboard() }
        } else if (autoCapped) {
            autoCapped = false
            if (isCaps) { isCaps = false; applyKeyboard() }
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        fieldAllowsSuggestions = allowsSuggestions(info)
        lastCapWanted = false
        autoCapped = false
        refreshAutoCaps()
        run {
            val configure = com.shoktuk.shoktukkeyboard.suggestions.WordSuggestionEngine.configurator(this)
            com.shoktuk.shoktukkeyboard.suggestions.SuggestionWorker.idleWork { configure(); com.shoktuk.shoktukkeyboard.suggestions.WordSuggestionEngine.warmNext() }
        }
        TopRowBuilder_Old.onTypedListener?.invoke()
        onKeyPressed.addListener { ic, key, isSystemKey ->
            TypingBench.noteKey(key)
            TypingBench.measure("key total") { handleKeyPress(ic, key, isSystemKey) }
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
        TypingBench.measure("onUpdateSelection") {
            TopRowBuilder_Old.onTypedListener?.invoke()
            refreshAutoCaps()
        }
    }

    override fun onWindowHidden() {
        MyKeyboardService.bottomPadding = null
        com.shoktuk.shoktukkeyboard.suggestions.LearnedWords.flush()
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
        } else if (keyboardMode == KeyboardMode.Settings) {
            root = makeSettingsView()
        } else if (keyboardMode == KeyboardMode.Resize) {
            root = makeResizeView()
        } else {
            if (isCaps) {
                root = root_ShiftOn
            } else {
                root = root_ShiftOff
            }

            if (keyboardMode == KeyboardMode.Main) {
            }
        }

        // Emojis fill the top bar's height with a taller grid, so the keyboard keeps one height.
        if (keyboardMode != KeyboardMode.Settings && keyboardMode != KeyboardMode.SavedStrings && keyboardMode != KeyboardMode.Emojis && keyboardMode != KeyboardMode.Resize) {
            ensureDetached(topBar)
            root?.addView(topBar, 0)
        }

        root?.let {
            if (keyboardMode != KeyboardMode.Resize) applyInsetsNowAndOnChange(it)
            showInputView(it)
        }
    }

    /** Puts the keyboard on screen. The debug store tooling (src/debug/.../store) overrides it to draw the keyboard into a picture. */
    protected open fun showInputView(view: View) = setInputView(view)

    private fun ensureDetached(view: View?) {
        val parent = view?.parent as? ViewGroup
        parent?.removeView(view)
    }

    fun reloadKeyboard() = TypingBench.measure("reloadKeyboard") { reloadKeyboardImpl() }

    private fun reloadKeyboardImpl() {
        current_bitikVariant = context.keyboardVariant
        current_bitikDialect = context.bitikDialect
        current_writingSystem = context.writingSystem
        current_letterTranscription = context.letterTranscription
        current_textTranscription = context.textTranscription

        maxButtonInOneRow = 10
        if (current_writingSystem == WritingSystem.Kiril) {
            maxButtonInOneRow = 11
        } else if (current_writingSystem == WritingSystem.Arab) {
            maxButtonInOneRow = 11
        }

        fillRoots()
        applyKeyboard()
    }

    private fun onModeChange(mode: KeyboardMode) {
        keyboardMode = mode
        applyKeyboard()
        // Back from the symbols page (or emojis, settings): the text may now call for a capital again.
        if (mode == KeyboardMode.Main) { lastCapWanted = false; autoCapped = false; refreshAutoCaps() }
    }

    fun availableWritingSystems(): List<WritingSystem> {
        // Order: Arabic, Bitik, Latin, Kiril. Bitik is the default and is always available.
        val available = mutableListOf<WritingSystem>()
        if (context.arabicStatus == Arabic_Status.On) available.add(WritingSystem.Arab)
        available.add(WritingSystem.Bitik)
        if (context.latinStatus == Latin_Status.On) available.add(WritingSystem.Latin)
        if (context.kirilisaStatus == Kirilisa_Status.On) available.add(WritingSystem.Kiril)
        return available
    }

    fun switchAlphabetTo(system: WritingSystem) {
        if (system == context.writingSystem || system !in availableWritingSystems()) return
        context.writingSystem = system
        keyboardMode = KeyboardMode.Main
        reloadKeyboard()
    }

    fun switchAlphabet(step: Int) {
        val available = availableWritingSystems()
        if (available.size < 2) return
        val current = available.indexOf(context.writingSystem).coerceAtLeast(0)
        context.writingSystem = available[(current + step + available.size) % available.size]
        keyboardMode = KeyboardMode.Main
        reloadKeyboard()
    }

    private fun onAlphabetChange() {
        if (context.latinStatus == Latin_Status.Off && context.kirilisaStatus == Kirilisa_Status.Off && context.arabicStatus == Arabic_Status.Off) {
            context.writingSystem = WritingSystem.Bitik
            keyboardMode = KeyboardMode.Emojis
        } else {
            val availableLanguages = availableWritingSystems()

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
        root_Emojis = null  // rebuilt on the next open, so theme and size changes apply
        EmojisViewBuilder.warmUp(EmojisData.defaultCategories())
        currentLayout = KeyboardLayoutLoader.loadKeyboardLayout(this, keyboardMode, getLanguage())
        var systemKeybHeight = (KeyboardTheme.getButtonHeight() / 1.5f).toInt()

        if (MyKeyboardService.current_writingSystem == WritingSystem.Bitik) {
            topBar_old = TopRowBuilder_Old.createTopRow(this, systemKeybHeight, current_textTranscription, onModeChange = { onModeChange(it) }, onAlphabetChange = { onAlphabetChange() })
            topBar = topBar_old
        } else {
            // With "transcription to Bitik" off, the alphabets show the saved strings row instead.
            // Latin and Kiril always get the suggestion bar (its Bitik chip hides itself when transcription is off).
            com.shoktuk.shoktukkeyboard.suggestions.SuggestionNeighbours.update(currentLayout!!)
            val suggestions = current_writingSystem == WritingSystem.Latin || current_writingSystem == WritingSystem.Kiril || current_writingSystem == WritingSystem.Arab
            topBar_noTR = if (suggestions || context.bitikTranscriptionAlphabets == BitikTranscriptionAlphabets.On) {
                TopRowBuilder_Alphabet.createTopRow(this, systemKeybHeight, TextTranscription.Off, onModeChange = { onModeChange(it) }, onAlphabetChange = { onAlphabetChange() })
            } else {
                TopRowBuilder.createTopRow(this, systemKeybHeight, onModeChange = { onModeChange(it) }, onAlphabetChange = { onAlphabetChange() })
            }
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
        root_Emojis?.let { return it }
        val topBarHeight = (KeyboardTheme.getButtonHeight() / 1.5f).toInt()
        root_Emojis = EmojisViewBuilder.create(service = this, EmojisData.defaultCategories(), totalHeight = KeyboardTheme.getButtonHeight() * 4 + topBarHeight, onKeyPress = {
            onKeyPressed?.invoke(this.currentInputConnection, it, false)
        }, onABC = {
            keyboardMode = KeyboardMode.Main
            reloadKeyboard()
        }, onBackspace = {
            SystemKeyBuilder.performDelete(context)
        })

        return root_Emojis!!
    }

    private fun makeSettingsView(): LinearLayout {
        val systemKeybHeight = (KeyboardTheme.getButtonHeight() / 1.5f).toInt()
        val height = KeyboardTheme.getButtonHeight() * 4 + systemKeybHeight
        val panel = SettingsPanelBuilder.create(this, height) {
            keyboardMode = KeyboardMode.Main
            reloadKeyboard()
        }
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(panel)
        }
    }

    /** The resize mode: the keyboard without labels and the buttons that change its height and position, see [ResizeOverlayView]. */
    private fun makeResizeView(): LinearLayout {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        // The space under the keyboard without the offset (the navigation bar); the overlay draws that space itself, so this root gets no padding.
        val base = (bottomPadding ?: (autoNavBarInsetPx(box) + this.bottomOffset)) - this.bottomOffset
        lateinit var overlay: ResizeOverlayView
        overlay = ResizeOverlayView(
            this,
            startHeight = this.keyboardHeight,
            startOffset = this.bottomOffset,
            defaultHeight = SettingsManager.DEFAULT_KEYBOARD_HEIGHT,
            defaultOffset = SettingsManager.DEFAULT_BOTTOM_OFFSET,
            basePadding = base,
            onChange = { keyHeight, offset ->
                this.keyboardHeight = keyHeight
                this.bottomOffset = offset
                ResizeSync.tick++  // the sliders in the app follow along
                overlay.layoutParams = overlay.layoutParams.apply { height = overlay.windowHeight() }
            },
            onReleased = { overlay.setLook(captureKeyboardLook()) },
            onDone = {
                bottomPadding = null  // worked out again for the new offset
                keyboardMode = KeyboardMode.Main
                reloadKeyboard()
            })
        overlay.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, overlay.windowHeight())
        overlay.setLook(captureKeyboardLook())
        box.addView(overlay)
        return box
    }

    /**
     * A picture of the real keyboard (top row included) at the current size with every label hidden, the same look the keys take while
     * the cursor is dragged. It is built off screen; null if anything goes wrong, and the resize view then draws plain keys.
     */
    private fun captureKeyboardLook(): android.graphics.Bitmap? {
        val savedMode = keyboardMode
        return try {
            keyboardMode = KeyboardMode.Main  // the Main layout, not the plain one
            fillRoots()
            val keyboard = root_ShiftOff ?: return null
            ensureDetached(keyboard)
            ensureDetached(topBar)
            topBar?.let { keyboard.addView(it, 0) }
            val width = resources.displayMetrics.widthPixels
            keyboard.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            keyboard.layout(0, 0, width, keyboard.measuredHeight)
            BottomRowBuilder.setLabelsHidden(keyboard, true)
            val bitmap = android.graphics.Bitmap.createBitmap(width, keyboard.measuredHeight, android.graphics.Bitmap.Config.ARGB_8888)
            keyboard.draw(android.graphics.Canvas(bitmap))
            BottomRowBuilder.setLabelsHidden(keyboard, false)
            topBar?.let { keyboard.removeView(it) }
            bitmap
        } catch (e: Throwable) {
            null
        } finally {
            keyboardMode = savedMode
        }
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
        com.shoktuk.shoktukkeyboard.bench.TypingBench.count("handleKeyPress")
        val isSys = isSystemKey || listOfAlwaysSyss.contains(key)
        var toPasteAfter = key.replace("sys", "")

        // Smart punctuation (like Gboard): only a space the keyboard added itself is taken back before punctuation,
        // a space the user typed stays ("Hello !"); "5.12" keeps together; a letter after "7." brings the space back.
        val before = TypingBench.measure("getTextBefore") { ic.getTextBeforeCursor(30, 0)?.toString() } ?: ""
        val first = toPasteAfter.firstOrNull()
        val isPunctuation = first != null && ".,!?⸮⹁·;:".indexOf(first) >= 0
        var afterDigit = false
        var tookBackSpace = false
        var autoSpaced = false
        if (isPunctuation) {
            if (ownsTrailingSpace(before)) {
                val beforeSpace = before.dropLast(1).lastOrNull()
                if (beforeSpace != null && !beforeSpace.isWhitespace() && beforeSpace != '⁚') { ic.deleteSurroundingText(1, 0); tookBackSpace = true }
            } else if (isSys && (first == '.' || first == ',' || first == '·' || first == '⹁') && before.lastOrNull()?.let { it in '0'..'9' } == true) {
                afterDigit = true
                toPasteAfter = first.toString()
            }
        } else if (!isSys && first != null && first.isLetter() && before.length >= 2 &&
            before.last() in ".,·⹁" && before[before.length - 2] in '0'..'9') {
            ic.commitText(" ", 1)
        }

        // A space or punctuation ends the word being typed: it counts as used once more.
        if (current_writingSystem != WritingSystem.Bitik && canSuggest() && toPasteAfter.firstOrNull()?.isLetter() == false) {
            // Only a piece made of letters alone counts ("abc123", "me@mail.com" or "don't" never do).
            val token = text_Original.split(Regex("\\s+")).lastOrNull().orEmpty()
            if (token.length >= 2 && token.all { it.isLetter() }) com.shoktuk.shoktukkeyboard.suggestions.SuggestionWorker.background { com.shoktuk.shoktukkeyboard.suggestions.WordSuggestionEngine.learnTyped(token) }
        }

        if (isAutoWriteBitikMode && isSys) {
            toPasteAfter = toPasteAfter.replace("  ", " ").replace("?", "⸮ ").replace("!", "! ").let { if (afterDigit) it else it.replace(".", "·").replace(",", "⹁") }

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
            // "!" and "?" from the symbols page end a sentence like the dot key does, so a space follows (not after a space, nor in e-mail/URL/password fields).
            var out = if (afterDigit) toPasteAfter else key
            if (out.length == 1 && out[0] in "!?⸮" && fieldAllowsSuggestions) {
                val prev = (if (tookBackSpace) before.dropLast(1) else before).lastOrNull()
                if (prev != null && !prev.isWhitespace() && prev != '⁚') { out += " "; autoSpaced = true }
            }
            when {
                current_writingSystem == WritingSystem.Bitik -> {
                    TypingBench.measure("commitText") { ic.commitText(out, 1) }
                }

                else -> {
                    // Shift goes off before the text goes in: a field in this process reports the change from inside commitText, and auto-capital may switch shift on again from there.
                    if (isCaps) {
                        isCaps = false
                        TypingBench.measure("applyKeyboard") { applyKeyboard() }
                    }
                    TypingBench.measure("commitText") { ic.commitText(out, 1) }
                }
            }

            TopRowBuilder_Old.onTypedListener?.invoke()
        }

        if (isPunctuation && (autoSpaced || (isSys && toPasteAfter.endsWith(" ")))) markAutoSpace(ic)

        TypingBench.measure("autoCaps") { refreshAutoCaps() }

        try {
            // Haptics.perform gates sound (Sounds setting) and vibration (Vibrations
            // setting) independently, so play it on every key regardless of vibration.
            TypingBench.measure("haptics") { Haptics.perform(root as View, HapticFeedbackConstants.KEYBOARD_TAP) }
        } catch (e: Exception) {
            print(e.message)
        }
    }

    fun replaceText(ic: InputConnection, toPasteAfter: String) {
        if (!inputText_LastWord.isBlank()) {
            try {
                ic.beginBatchEdit()
                ic.deleteSurroundingText(safeWordLength(ic, inputText_LastWord.length), 0)
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