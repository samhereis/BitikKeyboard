package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import com.google.gson.Gson
import java.io.InputStreamReader

object KeyboardLayoutLoader {

    fun loadKeyboardLayout(context: Context, mode: KeyboardMode, language: String): KeyboardLayout {
        var fileName = ""

        if (mode == KeyboardMode.Symbols) {
            fileName = "langs/keyboard_symbols.json"
        } else {
            fileName = "langs/${language}.json"
        }

        context.assets.open(fileName).use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                return Gson().fromJson(reader, KeyboardLayout::class.java)
            }
        }
    }
}
