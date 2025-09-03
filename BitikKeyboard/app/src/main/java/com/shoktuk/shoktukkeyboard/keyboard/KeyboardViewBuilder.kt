package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import com.shoktuk.shoktukkeyboard.project.data.AS_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.A_Letter_Variannt
import com.shoktuk.shoktukkeyboard.project.data.EB_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EN_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ESH_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.E_Letter_Variannt
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

object KeyboardViewBuilder {
    var a_Def = SettingsManager.getA_Variant(MyKeyboardService.context) == A_Letter_Variannt.Default
    var e_Def = SettingsManager.getE_Variant(MyKeyboardService.context) == E_Letter_Variannt.Default
    var eb_Def = SettingsManager.getEB_Variant(MyKeyboardService.context) == EB_Letter_Variant.Default
    var en_Def = SettingsManager.getEN_Variant(MyKeyboardService.context) == EN_Letter_Variant.Default
    var as_Def = SettingsManager.getAS_Variant(MyKeyboardService.context) == AS_Letter_Variant.Default
    var esh_Def = SettingsManager.getES_Variant(MyKeyboardService.context) == ESH_Letter_Variant.Default

    fun buildKeyboardView(
        service: InputMethodService, layout: KeyboardLayout, onCapsChange: (Boolean) -> Unit, onModeChange: (String) -> Unit, onAlphabetChange: () -> Unit
    ): LinearLayout {
        e_Def = SettingsManager.getE_Variant(MyKeyboardService.context) == E_Letter_Variannt.Default
        eb_Def = SettingsManager.getEB_Variant(MyKeyboardService.context) == EB_Letter_Variant.Default
        en_Def = SettingsManager.getEN_Variant(MyKeyboardService.context) == EN_Letter_Variant.Default
        as_Def = SettingsManager.getAS_Variant(MyKeyboardService.context) == AS_Letter_Variant.Default
        esh_Def = SettingsManager.getES_Variant(MyKeyboardService.context) == ESH_Letter_Variant.Default

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

        if (SettingsManager.getKeyboardVariant(service) == KeyboardVariant.CLASSIC && MyKeyboardService.currentAlphabet == "bitik") {
            container.addView(
                TopRowBuilder_Old.createTopRow(
                    service, (KeyboardTheme.getButtonHeight() / 1.5f).toInt(), onModeChange, onAlphabetChange
                )
            )
        } else {
            container.addView(
                TopRowBuilder.createTopRow(
                    service, layout, (KeyboardTheme.getButtonHeight() / 1.5f).toInt(), onAlphabetChange
                )
            )
        }

        layout.rows.forEach { row ->
            container.addView(
                createRowLayout(
                    service, row, layout, KeyboardTheme.getButtonHeight(), onCapsChange
                )
            )
        }

        container.addView(
            BottomRowBuilder.createBottomRow(
                service, KeyboardTheme.getButtonHeight(), onModeChange
            )
        )
        return container
    }

    private fun createRowLayout(
        service: InputMethodService, row: List<KeyEntry>, layout: KeyboardLayout, buttonHeight: Int, onCapsChange: (Boolean) -> Unit
    ): LinearLayout {
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
            rowLayout.addView(
                SystemKeyBuilder.forKey(
                    service, it, buttonHeight, onCapsChange
                )
            )
        }

        val middleContainer = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f)
            clipChildren = false
            clipToPadding = false
        }
        middleKeys.forEach { key ->
            middleContainer.addView(LetterKeyBuilder.createLetterKey(service, process(key), buttonHeight, onKeyClick = { letter ->
                if (MyKeyboardService.currentAlphabet == "bitik") {
                    if (ensureRTLContext(service)) {
                        service.currentInputConnection?.commitText("\u202B", 1)
                    }
                }
                service.currentInputConnection?.commitText(letter, 1)
                TopRowBuilder_Old.onTypedListener?.invoke()
                if (MyKeyboardService.isCaps && MyKeyboardService.currentAlphabet != "bitik") {
                    onCapsChange.invoke(false)
                }
            }, onLongPress = { letter ->
                letter?.let { service.currentInputConnection?.commitText(it, 1) }
            }))
        }

        rowLayout.addView(middleContainer)

        val extraLeft = KeyboardTheme.dpToPx(service, 20)
        val extraRight = KeyboardTheme.dpToPx(service, 20)
        rowLayout.post {
            if (middleContainer.childCount > 0) {
                middleContainer.getChildAt(0)?.extendHorizontalHit(extraLeft, 0)
                middleContainer.getChildAt(middleContainer.childCount - 1)?.extendHorizontalHit(0, extraRight)
            }
        }

        delKey?.let {
            rowLayout.addView(
                SystemKeyBuilder.forKey(
                    service, it, buttonHeight, onCapsChange
                )
            )
        }
        return rowLayout
    }

    private fun ensureRTLContext(service: InputMethodService): Boolean {
        val inputConnection = service.currentInputConnection ?: return false
        val textBefore = inputConnection.getTextBeforeCursor(1, 0)
        return textBefore.isNullOrEmpty() || textBefore.last() == '\n'
    }

    fun process(key: KeyEntry): KeyEntry {
        var keyToSet = key

        if (MyKeyboardService.currentAlphabet == "bitik") {
            if (key.name == "a") {
                if (!a_Def) {
                    keyToSet = getA(keyToSet)
                }
                if (MyKeyboardService.isClassic && e_Def) {
                    keyToSet = getE(keyToSet)
                }

                return keyToSet
            }

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
                if (!MyKeyboardService.isClassic && MyKeyboardService.currentLanguage == "enesay" && MyKeyboardService.currentVariant == KeyboardVariant.SAMAGAN) {
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
        return keyToSet
    }

    private fun getA(key: KeyEntry): KeyEntry {
        return key.copy(
            lowercase = "𐰀", lowerCaseHold = "𐰁",
        )
    }

    private fun getE(key: KeyEntry): KeyEntry {
        return key.copy(
            uppercase = "𐰅",
            upperCaseRomanization = "e",
            upperCaseRomanization_Alt = "",
            upperCaseHold = "𐰂",
        )
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