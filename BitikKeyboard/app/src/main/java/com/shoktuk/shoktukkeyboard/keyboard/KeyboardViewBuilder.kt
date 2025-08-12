package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.runtime.traceEventEnd
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.project.data.AS_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.E_Letter_Variannt
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager
import com.shoktuk.shoktukkeyboard.project.data.TamgaTranscription
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.systems.JSTranscriber

object KeyboardViewBuilder {
    var isCLassing = false;
    var textTranscription = TextTranscription.On;
    var letterTranscription = false;
    var eE = false;
    var aS_as_SU_Tamge = false;
    var isTamga = true;

    fun buildKeyboardView(
        service: InputMethodService, layout: KeyboardLayout, isCaps: Boolean, isTamga: Boolean, onCapsChange: (Boolean) -> Unit, onModeChange: (String) -> Unit, onAlphabetChange: () -> Unit
    ): LinearLayout {
        isCLassing = SettingsManager.getKeyboardVariant(service) == KeyboardVariant.CLASSIC;
        textTranscription = SettingsManager.getTextTranscription(service);
        letterTranscription = SettingsManager.getLeterTranscription(service) == TamgaTranscription.On;
        eE = SettingsManager.getEVariant(service) == E_Letter_Variannt.E_E;
        aS_as_SU_Tamge = SettingsManager.getASVariant(service) == AS_Letter_Variant.AS_SU;
        this.isTamga = isTamga;

        val margin = KeyboardTheme.dpToPx(service, KeyboardTheme.KEY_MARGIN_DP)

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
            setBackgroundColor(KeyboardTheme.CONTAINER_BACKGROUND_COLOR.toColorInt())
        }

        container.setOnApplyWindowInsetsListener { view, insets ->
            val bottomInset = android.view.WindowInsets.Side.BOTTOM
            view.setPadding(0, 0, 0, bottomInset)
            insets
        }

        if (SettingsManager.getKeyboardVariant(service) == KeyboardVariant.CLASSIC && MyKeyboardService.currentAlphabet == "bitik") {
            container.addView(
                TopRowBuilder_Old.createTopRow(
                    service, (KeyboardTheme.getButtonHeight() / 1.5f).toInt(), textTranscription, margin, onModeChange, onAlphabetChange
                )
            )
        } else {
            container.addView(
                TopRowBuilder.createTopRow(
                    service, layout, (KeyboardTheme.getButtonHeight() / 1.5f).toInt(), margin, onModeChange, onAlphabetChange
                )
            )
        }

        layout.rows.forEach { row ->
            container.addView(
                createRowLayout(
                    service, row, layout, KeyboardTheme.getButtonHeight(), margin, isCaps, onCapsChange
                )
            )
        }

        container.addView(
            BottomRowBuilder.createBottomRow(
                service, KeyboardTheme.getButtonHeight(), margin
            )
        )
        return container
    }

    private fun createRowLayout(
        service: InputMethodService, row: List<KeyEntry>, layout: KeyboardLayout, buttonHeight: Int, margin: Int, isCaps: Boolean, onCapsChange: (Boolean) -> Unit
    ): LinearLayout {
        val rowLayout = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = margin
                bottomMargin = margin
            }
        }

        val shiftKey = row.find { it.name == "Shift" }
        val delKey = row.find { it.name == "Del" }
        val middleKeys = row.filter { it.name != "Shift" && it.name != "Del" && it.name != "kgKey" }

        shiftKey?.let {
            rowLayout.addView(
                SystemKeyBuilder.createSystemKey(
                    service, it, layout, buttonHeight, margin, isCaps, onCapsChange
                )
            )
        }

        val middleContainer = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f)
        }
        middleKeys.forEach { key ->
            middleContainer.addView(LetterKeyBuilder.createLetterKey(service, process(key), buttonHeight, margin, isCaps, isTamga, letterTranscription, onKeyClick = { letter ->
                if (MyKeyboardService.currentAlphabet == "bitik") {
                    if (ensureRTLContext(service)) {
                        service.currentInputConnection?.commitText("\u202B", 1)
                    }
                }
                service.currentInputConnection?.commitText(letter, 1)
                TopRowBuilder_Old.onTypedListener?.invoke()

                if (MyKeyboardService.currentAlphabet != "bitik") {
                    onCapsChange?.invoke(false)
                }
            }, onLongPress = { letter ->
                letter?.let { service.currentInputConnection?.commitText(it, 1) }
            }))
        }

        rowLayout.addView(middleContainer)

        delKey?.let {
            rowLayout.addView(
                SystemKeyBuilder.createSystemKey(
                    service, it, layout, buttonHeight, margin, isCaps, onCapsChange
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
        var keyToSet = key;

        if (MyKeyboardService.currentAlphabet == "bitik") {
            if (key.name == "а" && isCLassing && eE) {
                keyToSet = key.copy(
                    lowercase = "𐰁",
                    lowerCaseRomanization = "a",
                    lowerCaseRomanization_Alt = "",
                    lowerCaseHold = "𐰀",
                    uppercase = "𐰅",
                    upperCaseRomanization = "e",
                    upperCaseRomanization_Alt = "",
                    upperCaseHold = "𐰂",
                )
            }

            if (key.name == "s" && aS_as_SU_Tamge) {
                keyToSet = key.copy(
                    lowercase = "𐰽", lowerCaseHold = "𐱂"
                )
            }
        } else {
            if (key.name == "⸮") {
                keyToSet = key.copy(
                    lowercase = "?", lowerCaseHold = "⸮", lowerCaseRomanization = "⸮", uppercase = "?", upperCaseHold = "⸮", upperCaseRomanization = "⸮"
                )
            }
        }

        return keyToSet
    }
}
