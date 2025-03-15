package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.core.graphics.toColorInt
import androidx.core.graphics.drawable.toDrawable

/**
 * A data class holding all style properties for a button.
 */
data class ButtonStyle(
    val fillColor: String,       // e.g. "#3A3A3C"
    val borderColor: String,     // e.g. "#888888"
    val borderWidthDp: Int,      // e.g. 2
    val cornerRadiusDp: Int,     // e.g. 4
    val textColor: String,       // e.g. "#FFFFFF"
    val textSizeSp: Float        // Base text size; will be scaled.
)

private const val i = 343537

object KeyboardTheme {
    val colorIndexes: List<String> = listOf("#0b84fe", "#806642", "#31d159")

    private const val BASE_SCREEN_WIDTH_DP = 350f

    // Maximum scale factor to avoid oversized keys on very wide screens.
    private const val MAX_SCALE_FACTOR = 1.5f

    // Basic dimensions (base design values)
    private const val BUTTON_HEIGHT_DP = 140
    const val KEY_MARGIN_DP = 4

    private const val BASE_LETTER_BUTTON_WIDTH_DP = 85
    private const val BASE_SYSTEM_BUTTON_WIDTH_DP = 125

    // Base text sizes for buttons
    private const val BASE_LETTER_TEXT_SIZE_SP = 22f
    private const val BASE_HINT_TEXT_SIZE_SP = 10f
    private const val BASE_SYSTEM_TEXT_SIZE_SP = 17f

    // Container background color
    const val CONTAINER_BACKGROUND_COLOR = "#000000" // black

    // Asset file paths for PNG icons (placed in assets/icons/)
    const val SHIFT_ICON_FILE = "icons/shift_icon.png"
    const val DELETE_ICON_FILE = "icons/delete_icon.png"
    const val ENTER_ICON_FILE = "icons/enter_icon.png"
    const val SPACE_ICON_FILE = "icons/space_icon.png"

    // Calculate scale factor based on the device's screen width in dp.
    private fun getScaleFactor(context: Context): Float {
        val density = context.resources.displayMetrics.density
        val screenWidthDp = context.resources.displayMetrics.widthPixels / density
        val scaleFactor = screenWidthDp / BASE_SCREEN_WIDTH_DP
        return scaleFactor.coerceAtMost(MAX_SCALE_FACTOR)
    }

    fun getLetterButtonWidth(context: Context): Int {
        val density = context.resources.displayMetrics.density
        // Convert screen width from px to dp.
        val screenWidthDp = context.resources.displayMetrics.widthPixels / density
        // Total margin: one margin at each end plus one margin between each button.
        val totalMarginDp = (10 + KEY_MARGIN_DP) * KEY_MARGIN_DP
        // Available width in dp after subtracting margins.
        val availableWidthDp = screenWidthDp - totalMarginDp
        // Width per button in dp.
        val buttonWidthDp = availableWidthDp / 10
        // Convert to px.
        return (buttonWidthDp * density).toInt() - KEY_MARGIN_DP
    }

    fun getSystemButtonWidth(context: Context): Int {
        return getLetterButtonWidth(context) + (getLetterButtonWidth(context) / 2)
    }

    fun getButtonHeight(): Int {
        return BUTTON_HEIGHT_DP
    }

    fun getKeyMargin(context: Context): Int = dpToPx(context, KEY_MARGIN_DP)

    private fun getLetterButtonTextSize(context: Context): Float {
        val scaleFactor = getScaleFactor(context)
        return BASE_LETTER_TEXT_SIZE_SP * scaleFactor
    }

    fun getHintButtonTextSize(context: Context): Float {
        val scaleFactor = getScaleFactor(context)
        return BASE_HINT_TEXT_SIZE_SP * scaleFactor
    }

    private fun getSystemButtonTextSize(context: Context): Float {
        val scaleFactor = getScaleFactor(context)
        return BASE_SYSTEM_TEXT_SIZE_SP * scaleFactor
    }

    fun dpToPx(context: Context, dp: Int): Int = (dp * context.resources.displayMetrics.density).toInt()

    fun createDrawableFromStyle(context: Context, style: ButtonStyle): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(style.fillColor.toColorInt())
            cornerRadius = dpToPx(context, style.cornerRadiusDp).toFloat()
            setStroke(dpToPx(context, style.borderWidthDp), style.borderColor.toColorInt())
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
                bitmap.toDrawable(context.resources)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Responsive styles for letter and system keys.
    fun getLetterButtonStyle(context: Context): ButtonStyle {
        return ButtonStyle(
            fillColor = "#535353",     // dark gray
            borderColor = "#535353", borderWidthDp = 0, cornerRadiusDp = 10, textColor = "#FFFFFF", textSizeSp = getLetterButtonTextSize(context)
        )
    }

    fun getSystemButtonStyle(context: Context): ButtonStyle {
        return ButtonStyle(
            fillColor = "#2d2d2d", borderColor = "#2d2d2d", borderWidthDp = 0, cornerRadiusDp = 10, textColor = "#FFFFFF", textSizeSp = getSystemButtonTextSize(context)
        )
    }
}
