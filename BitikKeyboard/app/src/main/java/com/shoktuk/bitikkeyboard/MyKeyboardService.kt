package com.shoktuk.bitikkeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout

class MyKeyboardService : InputMethodService()
{

   // State for shift and language selection
   private var isCaps: Boolean = false
   private var languageIndex: Int = 0
   private val languages = listOf("EN", "KG", "RU")

   // Define the key rows for a basic QWERTY layout for English.
   // For simplicity, we only define one layout here.
   private val qwertyRows =
      listOf(listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"), listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"), listOf("Shift", "z", "x", "c", "v", "b", "n", "m", "Del"))

   override fun onCreateInputView(): View
   {
      // Create a vertical container for the keyboard
      val container = LinearLayout(this).apply {
         orientation = LinearLayout.VERTICAL
         layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
      }

      // Create rows for letter keys
      for (row in qwertyRows)
      {
         val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
         }
         // For each key in the row, create a button
         for (key in row)
         {
            val keyButton = Button(this).apply {
               // For regular letter keys, apply shift logic.
               text = when (key)
               {
                  "Shift", "Del" -> key
                  else -> if (isCaps) key.uppercase() else key.lowercase()
               }
               // Use weight so that keys are distributed evenly.
               layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
               setOnClickListener {
                  when (key)
                  {
                     "Shift" ->
                     {
                        isCaps = !isCaps
                        updateLetterKeys(container)
                     }

                     "Del" ->
                     {
                        currentInputConnection?.deleteSurroundingText(1, 0)
                     }

                     else ->
                     {
                        // Commit the letter (apply shift if needed)
                        val letter = if (isCaps) key.uppercase() else key.lowercase()
                        currentInputConnection?.commitText(letter, 1)
                        // Optionally, turn off shift after one key press.
                        if (isCaps)
                        {
                           isCaps = false
                           updateLetterKeys(container)
                        }
                     }
                  }
               }
            }
            rowLayout.addView(keyButton)
         }
         container.addView(rowLayout)
      }

      // Bottom row for function keys (language, space, enter)
      val bottomRow = LinearLayout(this).apply {
         orientation = LinearLayout.HORIZONTAL
         layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
      }

      // Language switch button
      val langButton = Button(this).apply {
         text = languages[languageIndex]
         layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
         setOnClickListener {
            // Cycle through languages.
            languageIndex = (languageIndex + 1) % languages.size
            text = languages[languageIndex]
            // For this simple demo, the keyboard layout remains the same.
            // In a complete implementation, you might update the keys.
         }
      }
      bottomRow.addView(langButton)

      // Space button
      val spaceButton = Button(this).apply {
         text = "Space"
         layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f)
         setOnClickListener { currentInputConnection?.commitText(" ", 1) }
      }
      bottomRow.addView(spaceButton)

      // Enter button
      val enterButton = Button(this).apply {
         text = "Enter"
         layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
         setOnClickListener { currentInputConnection?.commitText("\n", 1) }
      }
      bottomRow.addView(enterButton)

      container.addView(bottomRow)
      return container
   }

   /**
    * Update the text of the letter keys based on the current shift state.
    * It iterates over the rows that contain letter keys (the first three rows).
    */
   private fun updateLetterKeys(container: LinearLayout)
   {
      // Assume the first three children of the container are letter rows.
      for (i in 0 until container.childCount)
      {
         val child = container.getChildAt(i)
         if (child is LinearLayout && i < qwertyRows.size)
         {
            val rowKeys = qwertyRows[i]
            for (j in 0 until child.childCount)
            {
               val btn = child.getChildAt(j) as? Button ?: continue
               val key = rowKeys[j]
               // Only update letter keys, not the "Shift" or "Del" keys.
               if (key != "Shift" && key != "Del")
               {
                  btn.text = if (isCaps) key.uppercase() else key.lowercase()
               }
            }
         }
      }
   }
}
