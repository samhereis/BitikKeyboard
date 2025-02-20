package com.shoktuk.bitikkeyboard

import android.content.Context
import com.google.gson.Gson
import java.io.InputStreamReader

object KeyboardLayoutLoader {
    /**
     * Loads a keyboard layout based on mode.
     * Mode should be either "letters" or "symbols".
     */
    fun loadKeyboardLayout(context: Context, mode: String): KeyboardLayout {
        val fileName = when (mode) {
            "letters" -> "langs/keyboard_kg_bitik.json"
            "symbols" -> "langs/keyboard_symbols.json"
            else -> "langs/keyboard_letters.json"
        }
        context.assets.open(fileName).use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                return Gson().fromJson(reader, KeyboardLayout::class.java)
            }
        }
    }
}
