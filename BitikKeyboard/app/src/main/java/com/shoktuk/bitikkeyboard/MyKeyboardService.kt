package com.shoktuk.bitikkeyboard

import android.inputmethodservice.InputMethodService
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast

class MyKeyboardService : InputMethodService()
{
   private var isCaps: Boolean = false
   private var isSymbols: Boolean = false
   private var languageIndex: Int = 0

   // Update your languages list to match your layouts.
   private val languages = listOf("EN", "KG", "RU")

   // Map language code to its corresponding layout resource.
   private val languageLayouts = mapOf("EN" to R.layout.keyboard_en, "KG" to R.layout.keyboard_bitik_kg, "RU" to R.layout.keyboard_ru)

   // Our container that holds the keyboard view.
   private lateinit var keyboardContainer: FrameLayout

   // The currently active keyboard view (child of the container)
   private lateinit var currentKeyboardView: View

   override fun onCreateInputView(): View
   {
      keyboardContainer = layoutInflater.inflate(R.layout.keyboard_container, null) as FrameLayout

      changeLanguage();
      return keyboardContainer
   }

   private fun changeLanguage()
   {
      currentKeyboardView = inflateKeyboardForLanguage(languages[languageIndex])
      keyboardContainer.addView(currentKeyboardView)
   }


   private fun inflateKeyboardForLanguage(lang: String): View
   {
      val layoutId = languageLayouts[lang] ?: R.layout.keyboard_en
      val view = layoutInflater.inflate(layoutId, keyboardContainer, false)
      setupKeyListeners(view)
      updateKeyboardKeys(view)
      return view
   }

   private fun setupKeyListeners(view: View)
   {
      // Setup letter key listeners.
      // Notice: iterate over rows (if letter keys are grouped in rows).
      val letterContainer = view.findViewById<ViewGroup>(R.id.letter_keys_container)
      if (letterContainer != null)
      {
         for (i in 0 until letterContainer.childCount)
         {
            val row = letterContainer.getChildAt(i)
            if (row is ViewGroup)
            {
               for (j in 0 until row.childCount)
               {
                  val keyView = row.getChildAt(j)
                  if (keyView is Button)
                  {
                     val baseLetter = keyView.tag as? String ?: continue
                     keyView.setOnClickListener {
                        val letter = if (isCaps) baseLetter.uppercase() else baseLetter.lowercase()
                        currentInputConnection?.commitText(letter, 1)
                        if (isCaps)
                        {
                           isCaps = false
                           updateKeyboardKeys(view)
                        }
                     }
                  }
               }
            }
         }
      }
      // Shift key toggles capitalization.
      view.findViewById<Button>(R.id.key_shift)?.setOnClickListener {
         isCaps = !isCaps
         updateKeyboardKeys(view)
      }
      // Symbols key toggles between letters and symbols.
      view.findViewById<Button>(R.id.key_symbols)?.setOnClickListener {
         isSymbols = !isSymbols
         updateKeyboardKeys(view)
      }
      // Language switch key cycles through available languages.
      view.findViewById<Button>(R.id.key_lang)?.setOnClickListener { btn ->
         languageIndex = (languageIndex + 1) % languages.size
         // Update the button text to reflect the current language.
         (btn as Button).text = languages[languageIndex]
         // Replace the current keyboard view with the new language layout.
         keyboardContainer.removeAllViews()

         changeLanguage();
      }

      view.findViewById<Button>(R.id.key_space)?.setOnClickListener {
         currentInputConnection?.commitText(" ", 1)
      }
      // Delete key.
      view.findViewById<Button>(R.id.key_delete)?.setOnClickListener {
         currentInputConnection?.deleteSurroundingText(1, 0)
      }
      // Enter key.
      view.findViewById<Button>(R.id.key_enter)?.setOnClickListener {
         currentInputConnection?.commitText("\n", 1)
      }
   }

   private fun updateKeyboardKeys(view: View)
   {
      // Update letter keys text based on the isCaps state.
      val letterContainer = view.findViewById<ViewGroup>(R.id.letter_keys_container)
      if (letterContainer != null)
      {
         for (i in 0 until letterContainer.childCount)
         {
            val row = letterContainer.getChildAt(i)
            if (row is ViewGroup)
            {
               for (j in 0 until row.childCount)
               {
                  val keyView = row.getChildAt(j)
                  if (keyView is Button)
                  {
                     val baseLetter = keyView.tag as? String ?: continue
                     keyView.text = if (isCaps) baseLetter.uppercase() else baseLetter.lowercase()
                  }
               }
            }
         }
      }
      // Toggle visibility of symbol keys if present.
      val symbolsContainer = view.findViewById<ViewGroup>(R.id.symbol_keys_container)
      if (symbolsContainer != null)
      {
         symbolsContainer.visibility = if (isSymbols) View.VISIBLE else View.GONE
         letterContainer.visibility = if (isSymbols) View.GONE else View.VISIBLE
      }
   }
}
