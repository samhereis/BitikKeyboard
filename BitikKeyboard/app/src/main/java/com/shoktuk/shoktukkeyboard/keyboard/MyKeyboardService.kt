package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import com.shoktuk.shoktukkeyboard.emoji.EmojisData
import com.shoktuk.shoktukkeyboard.emoji.EmojisViewBuilder
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
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

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onWindowShown() {
        super.onWindowShown()

        if (currentLayout != null) {
            reloadKeyboard()
        }
    }

    override fun onCreateInputView(): View? {
        context = this

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
        if (keyboardMode == KeyboardMode.Symbols) {
            root = root_Symbols
        } else if (keyboardMode == KeyboardMode.Emojis) {
            root = root_Emojis
        } else {
            if (isCaps) {
                root = root_ShiftOn
            } else {
                root = root_ShiftOff
            }
        }

        if (root != null) {
            applyInsetsNowAndOnChange(root!!)
            setInputView(root)
        }
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

    private fun fillRoots() {
        currentLayout = KeyboardLayoutLoader.loadKeyboardLayout(this, keyboardMode, getLanguage())

        if (root_Emojis == null) {
            root_Emojis = EmojisViewBuilder.create(service = this, EmojisData.defaultCategories(), onKeyPress = {
                this.currentInputConnection?.commitText(it, 1)
            }, onABC = {
                keyboardMode = KeyboardMode.Main
                reloadKeyboard()
            }, onBackspace = {
                this.currentInputConnection?.deleteSurroundingText(1, 1)
            })
        }

        root_Symbols = KeyboardViewBuilder.buildKeyboardView(
            service = this,
            layout = KeyboardLayoutLoader.loadKeyboardLayout(this, KeyboardMode.Symbols, getLanguage()),
            false,
            maxKeyCount = 10,
            onCapsChange = { isCaps = it; applyKeyboard() },
            onModeChange = {
                if (it == "emojis") {
                    keyboardMode = KeyboardMode.Emojis
                } else {
                    keyboardMode = if (keyboardMode == KeyboardMode.Main) KeyboardMode.Symbols else KeyboardMode.Main
                }
                applyKeyboard()
            },
            onAlphabetChange = {
                if (current_writingSystem == WritingSystem.Bitik) {
                    context.writingSystem = WritingSystem.Latin
                } else if (current_writingSystem == WritingSystem.Latin) {
                    context.writingSystem = WritingSystem.Kiril
                } else {
                    context.writingSystem = WritingSystem.Bitik
                }
                keyboardMode = KeyboardMode.Main
                reloadKeyboard()
            })

        root_ShiftOn = KeyboardViewBuilder.buildKeyboardView(service = this, layout = currentLayout!!, true, maxButtonInOneRow, onCapsChange = { isCaps = it; applyKeyboard() }, onModeChange = {
            if (it == "emojis") {
                keyboardMode = KeyboardMode.Emojis
            } else {
                keyboardMode = if (keyboardMode == KeyboardMode.Main) KeyboardMode.Symbols else KeyboardMode.Main
            }
            applyKeyboard()
        }, onAlphabetChange = {
            if (current_writingSystem == WritingSystem.Bitik) {
                context.writingSystem = WritingSystem.Latin
            } else if (current_writingSystem == WritingSystem.Latin) {
                context.writingSystem = WritingSystem.Kiril
            } else {
                context.writingSystem = WritingSystem.Bitik
            }
            keyboardMode = KeyboardMode.Main
            reloadKeyboard()
        })

        root_ShiftOff = KeyboardViewBuilder.buildKeyboardView(service = this, layout = currentLayout!!, false, maxButtonInOneRow, onCapsChange = { isCaps = it; applyKeyboard() }, onModeChange = {
            if (it == "emojis") {
                keyboardMode = KeyboardMode.Emojis
            } else {
                keyboardMode = if (keyboardMode == KeyboardMode.Main) KeyboardMode.Symbols else KeyboardMode.Main
            }
            applyKeyboard()
        }, onAlphabetChange = {
            if (current_writingSystem == WritingSystem.Bitik) {
                context.writingSystem = WritingSystem.Latin
            } else if (current_writingSystem == WritingSystem.Latin) {
                context.writingSystem = WritingSystem.Kiril
            } else {
                context.writingSystem = WritingSystem.Bitik
            }
            keyboardMode = KeyboardMode.Main
            reloadKeyboard()
        })
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
        } else {
            dialect
        }
    }
}