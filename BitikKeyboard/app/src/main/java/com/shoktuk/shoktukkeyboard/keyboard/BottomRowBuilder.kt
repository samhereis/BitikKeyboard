package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.ui.theme.ButtonStyle
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

object BottomRowBuilder {
    fun createBottomRow(
        service: InputMethodService, buttonHeight: Int, onModeChange: (String) -> Unit,
    ): LinearLayout {
        var isBitik = MyKeyboardService.currentAlphabet == "bitik"
        var comma = if (isBitik) "⹁" else ","
        var dot = if (isBitik) "·" else "."

        val bottomRow = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = MyKeyboardService.buttonMargin
                bottomMargin = MyKeyboardService.buttonMargin
            }
        }

        bottomRow.addView(
            SystemKeyBuilder.systemButton_Text(
                service, "123", buttonHeight, onClick = { onModeChange("symbols") }
            )
        )

        bottomRow.addView(
            SystemKeyBuilder.systemButton_Text(
                service, dot, buttonHeight, onClick = {
                    service.currentInputConnection?.commitText(dot, 1)
                    TopRowBuilder_Old.onTypedListener?.invoke()
                })
        )

        bottomRow.addView(
            SystemKeyBuilder.expandableSystemButton_Icon(
                service, KeyboardTheme.SPACE_ICON_FILE, " ", buttonHeight,
            )
        )

        if (isBitik) {
            bottomRow.addView(
                SystemKeyBuilder.expandableSystemButton_Text(
                    service, "⁚", buttonHeight,  "⁚"
                )
            )
        }

        bottomRow.addView(
            SystemKeyBuilder.systemButton_Text(
                service, comma, buttonHeight, onClick = {
                    service.currentInputConnection?.commitText(comma, 1)
                    TopRowBuilder_Old.onTypedListener?.invoke()
                })
        )

        bottomRow.addView(
            SystemKeyBuilder.systemButton_Icon(
                service, KeyboardTheme.ENTER_ICON_FILE, buttonHeight, onClick = {
                    service.currentInputConnection?.commitText("\n", 1)
                    TopRowBuilder_Old.onTypedListener?.invoke()
                })
        )
        return bottomRow
    }
}
