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
        buttonHeight: Int,
        onKeyClick: (String) -> Unit,
        onLongPress: (String?) -> Unit
    ): View {
        return KeyView(
            context = service,
            key = key,
            buttonHeight = buttonHeight,
            onKeyClick = onKeyClick,
            onLongPress = onLongPress
        )
    }
}