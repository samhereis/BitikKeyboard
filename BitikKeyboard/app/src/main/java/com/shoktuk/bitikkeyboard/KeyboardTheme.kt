package com.shoktuk.bitikkeyboard

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.Color

object KeyboardTheme {
   // Dimensions in dp
   const val BUTTON_HEIGHT_DP = 75
   const val KEY_MARGIN_DP = 4
   const val BUTTON_CORNER_RADIUS_DP = 8

   // Text sizes in sp
   const val MAIN_TEXT_SIZE_SP = 20f
   const val SUB_TEXT_SIZE_SP = 12f

   // Colors
   const val BUTTON_BACKGROUND_COLOR = "#21282F"  // dark blue/gray color
   const val MAIN_TEXT_COLOR = "#000000"
   const val SUB_TEXT_COLOR = "#000000"

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
