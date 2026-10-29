package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService.Companion.context
import com.shoktuk.shoktukkeyboard.project.data.AJ_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ANG_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.AS_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.EB_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EK_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EN_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ESH_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.Latin_CH
import com.shoktuk.shoktukkeyboard.project.data.Latin_NG
import com.shoktuk.shoktukkeyboard.project.data.Latin_O
import com.shoktuk.shoktukkeyboard.project.data.Latin_Variant
import com.shoktuk.shoktukkeyboard.project.data.Latin_ZH
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ajVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.angVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.asVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ebVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ekVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.enVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.eshVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.freeTamga_Click
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.freeTamga_Hold
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinNG
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinO
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinCH
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinZH
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

object KeyboardViewBuilder {
    fun buildKeyboardView(
        service: InputMethodService,
        layout: KeyboardLayout,
        isCaps: Boolean,
        mode: KeyboardMode,
        maxKeyCount: Int,
        onCapsChange: (Boolean) -> Unit,
        onModeChange: (KeyboardMode) -> Unit,
    ): LinearLayout {
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

        var isBitik = MyKeyboardService.current_writingSystem == WritingSystem.Bitik
        var isLeftSide = isBitik || MyKeyboardService.current_writingSystem == WritingSystem.Arab

        if (MyKeyboardService.current_writingSystem == WritingSystem.Kiril) {
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
        // On the symbols screen the free button is a system key that takes up the row's spare width.
        val freeKey = if (mode == KeyboardMode.Symbols || mode == KeyboardMode.Emojis) row.find { it.name == "kg" } else null
        val middleKeys = row.filter { it.name != "Shift" && it.name != "Del" && it.name != "kgKey" && it != freeKey }

        val isArab = MyKeyboardService.current_writingSystem == WritingSystem.Arab

        // Arabic has no case: its shift slot goes to the wider backspace (savables live in the top bar).
        shiftKey?.takeIf { !isArab }?.let {
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

        // On the symbols screen the free button is the shift slot's system key: same size and look.
        freeKey?.let { key ->
            val entry = process(key, mode)
            val click = entry.lowercase.orEmpty()
            val hold = entry.lowerCaseAlternates?.firstOrNull()
            val view = SystemKeyBuilder.systemButton_Text(
                service = service, text = click, buttonHeight = buttonHeight, onClick = {
                    onKeyPressed?.invoke(service.currentInputConnection, click)
                }, onLongClick = {
                    hold?.let { onKeyPressed?.invoke(service.currentInputConnection, it) }
                })
            view.layoutParams = LinearLayout.LayoutParams(systemKeybWidth, buttonHeight)
            rowLayout.addView(view)
        }

        val middleContainer = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = if (isArab) LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, buttonHeight)
            else LinearLayout.LayoutParams(0, buttonHeight, 1f)
            clipChildren = false
            clipToPadding = false
        }

        middleKeys.forEach { key ->
            middleContainer.addView(
                LetterKeyBuilder.createLetterKey(service, process(key, mode), isCaps, buttonHeight, keybWidth, onKeyClick = { letter ->
                    onKeyPressed?.invoke(service.currentInputConnection, letter)
                }, onLongPress = { letter ->
                    letter?.let { onKeyPressed?.invoke(service.currentInputConnection, letter) }
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
                if (isArab) 0 else systemKeybWidth, buttonHeight
            ).apply { if (isArab) weight = 1f }
            view.layoutParams = params

            if (isLeftSide) {
                view.scaleX = -1f
            }
            rowLayout.addView(view)
        }
        return rowLayout
    }

    /** Per-key touches that are not letter swaps (swaps live in [LayoutSwaps]): the free symbol key. */
    fun process(key: KeyEntry, mode: KeyboardMode): KeyEntry {
        var keyToSet = key

        if (mode == KeyboardMode.Emojis) {
            if (key.name == "kg") {
                val a = context.freeTamga_Click
                val b = context.freeTamga_Hold

                keyToSet = key.copy(
                    lowercase = a, lowerCaseAlternates = listOf(b), lowerCaseRomanization = b, uppercase = b, upperCaseAlternates = listOf(b), upperCaseRomanization = a
                )
            }
        }

        return keyToSet
    }
}
