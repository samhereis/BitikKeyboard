package com.shoktuk.bitikkeyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout

object KeyboardViewBuilder {

    /**
     * Builds the complete keyboard view.
     *
     * @param onLangChange Callback for language button press.
     */
    fun buildKeyboardView(
        service: InputMethodService,
        layout: KeyboardLayout,
        isCaps: Boolean,
        onCapsChange: (Boolean) -> Unit,
        onModeChange: (String) -> Unit,
        onLangChange: () -> Unit
    ): LinearLayout {
        val buttonHeight = KeyboardTheme.dpToPx(service, KeyboardTheme.BUTTON_HEIGHT_DP)
        val margin = KeyboardTheme.dpToPx(service, KeyboardTheme.KEY_MARGIN_DP)
        // Compute the maximum number of keys in any row.
        val maxCount = layout.rows.maxOf { it.size }
        val screenWidth = service.resources.displayMetrics.widthPixels
        // Calculate a fixed key width for system keys
        val fixedKeyWidth = (screenWidth - (maxCount + 1) * margin) / maxCount

        val container = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor(KeyboardTheme.CONTAINER_BACKGROUND_COLOR))
        }

        // Build each row.
        layout.rows.forEach { row ->
            container.addView(
                createRowLayout(
                    service, row, layout, buttonHeight, margin, fixedKeyWidth,
                    isCaps, onCapsChange
                )
            )
        }

        // Build bottom row, now with language button.
        container.addView(
            BottomRowBuilder.createBottomRow(
                service, layout, buttonHeight, margin,
                onModeChange,
                onLangChange = onLangChange
            )
        )
        return container
    }

    /**
     * Creates a row layout with system keys (Shift/Del) pinned and letter keys centered.
     */
    private fun createRowLayout(
        service: InputMethodService,
        row: List<KeyEntry>,
        layout: KeyboardLayout,
        buttonHeight: Int,
        margin: Int,
        fixedKeyWidth: Int,
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

        // Identify system keys
        val shiftKey = row.find { it.name == "Shift" }
        val delKey = row.find { it.name == "Del" }
        val middleKeys = row.filter { it.name != "Shift" && it.name != "Del" }

        // Add Shift key at far left if present.
        shiftKey?.let {
            rowLayout.addView(
                SystemKeyBuilder.createSystemKey(
                    service, it, layout, buttonHeight, margin, isCaps, onCapsChange
                )
            )
        }

        // Create a middle container that takes remaining space.
        val middleContainer = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f)
        }
        middleKeys.forEach { key ->
            middleContainer.addView(
                LetterKeyBuilder.createLetterKey(
                    service, key, layout, buttonHeight, margin, isCaps, onCapsChange
                )
            )
        }
        rowLayout.addView(middleContainer)

        // Add Delete key at far right if present.
        delKey?.let {
            rowLayout.addView(
                SystemKeyBuilder.createSystemKey(
                    service, it, layout, buttonHeight, margin, isCaps, onCapsChange
                )
            )
        }

        return rowLayout
    }
}
