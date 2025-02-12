package com.shoktuk.bitikkeyboard

import KeyboardLayout
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
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
      val buttonHeight = (75 * resources.displayMetrics.density).toInt()
      val margin = (4 * resources.displayMetrics.density).toInt()

      val container = LinearLayout(this).apply {
         orientation = LinearLayout.VERTICAL
         layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
      }

      // Build each row of keys.
      for (row in layout.rows)
      {
         val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
         }
         for (key in row)
         {
            // If the key is a system key, create a simple Button.
            if (key.name == "Shift" || key.name == "Del")
            {
               val keyButton = Button(this).apply {
                  when (key.name)
                  {
                     "Shift" ->
                     {
                        text = ""
                        setBackgroundResource(android.R.drawable.arrow_up_float)
                        contentDescription = "Shift key"
                     }

                     "Del" ->
                     {
                        text = ""
                        setBackgroundResource(android.R.drawable.ic_input_delete)
                        contentDescription = "Delete key"
                     }
                  }
                  textSize = 20f
                  height = 150
                  layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                  setOnClickListener {
                     when (key.name)
                     {
                        "Shift" ->
                        {
                           isCaps = !isCaps
                           updateLetterKeys(container, layout)
                        }

                        "Del" ->
                        {
                           if (currentLayout!!.directionality == 1)
                           {
                              currentInputConnection?.deleteSurroundingText(1, 0)
                              currentInputConnection?.deleteSurroundingText(1, 0)
                           } else if (currentLayout!!.directionality == 0)
                           {
                              currentInputConnection?.deleteSurroundingText(1, 0)
                           }
                        }
                     }
                  }
               }
               rowLayout.addView(keyButton)
            } else
            {
// For non-system keys (using custom compound view)
               val keyView = LayoutInflater.from(this).inflate(R.layout.keyboard_key, rowLayout, false)
               keyView.background = GradientDrawable().apply {
                  shape = GradientDrawable.RECTANGLE
                  setColor(Color.parseColor("#282626"))
                  cornerRadius = 8 * resources.displayMetrics.density
               }

               val mainTextView = keyView.findViewById<TextView>(R.id.mainText)
               val subTextView = keyView.findViewById<TextView>(R.id.subText)
               mainTextView.text = if (isCaps) key.uppercase else key.lowercase

               if (isCaps)
               {
                  subTextView.text = key.upperCaseHint ?: ""
               } else
               {
                  subTextView.text = key.lowerCaseHint ?: ""
               }

               keyView.setOnClickListener {
                  val letter = if (isCaps) key.uppercase else key.lowercase
                  val textToCommit = letter
                  currentInputConnection?.commitText(textToCommit, 1)
               }
               keyView.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                  height = buttonHeight
               }
               rowLayout.addView(keyView)
            }
         }
         container.addView(rowLayout)
      }

      // Create the bottom row with language switching, space, and enter keys.
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
         setOnClickListener {
            currentInputConnection?.commitText(currentLayout!!.spaceKey, 1)
         }
      }
      bottomRow.addView(spaceButton)

      val enterButton = Button(this).apply {
         text = "Enter"
         height = 150
         layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
         setOnClickListener {
            currentInputConnection?.commitText("\n", 1)
         }
      }
      bottomRow.addView(enterButton)

      container.addView(bottomRow)
      return container
   }

   private fun updateLetterKeys(container: LinearLayout, layout: KeyboardLayout)
   {
      // Update only non-system keys (those that use the custom view).
      for (i in layout.rows.indices)
      {
         val rowLayout = container.getChildAt(i) as? LinearLayout ?: continue
         val rowKeys = layout.rows[i]
         for (j in rowKeys.indices)
         {
            val key = rowKeys[j]
            if (key.name != "Shift" && key.name != "Del")
            {
               // For custom key views, the first child (with id mainText) holds the letter.
               val keyView = rowLayout.getChildAt(j)
               val mainTextView = keyView.findViewById<TextView>(R.id.mainText)
               val subTextView = keyView.findViewById<TextView>(R.id.subText)
               mainTextView.text = if (isCaps) key.uppercase else key.lowercase

               if (isCaps)
               {
                  subTextView.text = key.upperCaseHint ?: ""
               } else
               {
                  subTextView.text = key.lowerCaseHint ?: ""
               }
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
