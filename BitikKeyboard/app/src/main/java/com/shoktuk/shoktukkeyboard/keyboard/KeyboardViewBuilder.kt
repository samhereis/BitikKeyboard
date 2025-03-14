package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.graphics.toColorInt

object KeyboardViewBuilder {

    /**
     * Builds the complete keyboard view.
     *
     * @param onLangChange Callback for language button press.
     */
    fun buildKeyboardView(
        service: InputMethodService, layout: KeyboardLayout, isCaps: Boolean, onCapsChange: (Boolean) -> Unit, onModeChange: (String) -> Unit, onLangChange: () -> Unit
    ): LinearLayout {
        val margin = KeyboardTheme.dpToPx(service, KeyboardTheme.KEY_MARGIN_DP)

        // Get the screen width in pixels.
        val screenWidthPx = service.resources.displayMetrics.widthPixels
        // Define your base design width in dp (e.g., 360 dp for a typical design).
        val baseDesignWidthDp = 360f
        // Convert base design width to pixels.
        val density = service.resources.displayMetrics.density
        val maxWidthPx = (baseDesignWidthDp * density).toInt()
        // Calculate side margin if screen width is larger than max width.
        val sideMarginPx = if (screenWidthPx > maxWidthPx) (screenWidthPx - maxWidthPx) / 2 else 0

        val container = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            // Set width to the lesser of screen width or maxWidthPx, and add side margins if needed.
            layoutParams = LinearLayout.LayoutParams(
                if (screenWidthPx > maxWidthPx) maxWidthPx else ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                leftMargin = sideMarginPx
                rightMargin = sideMarginPx
            }
            setBackgroundColor(KeyboardTheme.CONTAINER_BACKGROUND_COLOR.toColorInt())
        }

        // Use an insets listener if needed to add additional bottom padding (safe area).
        container.setOnApplyWindowInsetsListener { view, insets ->
            val bottomInset = android.view.WindowInsets.Side.BOTTOM
            view.setPadding(0, 0, 0, bottomInset)
            insets
        }

        // Build each row.
        layout.rows.forEach { row ->
            container.addView(
                createRowLayout(
                    service, row, layout, KeyboardTheme.getButtonHeight(), margin,
                    // Calculate a fixed key width for system keys if needed.
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

    /**
     * Creates a row layout with system keys (Shift/Del) pinned and letter keys centered.
     */
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

        // Identify system keys.
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
                    service, key, layout, buttonHeight, margin, isCaps
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
