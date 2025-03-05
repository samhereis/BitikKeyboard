package com.shoktuk.bitikkeyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.graphics.drawable.DrawableCompat

object BottomRowBuilder {
    fun createBottomRow(
        service: InputMethodService,
        layout: KeyboardLayout,
        buttonHeight: Int,
        margin: Int,
        onModeChange: (String) -> Unit,
        onLangChange: () -> Unit
    ): LinearLayout {
        val bottomRow = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = margin
                bottomMargin = margin
            }
        }

        if (layout.name == "symbols") {
            bottomRow.addView(
                createSystemAssetButton(
                    service = service,
                    assetPath = null, // No icon for "ABC" in this example
                    textToSet = "ABC",
                    buttonHeight = buttonHeight,
                    margin = margin,
                    onClick = {
                        onModeChange("letters")
                    },
                    buttonStyle = KeyboardTheme.getSystemButtonStyle(service),
                )
            )
            // 3) Space
            bottomRow.addView(
                createExpandableAssetButton(
                    service, KeyboardTheme.SPACE_ICON_FILE, "", buttonHeight, margin, " "
                )
            )
            // 4) ":"
            bottomRow.addView(
                createExpandableAssetButton(
                    service, null, ":", buttonHeight, margin, ":"
                )
            )
            // 5) Delete
            bottomRow.addView(
                createSystemAssetButton(
                    service, KeyboardTheme.DELETE_ICON_FILE, "", buttonHeight, margin,
                    onClick = {
                        // This is your delete logic
                        service.currentInputConnection?.deleteSurroundingText(1, 0)
                    },
                    buttonStyle = KeyboardTheme.getSystemButtonStyle(service),
                )
            )
        } else {
            bottomRow.addView(
                createSystemAssetButton(
                    service = service,
                    assetPath = null,
                    textToSet = layout.languageCode,
                    buttonHeight = buttonHeight,
                    onClick = { onLangChange() },
                    margin = margin,
                    buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
                )
            )
            // 2) "123" button
            bottomRow.addView(
                createSystemAssetButton(
                    service = service,
                    assetPath = null,
                    textToSet = "123",
                    buttonHeight = buttonHeight,
                    margin = margin,
                    onClick = {
                        onModeChange("symbols")
                    },
                    buttonStyle = KeyboardTheme.getSystemButtonStyle(service),
                )
            )
            // 3) Space
            bottomRow.addView(
                createExpandableAssetButton(
                    service, KeyboardTheme.SPACE_ICON_FILE, "", buttonHeight, margin, " "
                )
            )
            // 4) ":"
            bottomRow.addView(
                createExpandableAssetButton(
                    service, null, ":", buttonHeight, margin, ":"
                )
            )
            // 5) Enter
            bottomRow.addView(
                createSystemAssetButton(
                    service,
                    KeyboardTheme.ENTER_ICON_FILE,
                    "",
                    buttonHeight,
                    margin,
                    buttonStyle = KeyboardTheme.getSystemButtonStyle(service),
                    onClick = {
                        service.currentInputConnection?.commitText("\n", 1)
                    })
            )
        }
        return bottomRow
    }

    /**
     * Creates a system button (fixed width) that may or may not have an icon from assets,
     * plus optional text.
     */
    private fun createSystemAssetButton(
        service: InputMethodService,
        assetPath: String?,
        textToSet: String,
        buttonHeight: Int,
        margin: Int,
        onClick: () -> Unit,
        buttonStyle: ButtonStyle,
    ): Button {

        return Button(service).apply {
            text = textToSet
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)
            compoundDrawablePadding = 0
            textSize = buttonStyle.textSizeSp
            setTextColor(Color.parseColor(buttonStyle.textColor))
            background =
                KeyboardTheme.createDrawableFromStyle(service, buttonStyle)
            layoutParams = LinearLayout.LayoutParams(KeyboardTheme.getSystemButtonWidth(service), buttonHeight).apply {
                marginStart = margin
                marginEnd = margin
            }
            // If there's an icon to load
            assetPath?.let { path ->
                val iconDrawable = KeyboardTheme.loadAssetDrawable(service, path)
                iconDrawable?.let { d ->
                    val tintedIcon = DrawableCompat.wrap(d).mutate()
                    DrawableCompat.setTint(
                        tintedIcon,
                        Color.parseColor(buttonStyle.textColor)
                    )
                    val iconSize = buttonHeight / 2
                    tintedIcon.setBounds(0, 0, iconSize, iconSize)
                    setCompoundDrawables(null, tintedIcon, null, null)
                    setPadding(0, KeyboardTheme.getButtonHeight(service) / 4, 0, 0)
                }
            }
            setOnClickListener { onClick() }
        }
    }

    /**
     * Creates an expandable button (weight=1) that might have an icon from assets or just text.
     */
    private fun createExpandableAssetButton(
        service: InputMethodService,
        assetPath: String?,
        textToSet: String,
        buttonHeight: Int,
        margin: Int,
        textToCommit: String
    ): Button {
        return Button(service).apply {
            text = textToSet
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)
            compoundDrawablePadding = 0
            textSize = KeyboardTheme.getSystemButtonStyle(service).textSizeSp
            setTextColor(Color.parseColor(KeyboardTheme.getSystemButtonStyle(service).textColor))
            background =
                KeyboardTheme.createDrawableFromStyle(service, KeyboardTheme.getLetterButtonStyle(service))
            layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f).apply {
                marginStart = margin
                marginEnd = margin
            }
            // If there's an icon to load
            assetPath?.let { path ->
                val iconDrawable = KeyboardTheme.loadAssetDrawable(service, path)
                iconDrawable?.let { d ->
                    val tintedIcon = DrawableCompat.wrap(d).mutate()
                    val iconSize = buttonHeight / 2
                    tintedIcon.setBounds(0, 0, iconSize, iconSize)
                    setCompoundDrawables(null, tintedIcon, null, null)
                    setPadding(0, KeyboardTheme.getButtonHeight(service) / 4, 0, KeyboardTheme.getButtonHeight(service) / 4)
                }
            }
            setOnClickListener {
                service.currentInputConnection?.commitText(textToCommit, 1)
            }
        }
    }
}
