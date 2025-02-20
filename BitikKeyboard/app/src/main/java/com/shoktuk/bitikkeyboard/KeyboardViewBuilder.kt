package com.shoktuk.bitikkeyboard

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

object KeyboardViewBuilder {

    fun buildKeyboardView(
        service: InputMethodService,
        layout: KeyboardLayout,
        isCaps: Boolean,
        onCapsChange: (Boolean) -> Unit,
        onModeChange: (String) -> Unit
    ): LinearLayout {
        val container = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(
                android.graphics.Color.parseColor(KeyboardTheme.CONTAINER_BACKGROUND_COLOR)
            )
        }

        val buttonHeight = KeyboardTheme.dpToPx(service, KeyboardTheme.BUTTON_HEIGHT_DP)
        val margin = KeyboardTheme.dpToPx(service, KeyboardTheme.KEY_MARGIN_DP)

        // Build each row
        layout.rows.forEach { row ->
            container.addView(
                createRowLayout(
                    service,
                    row,
                    layout,
                    buttonHeight,
                    margin,
                    isCaps,
                    onCapsChange
                )
            )
        }

        // Build bottom row
        container.addView(
            BottomRowBuilder.createBottomRow(
                service, layout, buttonHeight, margin,
                onModeChange
            )
        )

        return container
    }

    /**
     * Creates a row. If Shift and Delete exist, pin them left/right; otherwise, just add keys in center.
     */
    private fun createRowLayout(
        service: InputMethodService,
        row: List<KeyEntry>,
        layout: KeyboardLayout,
        buttonHeight: Int,
        margin: Int,
        isCaps: Boolean,
        onCapsChange: (Boolean) -> Unit
    ): LinearLayout {
        val rowLayout = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = margin
                bottomMargin = margin
            }
        }

        // Find shift and delete keys
        val shiftKey = row.find { it.name == "Shift" }
        val delKey = row.find { it.name == "Del" }
        // Middle keys are letters (and anything else that's not Shift/Del)
        val middleKeys = row.filter { it.name != "Shift" && it.name != "Del" }

        // SHIFT pinned left if present
        shiftKey?.let {
            rowLayout.addView(
                SystemKeyBuilder.createSystemKey(
                    service,
                    it,
                    layout,
                    buttonHeight,
                    margin,
                    isCaps,
                    onCapsChange
                )
            )
        }

        // Middle container for letter keys, centered
        val middleContainer = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f)
        }
        middleKeys.forEach { key ->
            middleContainer.addView(
                LetterKeyBuilder.createLetterKey(
                    service,
                    key,
                    layout,
                    buttonHeight,
                    margin,
                    isCaps,
                    onCapsChange
                )
            )
        }
        rowLayout.addView(middleContainer)

        // DEL pinned right if present
        delKey?.let {
            rowLayout.addView(
                SystemKeyBuilder.createSystemKey(
                    service,
                    it,
                    layout,
                    buttonHeight,
                    margin,
                    isCaps,
                    onCapsChange
                )
            )
        }

        return rowLayout
    }
}
