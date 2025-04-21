package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager

object KeyboardViewBuilder {

    fun buildKeyboardView(
        service: InputMethodService, layout: KeyboardLayout, isCaps: Boolean, onCapsChange: (Boolean) -> Unit, onModeChange: (String) -> Unit, onLangChange: () -> Unit
    ): LinearLayout {
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

        if (layout.name != "symbols") {

            if(SettingsManager.getKeyboardVariant(service) == KeyboardVariant.CLASSIC)
            {
                container.addView(
                    TopRowBuilder_Old.createTopRow(
                        service, layout, (KeyboardTheme.getButtonHeight() / 1.5f).toInt(), margin, onModeChange, onLangChange = onLangChange
                    )
                )
            }
            else{
                container.addView(
                    TopRowBuilder.createTopRow(
                        service, layout, (KeyboardTheme.getButtonHeight() / 1.5f).toInt(), margin, onModeChange, onLangChange = onLangChange
                    )
                )
            }
        }

        layout.rows.forEach { row ->
            container.addView(
                createRowLayout(
                    service, row, layout, KeyboardTheme.getButtonHeight(), margin,
                    isCaps, onCapsChange
                )
            )
        }

        // Build bottom row, now with language button.
        container.addView(
            BottomRowBuilder.createBottomRow(
                service, layout, KeyboardTheme.getButtonHeight(), margin, onModeChange, onLangChange = onLangChange
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
        val middleKeys = row.filter { it.name != "Shift" && it.name != "Del" }

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
            middleContainer.addView(LetterKeyBuilder.createLetterKey(service, key, layout, buttonHeight, margin, isCaps, onKeyClick = { letter ->
                if (ensureRTLContext(service)) {
                    service.currentInputConnection?.commitText("\u202B", 1)
                }
                service.currentInputConnection?.commitText(letter, 1)
                TopRowBuilder_Old.onTypedListener?.invoke()
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
}
