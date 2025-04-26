package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt

object BottomRowBuilder {
    fun createBottomRow(
        service: InputMethodService, layout: KeyboardLayout, buttonHeight: Int, margin: Int, onModeChange: (String) -> Unit, onLangChange: () -> Unit
    ): LinearLayout {
        val bottomRow = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = margin
                bottomMargin = margin
            }
        }

        if (layout.name == "symbols") {
            bottomRow.addView(
                createSystemAssetButton(
                    service = service, assetPath = null, textToSet = "ABC", buttonHeight = buttonHeight, margin = margin, onClick = {
                        onModeChange("letters")
                    }, buttonStyle = KeyboardTheme.getSystemButtonStyle(service), bigText = false
                )
            )

            bottomRow.addView(
                createExpandableAssetButton(
                    service, KeyboardTheme.SPACE_ICON_FILE, "", buttonHeight, margin, " "
                )
            )

            bottomRow.addView(
                createExpandableAssetButton(
                    service, null, ":", buttonHeight, margin, ":"
                )
            )

            bottomRow.addView(
                createSystemAssetButton(
                    service, KeyboardTheme.DELETE_ICON_FILE, "", buttonHeight, margin,
                    onClick = {
                        service.currentInputConnection?.deleteSurroundingText(2, 0)
                        TopRowBuilder_Old.onTypedListener?.invoke()
                    },
                    buttonStyle = KeyboardTheme.getSystemButtonStyle(service),
                )
            )
        } else {
            bottomRow.addView(
                createSystemAssetButton(
                    service, KeyboardTheme.LANGUAGE_ICON_FILE, "", buttonHeight, margin,
                    onClick = {
                        val imm = service.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showInputMethodPicker()
                        TopRowBuilder_Old.onTypedListener?.invoke()
                    },
                    buttonStyle = KeyboardTheme.getSystemButtonStyle(service),
                )
            )

            bottomRow.addView(
                createSystemAssetButton(
                    service, null, "·", buttonHeight, margin, buttonStyle = KeyboardTheme.getSystemButtonStyle(service), onClick = {
                        service.currentInputConnection?.commitText("·", 1)
                        TopRowBuilder_Old.onTypedListener?.invoke()
                    })
            )

            bottomRow.addView(
                createExpandableAssetButton(
                    service, KeyboardTheme.SPACE_ICON_FILE, "", buttonHeight, margin, " "
                )
            )

            bottomRow.addView(
                createExpandableAssetButton(
                    service, null, ":", buttonHeight, margin, ":"
                )
            )

            bottomRow.addView(
                createSystemAssetButton(
                    service, null, ",", buttonHeight, margin, buttonStyle = KeyboardTheme.getSystemButtonStyle(service), onClick = {
                        service.currentInputConnection?.commitText(",", 1)
                        TopRowBuilder_Old.onTypedListener?.invoke()
                    })
            )

            bottomRow.addView(
                createSystemAssetButton(
                    service, KeyboardTheme.ENTER_ICON_FILE, "", buttonHeight, margin, buttonStyle = KeyboardTheme.getSystemButtonStyle(service), onClick = {
                        service.currentInputConnection?.commitText("\n", 1)
                        TopRowBuilder_Old.onTypedListener?.invoke()
                    })
            )
        }
        return bottomRow
    }

    private fun createSystemAssetButton(
        service: InputMethodService, assetPath: String?, textToSet: String, buttonHeight: Int, margin: Int, onClick: () -> Unit, buttonStyle: ButtonStyle, bigText: Boolean = true
    ): Button {
        return Button(service).apply {
            text = textToSet
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)
            compoundDrawablePadding = 0
            textSize = if (bigText) KeyboardTheme.getLetterButtonStyle(service).textSizeSp.value else KeyboardTheme.getHintButtonTextSize(service).value
            setTextColor(buttonStyle.textColor.toColorInt())
            background = KeyboardTheme.createDrawableFromStyle(service, buttonStyle)
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
                        tintedIcon, buttonStyle.textColor.toColorInt()
                    )
                    val iconSize = buttonHeight / 2
                    tintedIcon.setBounds(0, 0, iconSize, iconSize)
                    setCompoundDrawables(null, tintedIcon, null, null)
                    setPadding(0, KeyboardTheme.getButtonHeight() / 4, 0, 0)
                }
            }
            setOnClickListener {
                onClick()
                TopRowBuilder_Old.onTypedListener?.invoke()
            }
        }
    }

    private fun createExpandableAssetButton(
        service: InputMethodService, assetPath: String?, textToSet: String, buttonHeight: Int, margin: Int, textToCommit: String
    ): Button {
        return Button(service).apply {
            text = textToSet
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)
            compoundDrawablePadding = 0
            textSize = KeyboardTheme.getSystemButtonStyle(service).textSizeSp.value
            setTextColor(KeyboardTheme.getSystemButtonStyle(service).textColor.toColorInt())
            background = KeyboardTheme.createDrawableFromStyle(service, KeyboardTheme.getLetterButtonStyle(service))
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
                    setPadding(0, KeyboardTheme.getButtonHeight() / 4, 0, KeyboardTheme.getButtonHeight() / 4)
                }
            }
            setOnClickListener {
                service.currentInputConnection?.commitText(textToCommit, 1)
                TopRowBuilder_Old.onTypedListener?.invoke()
            }
        }
    }
}
