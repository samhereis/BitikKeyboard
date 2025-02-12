package com.shoktuk.bitikkeyboard

import android.inputmethodservice.InputMethodService
import com.google.gson.Gson
import java.io.InputStreamReader

object KeyboardExtentions
{
   fun InputMethodService.loadKeyboardLayout(languageCode: String): KeyboardLayout
   {
      // Map language code to asset file name.
      val fileName = when (languageCode.uppercase())
      {
         "RU" -> "keyboard_ru.json"
         else -> "keyboard_en.json"
      }
      assets.open(fileName).use { inputStream ->
         InputStreamReader(inputStream).use { reader ->
            return Gson().fromJson(reader, KeyboardLayout::class.java)
         }
      }
   }
}