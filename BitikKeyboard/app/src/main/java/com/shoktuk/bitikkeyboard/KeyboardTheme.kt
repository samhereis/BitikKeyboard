package com.shoktuk.bitikkeyboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

object KeyboardTheme {
   // Dimensions in dp
   const val BUTTON_HEIGHT_DP = 75
   const val KEY_MARGIN_DP = 4
   const val BUTTON_CORNER_RADIUS_DP = 8

   // Text sizes in sp
   const val MAIN_TEXT_SIZE_SP = 20f
   const val SUB_TEXT_SIZE_SP = 12f

   // Colors
   const val BUTTON_BACKGROUND_COLOR = "#282626"       // background for letter keys
   const val LETTER_TEXT_COLOR = "#FFFFFF"              // white text for letter keys
   const val SPECIAL_KEY_BACKGROUND_COLOR = "#444444"   // background for system keys
   const val SPECIAL_KEY_TEXT_COLOR = "#FFFFFF"         // text color for system keys

   // System key icons (using default Android resources)
   val ICON_SHIFT = android.R.drawable.arrow_up_float
   val ICON_DEL = android.R.drawable.ic_input_delete

   fun dpToPx(context: Context, dp: Int): Int =
      (dp * context.resources.displayMetrics.density).toInt()

   fun createButtonBackground(context: Context): GradientDrawable {
      return GradientDrawable().apply {
         shape = GradientDrawable.RECTANGLE
         setColor(Color.parseColor(BUTTON_BACKGROUND_COLOR))
         cornerRadius = BUTTON_CORNER_RADIUS_DP * context.resources.displayMetrics.density
      }
   }
}
