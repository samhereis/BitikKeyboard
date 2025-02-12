package com.shoktuk.bitikkeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import java.io.InputStreamReader

class MyKeyboardService : InputMethodService()
{
   private var isCaps: Boolean = false
   private var languageIndex: Int = 0
   private val languages = listOf("en", "kg_bitik", "ru")

   private var currentLayout: KeyboardLayout? = null

   override fun onCreateInputView(): View
   {
      currentLayout = loadKeyboardLayout(languages[languageIndex])
      return buildKeyboardView(currentLayout!!)
   }

   private fun buildKeyboardView(layout: KeyboardLayout): LinearLayout
   {
      val container = LinearLayout(this).apply {
         orientation = LinearLayout.VERTICAL
         layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
      }
      for (row in layout.rows)
      {
         val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
         }
         for (key in row)
         {
            val keyButton = Button(this).apply {
               if (key.name == "Shift")
               {
                  text = "" // Remove text to display only the icon
                  setBackgroundResource(android.R.drawable.arrow_up_float)
                  contentDescription = "Shift key"
               } else if (key.name == "Del")
               {
                  text = ""
                  setBackgroundResource(android.R.drawable.ic_input_delete)
                  contentDescription = "Delete key"
               } else
               {
                  text = if (isCaps) key.uppercase else key.lowercase
               }

               textSize = 20f
               height = 150

               layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
               setOnClickListener {
                  if (key.name == "Shift")
                  {
                     isCaps = !isCaps
                     updateLetterKeys(container, layout)
                  } else if (key.name == "Del")
                  {
                     if (layout.directionality == 1)
                     {
                        currentInputConnection?.deleteSurroundingText(1, 0)
                        currentInputConnection?.deleteSurroundingText(1, 0)
                        currentInputConnection?.deleteSurroundingText(1, 0)
                     } else
                     {
                        currentInputConnection?.deleteSurroundingText(1, 0)
                     }
                  } else
                  {
                     val letter = if (isCaps) key.uppercase else key.lowercase
                     currentInputConnection?.commitText("\u202B$letter\u202C", 1)
                  }
               }
            }
            rowLayout.addView(keyButton)
         }
         container.addView(rowLayout)
      }

      val bottomRow = LinearLayout(this).apply {
         orientation = LinearLayout.HORIZONTAL
         layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
      }

      val langButton = Button(this).apply {
         text = languages[languageIndex]
         height = 150
         layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
         setOnClickListener {
            languageIndex = (languageIndex + 1) % languages.size
            text = languages[languageIndex]
            currentLayout = loadKeyboardLayout(languages[languageIndex])
            val newKeyboard = buildKeyboardView(currentLayout!!)
            setInputView(newKeyboard)
         }
      }
      bottomRow.addView(langButton)

      val spaceButton = Button(this).apply {
         text = "Space"
         height = 150
         layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f)
         setOnClickListener { currentInputConnection?.commitText(currentLayout!!.spaceKey, 1) }
      }
      bottomRow.addView(spaceButton)

      val enterButton = Button(this).apply {
         text = "Enter"
         height = 150
         layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
         setOnClickListener { currentInputConnection?.commitText("\n", 1) }
      }
      bottomRow.addView(enterButton)

      container.addView(bottomRow)
      return container
   }

   private fun updateLetterKeys(container: LinearLayout, layout: KeyboardLayout)
   {
      for (i in layout.rows.indices)
      {
         val rowLayout = container.getChildAt(i) as? LinearLayout ?: continue
         val rowKeys = layout.rows[i]
         for (j in rowKeys.indices)
         {
            val btn = rowLayout.getChildAt(j) as? Button ?: continue
            val key = rowKeys[j]
            if (key.name != "Shift" && key.name != "Del")
            {
               btn.text = if (isCaps) key.uppercase else key.lowercase
            }
         }
      }
   }

   private fun loadKeyboardLayout(languageCode: String): KeyboardLayout
   {
      val fileName = when (languageCode)
      {
         "en" -> "langs/keyboard_en.json"
         "kg_bitik" -> "langs/keyboard_kg_bitik.json"
         "ru" -> "langs/keyboard_ru.json"
         else -> "langs/keyboard_en.json"
      }

      assets.open(fileName).use { inputStream ->
         InputStreamReader(inputStream).use { reader ->
            return Gson().fromJson(reader, KeyboardLayout::class.java)
         }
      }
   }
}
