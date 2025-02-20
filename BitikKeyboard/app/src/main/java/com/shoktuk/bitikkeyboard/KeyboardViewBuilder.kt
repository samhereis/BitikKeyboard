package com.shoktuk.bitikkeyboard

import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

object KeyboardViewBuilder {
    /**
     * Builds the complete keyboard view.
     * onModeChange toggles between "letters" and "symbols" mode.
     */
    fun buildKeyboardView(
        service: MyKeyboardService,
        layout: KeyboardLayout,
        isCaps: Boolean,
        onCapsChange: (Boolean) -> Unit,
        onModeChange: (String) -> Unit
    ): LinearLayout {
        val buttonHeight = KeyboardTheme.dpToPx(service, KeyboardTheme.BUTTON_HEIGHT_DP)
        val margin = KeyboardTheme.dpToPx(service, KeyboardTheme.KEY_MARGIN_DP)
        // Calculate the maximum number of keys in any row.
        val maxCount = layout.rows.maxOf { it.size }
        // Compute a fixed key width based on screen width minus margins.
        val screenWidth = service.resources.displayMetrics.widthPixels
        val keyWidth = (screenWidth - (maxCount + 1) * margin) / maxCount

        val container = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Build each row.
        layout.rows.forEach { row ->
            container.addView(createKeyRow(service, row, layout, buttonHeight, margin, isCaps, onCapsChange, keyWidth))
        }

        // Build bottom row.
        container.addView(createBottomRow(service, buttonHeight, margin, layout, onModeChange, keyWidth))
        return container
    }

