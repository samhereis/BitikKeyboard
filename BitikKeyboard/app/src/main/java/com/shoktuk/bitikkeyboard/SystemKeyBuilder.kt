package com.shoktuk.bitikkeyboard

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.widget.Button
import androidx.core.graphics.drawable.DrawableCompat

object SystemKeyBuilder {

    fun createSystemKey(
        service: InputMethodService, key: KeyEntry, layout: KeyboardLayout, buttonHeight: Int, margin: Int, isCaps: Boolean, onCapsChange: (Boolean) -> Unit
    ): Button {
        return Button(service).apply {
            text = "" // No text—icon only
            gravity = Gravity.CENTER  // Center content
            // Set padding to zero
            setPadding(0, 0, 0, 0)
            compoundDrawablePadding = 0

            textSize = KeyboardTheme.getSystemButtonStyle(service).textSizeSp
            setTextColor(Color.parseColor(KeyboardTheme.getSystemButtonStyle(service).textColor))
            layoutParams = android.widget.LinearLayout.LayoutParams(KeyboardTheme.getSystemButtonWidth(service), buttonHeight).apply {
                marginStart = margin
                marginEnd = margin
            }

            if (key.name == "Shift") {
                if (isCaps) {
                    background = KeyboardTheme.createDrawableFromStyle(service, KeyboardTheme.getSystemButtonStyle(service).copy(fillColor = KeyboardTheme.colorIndexes[2]))
                } else {
                    background = KeyboardTheme.createDrawableFromStyle(service, KeyboardTheme.getSystemButtonStyle(service))
                }
            } else {

                background = KeyboardTheme.createDrawableFromStyle(service, KeyboardTheme.getSystemButtonStyle(service))
            }

            val assetPath = when (key.name) {
                "Shift" -> KeyboardTheme.SHIFT_ICON_FILE
                "Del" -> KeyboardTheme.DELETE_ICON_FILE
                else -> null
            }
            assetPath?.let { path ->
                val iconDrawable: Drawable? = KeyboardTheme.loadAssetDrawable(service, path)
                iconDrawable?.let { d ->
                    val tintedIcon = DrawableCompat.wrap(d).mutate()
                    DrawableCompat.setTint(tintedIcon, Color.parseColor(KeyboardTheme.getSystemButtonStyle(service).textColor))
                    // Set icon size to half the button height
                    val iconSize = buttonHeight / 2
                    tintedIcon.setBounds(0, 0, iconSize, iconSize)
                    setCompoundDrawables(null, tintedIcon, null, null)
                    setPadding(0, KeyboardTheme.getButtonHeight(service) / 4, 0, 0)
                }
            }
            setOnClickListener {
                when (key.name) {
                    "Shift" -> onCapsChange(!isCaps)
                    "Del" -> {
                        if (layout.directionality == 1) {
                            service.currentInputConnection?.deleteSurroundingText(2, 1)
                            service.currentInputConnection?.deleteSurroundingText(2, 1)
                        } else {
                            service.currentInputConnection?.deleteSurroundingText(1, 0)
                        }
                    }
                }
            }
        }
    }
}
