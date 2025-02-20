package com.shoktuk.bitikkeyboard

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable

/**
 * A data class holding all style properties for a button.
 */
data class ButtonStyle(
   val fillColor: String,       // e.g. "#3A3A3C"
   val borderColor: String,     // e.g. "#888888"
   val borderWidthDp: Int,      // e.g. 2
   val cornerRadiusDp: Int,     // e.g. 4
   val textColor: String,       // e.g. "#FFFFFF"
   val textSizeSp: Float        // e.g. 25f for letters, 18f for system keys
)

object KeyboardTheme {
   // Basic dimensions
   const val BUTTON_HEIGHT_DP = 70
   const val KEY_MARGIN_DP = 3

   // Widths for keys
   const val LETTER_BUTTON_WIDTH_DP = 50
   const val SYSTEM_BUTTON_WIDTH_DP = 70

   // Container background
   const val CONTAINER_BACKGROUND_COLOR = "#000000" // black

   // Asset file paths for PNG icons (placed in assets/icons/)
   const val SHIFT_ICON_FILE = "icons/shift_icon.png"
   const val DELETE_ICON_FILE = "icons/delete_icon.png"
   const val ENTER_ICON_FILE = "icons/enter_icon.png"
   const val SPACE_ICON_FILE = "icons/space_icon.png"

   // Two styles: one for letter keys, one for system keys.
   val letterButtonStyle = ButtonStyle(
      fillColor = "#3A3A3C",     // dark gray
      borderColor = "#888888",   // letter border color
      borderWidthDp = 2,
      cornerRadiusDp = 4,
      textColor = "#FFFFFF",
      textSizeSp = 25f
   )

   val systemButtonStyle = ButtonStyle(
      fillColor = "#3A3A3C",     // dark gray
      borderColor = "#888888",   // system key border color
      borderWidthDp = 2,
      cornerRadiusDp = 4,
      textColor = "#FFFFFF",
      textSizeSp = 18f
   )

   /**
    * Converts dp to px.
    */
   fun dpToPx(context: Context, dp: Int): Int =
      (dp * context.resources.displayMetrics.density).toInt()

   /**
    * Creates a GradientDrawable from the given ButtonStyle.
    */
   fun createDrawableFromStyle(context: Context, style: ButtonStyle): GradientDrawable {
      return GradientDrawable().apply {
         shape = GradientDrawable.RECTANGLE
         setColor(Color.parseColor(style.fillColor))
         cornerRadius = dpToPx(context, style.cornerRadiusDp).toFloat()
         setStroke(dpToPx(context, style.borderWidthDp), Color.parseColor(style.borderColor))
      }
   }

   /**
    * Loads a PNG from assets/icons and returns it as a Drawable.
    * Returns null if loading fails.
    */
   fun loadAssetDrawable(context: Context, assetPath: String): Drawable? {
      return try {
         context.assets.open(assetPath).use { input ->
            val bitmap = BitmapFactory.decodeStream(input)
            BitmapDrawable(context.resources, bitmap)
         }
      } catch (e: Exception) {
         e.printStackTrace()
         null
      }
   }
}