    private fun createKeyRow(
        service: MyKeyboardService,
        row: List<KeyEntry>,
        layout: KeyboardLayout,
        buttonHeight: Int,
        margin: Int,
        isCaps: Boolean,
        onCapsChange: (Boolean) -> Unit,
        keyWidth: Int
    ): LinearLayout {
        return LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = margin
                bottomMargin = margin
            }
            // Use fixed keyWidth for each key.
            row.forEach { key ->
                if (layout.name == "symbols" && key.name == "Shift") {
                    // Skip Shift in symbols mode.
                } else if (key.name == "Shift" || key.name == "Del") {
                    addView(createSystemKey(service, key, buttonHeight, margin, layout, onCapsChange, keyWidth))
                } else {
                    addView(createLetterKey(service, key, layout, buttonHeight, margin, isCaps, onCapsChange, keyWidth))
                }
            }
        }
    }

    private fun createSystemKey(
        service: MyKeyboardService,
        key: KeyEntry,
        buttonHeight: Int,
        margin: Int,
        layout: KeyboardLayout,
        onCapsChange: (Boolean) -> Unit,
        keyWidth: Int
    ): Button {
        return Button(service).apply {
            when (key.name) {
                "Shift" -> {
                    text = ""
                    setBackgroundResource(KeyboardTheme.ICON_SHIFT)
                    contentDescription = "Shift key"
                    setTextColor(android.graphics.Color.parseColor(KeyboardTheme.SPECIAL_KEY_TEXT_COLOR))
                }
                "Del" -> {
                    text = ""
                    setBackgroundResource(KeyboardTheme.ICON_DEL)
                    contentDescription = "Delete key"
                    setTextColor(android.graphics.Color.parseColor(KeyboardTheme.SPECIAL_KEY_TEXT_COLOR))
                }
            }
            // Use a special background color for system keys.
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(android.graphics.Color.parseColor(KeyboardTheme.SPECIAL_KEY_BACKGROUND_COLOR))
                cornerRadius = KeyboardTheme.BUTTON_CORNER_RADIUS_DP * service.resources.displayMetrics.density
            }
            textSize = KeyboardTheme.MAIN_TEXT_SIZE_SP
            layoutParams = LinearLayout.LayoutParams(keyWidth, buttonHeight).apply {
                marginStart = margin
                marginEnd = margin
            }
            setOnClickListener {
                when (key.name) {
                    "Shift" -> onCapsChange(!service.isCaps)
                    "Del" -> {
                        if (layout.directionality == 1) {
                            service.currentInputConnection?.deleteSurroundingText(1, 0)
                            service.currentInputConnection?.deleteSurroundingText(1, 0)
                        } else {
                            service.currentInputConnection?.deleteSurroundingText(1, 0)
                        }
                    }
                }
            }
        }
    }

    private fun createLetterKey(
        service: InputMethodService,
        key: KeyEntry,
        layout: KeyboardLayout,
        buttonHeight: Int,
        margin: Int,
        isCaps: Boolean,
        onCapsChange: (Boolean) -> Unit,
        keyWidth: Int
    ): View {
        val keyView = LayoutInflater.from(service).inflate(R.layout.keyboard_key, null, false)
        keyView.background = KeyboardTheme.createButtonBackground(service)
        val mainTextView = keyView.findViewById<TextView>(R.id.mainText)
        val subTextView = keyView.findViewById<TextView>(R.id.subText)
        mainTextView.text = if (isCaps) key.uppercase else key.lowercase
        mainTextView.setTextColor(android.graphics.Color.parseColor(KeyboardTheme.LETTER_TEXT_COLOR))
        subTextView.text = if (isCaps) key.upperCaseHint ?: "" else key.lowerCaseHint ?: ""
        keyView.setOnClickListener {
            val letter = if (isCaps) key.uppercase else key.lowercase
            val textToCommit = if (layout.directionality == 1) "\u202B$letter\u202C" else letter
            service.currentInputConnection?.commitText(textToCommit, 1)
            if (isCaps) onCapsChange(false)
        }
        keyView.layoutParams = LinearLayout.LayoutParams(keyWidth, buttonHeight).apply {
            marginStart = margin
            marginEnd = margin
        }
        return keyView
    }

    /**
     * Creates the bottom row.
     * In letters mode: [Symbols] [Space] [":"] [Enter]
     * In symbols mode: [ABC] [Space] [":"] [Delete]
     *
     * For the two middle keys ("Space" and ":") we want them to take all available space.
     * The mode toggle and Enter/Delete keys use fixed width (keyWidth).
     */
    private fun createBottomRow(
        service: InputMethodService,
        buttonHeight: Int,
        margin: Int,
        layout: KeyboardLayout,
        onModeChange: (String) -> Unit,
        keyWidth: Int
    ): LinearLayout {
        return LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
            // Fixed keys for left and right, and expandable for the middle two.
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = margin
                bottomMargin = margin
            }
            // In symbols mode, bottom row: [ABC] [Space] [":"] [Delete]
            if (layout.name == "symbols") {
                addView(Button(service).apply {
                    text = "ABC"
                    height = buttonHeight
                    layoutParams = LinearLayout.LayoutParams(keyWidth, buttonHeight).apply {
                        marginStart = margin
                        marginEnd = margin
                    }
                    setOnClickListener { onModeChange("letters") }
                })
                addView(Button(service).apply {
                    text = "Space"
                    height = buttonHeight
                    // Expand to fill available space.
                    layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f).apply {
                        marginStart = margin
                        marginEnd = margin
                    }
                    setOnClickListener { service.currentInputConnection?.commitText(" ", 1) }
                })
                addView(Button(service).apply {
                    text = ":"
                    textSize = 17f
                    height = buttonHeight
                    // Expand to fill available space.
                    layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f).apply {
                        marginStart = margin
                        marginEnd = margin
                    }
                    setOnClickListener { service.currentInputConnection?.commitText(":", 1) }
                })
                addView(Button(service).apply {
                    text = ""
                    setBackgroundResource(KeyboardTheme.ICON_DEL)
                    height = buttonHeight
                    layoutParams = LinearLayout.LayoutParams(keyWidth, buttonHeight).apply {
                        marginStart = margin
                        marginEnd = margin
                    }
                    setOnClickListener { service.currentInputConnection?.deleteSurroundingText(1, 0) }
                })
            } else {
                // Letters mode: [Symbols] [Space] [":"] [Enter]
                addView(Button(service).apply {
                    text = "Symbols"
                    height = buttonHeight
                    layoutParams = LinearLayout.LayoutParams(keyWidth, buttonHeight).apply {
                        marginStart = margin
                        marginEnd = margin
                    }
                    setOnClickListener { onModeChange("symbols") }
                })
                addView(Button(service).apply {
                    text = "Space"
                    height = buttonHeight
                    layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f).apply {
                        marginStart = margin
                        marginEnd = margin
                    }
                    setOnClickListener { service.currentInputConnection?.commitText(" ", 1) }
                })
                addView(Button(service).apply {
                    text = ":"
                    textSize = 17f
                    height = buttonHeight
                    layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f).apply {
                        marginStart = margin
                        marginEnd = margin
                    }
                    setOnClickListener { service.currentInputConnection?.commitText(":", 1) }
                })
                addView(Button(service).apply {
                    text = "Enter"
                    height = buttonHeight
                    layoutParams = LinearLayout.LayoutParams(keyWidth, buttonHeight).apply {
                        marginStart = margin
                        marginEnd = margin
                    }
                    setOnClickListener { service.currentInputConnection?.commitText("\n", 1) }
                })
            }
        }
    }
}
