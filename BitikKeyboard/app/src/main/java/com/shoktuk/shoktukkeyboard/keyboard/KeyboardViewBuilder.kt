package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService.Companion.context
import com.shoktuk.shoktukkeyboard.project.data.AS_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.EB_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EN_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ESH_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.asVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ebVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.enVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.eshVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.freeTamga_Click
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.freeTamga_Hold
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

object KeyboardViewBuilder {
    var eb_Def = context.ebVariant == EB_Letter_Variant.Default
    var en_Def = context.enVariant == EN_Letter_Variant.Default
    var as_Def = context.asVariant == AS_Letter_Variant.Default
    var esh_Def = context.eshVariant == ESH_Letter_Variant.Default

    fun buildKeyboardView(
        service: InputMethodService,
        layout: KeyboardLayout,
        isCaps: Boolean,
        mode: KeyboardMode,
        maxKeyCount: Int,
        onCapsChange: (Boolean) -> Unit,
        onModeChange: (KeyboardMode) -> Unit,
        onAlphabetChange: () -> Unit
    ): LinearLayout {
        eb_Def = context.ebVariant == EB_Letter_Variant.Default
        en_Def = context.enVariant == EN_Letter_Variant.Default
        as_Def = context.asVariant == AS_Letter_Variant.Default
        esh_Def = context.eshVariant == ESH_Letter_Variant.Default

        var systemKeybHeight = (KeyboardTheme.getButtonHeight() / 1.5f).toInt()

        val screenWidthPx = service.resources.displayMetrics.widthPixels
        val baseDesignWidthDp = 360f
        val density = service.resources.displayMetrics.density
        val maxWidthPx = (baseDesignWidthDp * density).toInt()
        val sideMarginPx = if (screenWidthPx > maxWidthPx) (screenWidthPx - maxWidthPx) / 2 else 0

        val container = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                if (screenWidthPx > maxWidthPx) maxWidthPx else ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                leftMargin = sideMarginPx
                rightMargin = sideMarginPx
            }
            setBackgroundColor(KeyboardTheme.containerBg())
        }
        container.clipChildren = false
        container.clipToPadding = false

        layout.rows.forEach { row ->
            container.addView(
                createRowLayout(
                    service, row, isCaps, mode, KeyboardTheme.getButtonHeight(), maxKeyCount, onCapsChange, onModeChange
                )
            )
        }

        container.addView(
            BottomRowBuilder.createBottomRow(
                service, isCaps, mode, KeyboardTheme.getButtonHeight(), onModeChange
            )
        )
        return container
    }

    private fun createRowLayout(
        service: InputMethodService,
        row: List<KeyEntry>,
        isCaps: Boolean,
        mode: KeyboardMode,
        buttonHeight: Int,
        maxKeyCount: Int,
        onCapsChange: (Boolean) -> Unit,
        onModeChange: (KeyboardMode) -> Unit
    ): LinearLayout {
        var keybWidth = KeyboardTheme.getLetterButtonWidth(context, maxKeyCount)
        var systemKeybWidth = KeyboardTheme.getSystemButtonWidth(service)
        if (MyKeyboardService.maxButtonInOneRow > 10) {
            systemKeybWidth = keybWidth
        }

        val rowLayout = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            clipChildren = false
            clipToPadding = false
        }
        rowLayout.isMotionEventSplittingEnabled = true

        val shiftKey = row.find { it.name == "Shift" }
        val delKey = row.find { it.name == "Del" }
        val middleKeys = row.filter { it.name != "Shift" && it.name != "Del" && it.name != "kgKey" }

        shiftKey?.let {
            val view = SystemKeyBuilder.forKey(
                service, it, isCaps, buttonHeight, maxKeyCount, onCapsChange, KeyboardTheme.getSystemButtonStyle(service)
            )

            view.setOnLongClickListener {
                onModeChange.invoke(KeyboardMode.SavedStrings)
                true
            }

            val params = LinearLayout.LayoutParams(
                systemKeybWidth, buttonHeight
            )
            view.layoutParams = params

            rowLayout.addView(view)
        }

        val middleContainer = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f)
            clipChildren = false
            clipToPadding = false
        }

        middleKeys.forEach { key ->
            middleContainer.addView(
                LetterKeyBuilder.createLetterKey(service, process(key, mode), isCaps, buttonHeight, keybWidth, onKeyClick = { letter ->
                    if (MyKeyboardService.current_writingSystem == WritingSystem.Bitik) {
                        if (ensureRTLContext(service)) {
                            service.currentInputConnection?.commitText("\u202B", 1)
                        }
                    }
                    service.currentInputConnection?.commitText(letter, 1)
                    TopRowBuilder_Old.onTypedListener?.invoke()
                    if (isCaps && MyKeyboardService.current_writingSystem != WritingSystem.Bitik) {
                        onCapsChange.invoke(false)
                    }
                }, onLongPress = { letter ->
                    letter?.let { service.currentInputConnection?.commitText(it, 1) }
                })
            )
        }

        rowLayout.addView(middleContainer)

        var extraLeft = KeyboardTheme.dpToPx(service, 20)
        var extraRight = KeyboardTheme.dpToPx(service, 20)

        if (MyKeyboardService.keyboardMode == KeyboardMode.Main && MyKeyboardService.current_writingSystem == WritingSystem.Kiril) {
            extraLeft = 0
            extraRight = 0
        }

        rowLayout.post {
            if (middleContainer.childCount > 0) {
                middleContainer.getChildAt(0)?.extendHorizontalHit(extraLeft, 0)
                middleContainer.getChildAt(middleContainer.childCount - 1)?.extendHorizontalHit(0, extraRight)
            }
        }

        delKey?.let {
            val view = SystemKeyBuilder.forKey(service, it, isCaps, buttonHeight, maxKeyCount, onCapsChange)
            val params = LinearLayout.LayoutParams(
                systemKeybWidth, buttonHeight
            )
            view.layoutParams = params
            rowLayout.addView(view)
        }
        return rowLayout
    }

    private fun ensureRTLContext(service: InputMethodService): Boolean {
        val inputConnection = service.currentInputConnection ?: return false
        val textBefore = inputConnection.getTextBeforeCursor(1, 0)
        return textBefore.isNullOrEmpty() || textBefore.last() == '\n'
    }

    fun process(key: KeyEntry, mode: KeyboardMode): KeyEntry {
        var keyToSet = key

        if (mode == KeyboardMode.Main) {
            if (MyKeyboardService.current_writingSystem == WritingSystem.Bitik) {
                if (key.name == "b" && !eb_Def) {
                    keyToSet = getEB(keyToSet)
                    return keyToSet
                }
                if (key.name == "n" && !en_Def) {
                    keyToSet = getEN(keyToSet)
                    return keyToSet
                }

                if (key.name == "s" && as_Def) {
                    keyToSet = getAS(keyToSet)
                    return keyToSet
                }

                if (key.name == "ş" && !esh_Def) {
                    if (!MyKeyboardService.isClassic && MyKeyboardService.current_bitikDialect == BitikDialect.Altay && MyKeyboardService.current_bitikVariant == BitikVariant.SAMAGAN) {
                        keyToSet = getESH(keyToSet)
                    }
                    return keyToSet
                }
            } else {
                if (key.name == "⸮") {
                    keyToSet = key.copy(
                        lowercase = "?", lowerCaseHold = "⸮", lowerCaseRomanization = "⸮", uppercase = "?", upperCaseHold = "⸮", upperCaseRomanization = "⸮"
                    )
                    return keyToSet
                }
            }
        }

        if (mode == KeyboardMode.Emojis) {
            if (key.name == "kg") {
                val a = context.freeTamga_Click
                val b = context.freeTamga_Hold

                keyToSet = key.copy(
                    lowercase = a, lowerCaseHold = b, lowerCaseRomanization = b, uppercase = b, upperCaseHold = b, upperCaseRomanization = a
                )
            }
        }

        return keyToSet
    }

    private fun getEB(key: KeyEntry): KeyEntry {
        return key.copy(
            uppercase = "𐰋", upperCaseHold = "𐰌"
        )
    }

    private fun getEN(key: KeyEntry): KeyEntry {
        return key.copy(
            uppercase = "𐰥", upperCaseHold = "𐰤"
        )
    }

    private fun getAS(key: KeyEntry): KeyEntry {
        return key.copy(
            lowercase = "𐰽", lowerCaseHold = "𐱂"
        )
    }

    private fun getESH(key: KeyEntry): KeyEntry {
        return key.copy(
            uppercase = "𐰿", upperCaseHold = "𐱁"
        )
    }
}