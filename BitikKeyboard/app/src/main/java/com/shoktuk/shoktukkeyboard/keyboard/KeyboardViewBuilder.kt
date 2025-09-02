package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.shoktuk.shoktukkeyboard.project.data.AS_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.E_Letter_Variannt
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

object KeyboardViewBuilder {
    var isCLassing = false
    var eE = false
    var aS_as_SU_Tamge = false
    var isTamga = true

    fun buildKeyboardView(
        service: InputMethodService,
        layout: KeyboardLayout,
        onCapsChange: (Boolean) -> Unit,
        onModeChange: (String) -> Unit,
        onAlphabetChange: () -> Unit
    ): LinearLayout {
        isCLassing = SettingsManager.getKeyboardVariant(service) == KeyboardVariant.CLASSIC
        eE = SettingsManager.getEVariant(service) == E_Letter_Variannt.Default
        aS_as_SU_Tamge = SettingsManager.getASVariant(service) == AS_Letter_Variant.Default
        this.isTamga = isTamga

        val screenWidthPx = service.resources.displayMetrics.widthPixels
        val baseDesignWidthDp = 360f
        val density = service.resources.displayMetrics.density
        val maxWidthPx = (baseDesignWidthDp * density).toInt()
        val sideMarginPx = if (screenWidthPx > maxWidthPx) (screenWidthPx - maxWidthPx) / 2 else 0

        val container = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                if (screenWidthPx > maxWidthPx) maxWidthPx else ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                leftMargin = sideMarginPx
                rightMargin = sideMarginPx
            }
            setBackgroundColor(KeyboardTheme.containerBg())
            // CHANGED: mark as host so KeyView can find it
            tag = KeyPreviewOverlay.TAG_HOST
        }
        container.clipChildren = false
        container.clipToPadding = false
        KeyPreviewOverlay.ensureAttached(container)

        if (SettingsManager.getKeyboardVariant(service) == KeyboardVariant.CLASSIC && MyKeyboardService.currentAlphabet == "bitik") {
            container.addView(
                TopRowBuilder_Old.createTopRow(
                    service,
                    (KeyboardTheme.getButtonHeight() / 1.5f).toInt(),
                    onModeChange,
                    onAlphabetChange
                )
            )
        } else {
            container.addView(
                TopRowBuilder.createTopRow(
                    service,
                    layout,
                    (KeyboardTheme.getButtonHeight() / 1.5f).toInt(),
                    onAlphabetChange
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
            middleContainer.addView(LetterKeyBuilder.createLetterKey(service, process(key), buttonHeight, isTamga, onKeyClick = { letter ->
                if (MyKeyboardService.currentAlphabet == "bitik") {
                    if (ensureRTLContext(service)) {
                        service.currentInputConnection?.commitText("\u202B", 1)
                    }
                }
                service.currentInputConnection?.commitText(letter, 1)
                TopRowBuilder_Old.onTypedListener?.invoke()
                if (MyKeyboardService.currentAlphabet != "bitik") {
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
            //rowLayout.getChildAt(0)?.extendHorizontalHit(extraLeft, 0)
            //rowLayout.getChildAt(rowLayout.childCount - 1)?.extendHorizontalHit(0, extraRight)
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

@Composable
fun ColorPreviewBox(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
            contentAlignment = Alignment.Center
    ) {
        Text(text = label)
    }
}

@Preview(name = "Keyboard – Light", showBackground = true)
@Composable
fun PreviewKeyboard_Light() {
    ShoktukKeyboardTheme(darkTheme = false) {
        ColorPreviewBox("Light")
    }
}

@Preview(name = "Keyboard – Dark", showBackground = true)
@Composable
fun PreviewKeyboard_Dark() {
    ShoktukKeyboardTheme(darkTheme = true) {
        ColorPreviewBox("Dark")
    }
}