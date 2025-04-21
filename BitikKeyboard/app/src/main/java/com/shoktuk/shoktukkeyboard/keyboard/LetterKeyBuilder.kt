package com.shoktuk.shoktukkeyboard.keyboard

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.view.View
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager

@SuppressLint("StaticFieldLeak")
object LetterKeyBuilder {
    fun createLetterKey(
        service: InputMethodService,
        key: KeyEntry,
        layout: KeyboardLayout,
        buttonHeight: Int,
        margin: Int,
        isCaps: Boolean,
        symbol: Boolean,
        onKeyClick: (String) -> Unit,
        onLongPress: (String?) -> Unit
    ): View {
        var isCLassing = SettingsManager.getKeyboardVariant(service) == KeyboardVariant.CLASSIC

        return KeyView(
            context = service, key = key, isCaps = isCaps, buttonHeight = buttonHeight, margin = margin, isCLassic = isCLassing, symbol = symbol, onKeyClick = onKeyClick, onLongPress = onLongPress
        )
    }
}