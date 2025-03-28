package com.shoktuk.shoktukkeyboard.keyboard

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.view.View

@SuppressLint("StaticFieldLeak")
object LetterKeyBuilder {
    fun createLetterKey(
        service: InputMethodService, key: KeyEntry, layout: KeyboardLayout, buttonHeight: Int, margin: Int, isCaps: Boolean, onKeyClick: (String) -> Unit, onLongPress: (String?) -> Unit
    ): View {
        return KeyView(
            context = service, key = key, isCaps = isCaps, buttonHeight = buttonHeight, margin = margin, onKeyClick = onKeyClick, onLongPress = onLongPress
        )
    }
}