package com.shoktuk.bitikkeyboard

import android.graphics.Color
import android.graphics.drawable.Drawable
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
        onModeChange: (String) -> Unit
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
            // Symbols mode: [ABC] [Space] [":"] [Delete]
            bottomRow.addView(createSystemAssetButton(
                service, null, "ABC", buttonHeight, margin
            ) {
                onModeChange("letters")
            })
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
            bottomRow.addView(createSystemAssetButton(
                service, KeyboardTheme.DELETE_ICON_FILE, "", buttonHeight, margin
            ) {
                service.currentInputConnection?.deleteSurroundingText(2, 1)
                service.currentInputConnection?.deleteSurroundingText(2, 1)
            })
        } else {
            // Letters mode: [Symbols] [Space] [":"] [Enter]
            bottomRow.addView(createSystemAssetButton(
                service, null, "123", buttonHeight, margin
            ) {
                onModeChange("symbols")
            })
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
            bottomRow.addView(createSystemAssetButton(
                service, KeyboardTheme.ENTER_ICON_FILE, "", buttonHeight, margin
            ) {
                service.currentInputConnection?.commitText("\n", 1)
            })
        }
        return bottomRow
    }

    private fun createSystemAssetButton(
        service: InputMethodService,
        assetPath: String?,
        textToSet: String,
        buttonHeight: Int,
        margin: Int,
        onClick: () -> Unit
    ): Button {
        val systemKeyWidthPx = KeyboardTheme.dpToPx(service, KeyboardTheme.SYSTEM_BUTTON_WIDTH_DP)
        return Button(service).apply {
            text = textToSet
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)
            compoundDrawablePadding = 0
            textSize = KeyboardTheme.systemButtonStyle.textSizeSp
            setTextColor(Color.parseColor(KeyboardTheme.systemButtonStyle.textColor))
            background =
                KeyboardTheme.createDrawableFromStyle(service, KeyboardTheme.systemButtonStyle)
            layoutParams = LinearLayout.LayoutParams(systemKeyWidthPx, buttonHeight).apply {
                marginStart = margin
                marginEnd = margin
            }
            assetPath?.let { path ->
                val iconDrawable: Drawable? = KeyboardTheme.loadAssetDrawable(service, path)
                iconDrawable?.let { d ->
                    val tintedIcon = DrawableCompat.wrap(d).mutate()
                    DrawableCompat.setTint(
                        tintedIcon, Color.parseColor(KeyboardTheme.systemButtonStyle.textColor)
                    )
                    val iconSize = buttonHeight / 2
                    tintedIcon.setBounds(0, 0, iconSize, iconSize)
                    setCompoundDrawables(null, tintedIcon, null, null)


                    setPadding(0, KeyboardTheme.BUTTON_HEIGHT_DP / 2, 0, 0)
                }
            }
            setOnClickListener { onClick() }
        }
    }

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
            textSize = KeyboardTheme.letterButtonStyle.textSizeSp
            setTextColor(Color.parseColor(KeyboardTheme.letterButtonStyle.textColor))
            background =
                KeyboardTheme.createDrawableFromStyle(service, KeyboardTheme.letterButtonStyle)
            layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f).apply {
                marginStart = margin
                marginEnd = margin
            }
            assetPath?.let { path ->
                val iconDrawable: Drawable? = KeyboardTheme.loadAssetDrawable(service, path)
                iconDrawable?.let { d ->
                    val tintedIcon = DrawableCompat.wrap(d).mutate()
                    val iconSize = buttonHeight / 2
                    tintedIcon.setBounds(0, 0, iconSize, iconSize)
                    setCompoundDrawables(null, tintedIcon, null, null)

                    setPadding(0, KeyboardTheme.BUTTON_HEIGHT_DP / 2, 0, 0)
                }
            }
            setOnClickListener {
                service.currentInputConnection?.commitText(textToCommit, 1)
            }
        }
    }
}
