package com.shoktuk.shoktukkeyboard.keyboard

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.view.View

@SuppressLint("StaticFieldLeak")
object LetterKeyBuilder {
    fun createLetterKey(
        service: InputMethodService, key: KeyEntry, isCaps: Boolean, buttonHeight: Int, buttonWidth: Int, onKeyClick: (String) -> Unit, onLongPress: (String?) -> Unit
    ): View {
        return KeyView(
            context = service, key = key, isCaps = isCaps, buttonHeight = buttonHeight, buttonWidth = buttonWidth, onKeyClick = onKeyClick, onLongPress = onLongPress
        )
    }
}