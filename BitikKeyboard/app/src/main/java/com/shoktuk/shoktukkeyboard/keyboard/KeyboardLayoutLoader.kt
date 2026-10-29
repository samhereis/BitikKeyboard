package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardLetters
import com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardLetters_Arab
import com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardLetters_Kiril
import com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardLetters_Latin
import com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardLetters_Latin_Full
import com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardLetters_Old
import com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardSymbols

/** Hands out the layout classes (the Kotlin twins of the iOS `KeyboardLetters_*` structs) and applies the settings swaps. */
object KeyboardLayoutLoader {

    fun loadKeyboardLayout(context: Context, mode: KeyboardMode, language: String): KeyboardLayout {
        if (mode == KeyboardMode.Symbols) return LayoutSwaps.applySymbols(KeyboardSymbols.layout(), MyKeyboardService.current_writingSystem)

        val layout = when (language) {
            "latin" -> KeyboardLetters_Latin.layout()
            "latin_full" -> KeyboardLetters_Latin_Full.layout()
            "kiril" -> KeyboardLetters_Kiril.layout()
            "arabic" -> KeyboardLetters_Arab.layout()
            "enesay_old" -> KeyboardLetters_Old.layout()
            "enesay_modern", "enesay" -> KeyboardLetters.layout()
            else -> error("No layout class for \"$language\"")
        }

        // Like iOS: the data is used as is, then the settings' letter swaps are applied once, here.
        return if (mode == KeyboardMode.Main) LayoutSwaps.apply(layout, MyKeyboardService.current_writingSystem, context) else layout
    }
}
