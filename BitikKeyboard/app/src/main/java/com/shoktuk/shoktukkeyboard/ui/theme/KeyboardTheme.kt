package com.shoktuk.shoktukkeyboard.ui.theme

import android.content.Context
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService

data class ButtonStyle(
    var fillColor: String,
    val borderColor: String,
    val borderWidthDp: Int,
    val cornerRadiusDp: Int,
    val textColor: String,
    val textSizeSp: TextUnit
)

object KeyboardTheme {
    // Palette indexes:
    // 0 = container background
    // 1 = key background
    // 2 = key text (normal)
    // 3 = key text (accent e.g., Shift active)
    // 4 = soft tamga key bg
    // 5 = special tamga key bg
    private val colorIndexes_Light = listOf(
        "#f1f0f7", // 0 container
        "#ffffff", // 1 key bg
        "#1B1B17", // 2 key text
        "#1d192b",  // 3 accent text
        "#e6e0e9",  // 4 soft tamga key bg
        "#dce2f9",  // 5 special tamga key bg
    )
    private val colorIndexes_Dark = listOf(
        "#1a1b20", // 0 container
        "#2f3036", // 1 key bg
        "#FFFFFF", // 2 key text
        "#d8e2ff", // 3 accent text
        "#2a3036",  // 4 soft tamga key bg
        "#323036",  // 5 special tamga key bg
    )

    private const val BASE_SCREEN_WIDTH_DP = 350f
    private const val MAX_SCALE_FACTOR = 1.5f
    private const val BUTTON_HEIGHT_DP = 175
    const val KEY_MARGIN_DP = 0

    private val BASE_LETTER_TEXT_SIZE_SP = 20.sp
    private val BASE_LETTER_TEXT_SIZE_SP_NOHINT = 25.sp
    private val BASE_HINT_TEXT_SIZE_SP = 8.sp
    private val BASE_SYSTEM_TEXT_SIZE_SP = 17.sp

    const val SHIFT_ICON_FILE = "icons/shift_icon.png"
    const val DELETE_ICON_FILE = "icons/delete_icon.png"
    const val LANGUAGE_ICON_FILE = "icons/icon_language.png"
    const val ENTER_ICON_FILE = "icons/enter_icon.png"
    const val SPACE_ICON_FILE = "icons/space_icon.png"

    private fun isNight(): Boolean =
        (MyKeyboardService.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    fun getColor(index: Int): String =
        (if (isNight()) colorIndexes_Dark else colorIndexes_Light)[index]

    fun getColorInt(index: Int): Int =
        Color.parseColor(getColor( index))

    fun containerBg(): Int = getColorInt(0)

    private fun getScaleFactor(context: Context): Float {
        val density = context.resources.displayMetrics.density
        val screenWidthDp = context.resources.displayMetrics.widthPixels / density
        val scaleFactor = screenWidthDp / BASE_SCREEN_WIDTH_DP
        return scaleFactor.coerceAtMost(MAX_SCALE_FACTOR)
    }

    fun getLetterButtonWidth(context: Context): Int {
        val density = context.resources.displayMetrics.density
        val screenWidthDp = context.resources.displayMetrics.widthPixels / density
        val totalMarginDp = (10 + KEY_MARGIN_DP) * KEY_MARGIN_DP
        val availableWidthDp = screenWidthDp - totalMarginDp
        val buttonWidthDp = availableWidthDp / 10
        return (buttonWidthDp * density).toInt() - KEY_MARGIN_DP
    }

    fun getSystemButtonWidth(context: Context): Int {
        return getLetterButtonWidth(context) + (getLetterButtonWidth(context) / 4)
    }

    fun getButtonHeight(): Int = BUTTON_HEIGHT_DP

    private fun getLetterButtonTextSize(context: Context): TextUnit {
        val scaleFactor = getScaleFactor(context)
        return BASE_LETTER_TEXT_SIZE_SP * scaleFactor
    }

    private fun getLetterButtonTextSize_NoTranscription(context: Context): TextUnit {
        val scaleFactor = getScaleFactor(context)
        return BASE_LETTER_TEXT_SIZE_SP_NOHINT * scaleFactor
    }

    fun getHintButtonTextSize(context: Context): TextUnit {
        val scaleFactor = getScaleFactor(context)
        return BASE_HINT_TEXT_SIZE_SP * scaleFactor
    }

    private fun getSystemButtonTextSize(context: Context): TextUnit {
        val scaleFactor = getScaleFactor(context)
        return BASE_SYSTEM_TEXT_SIZE_SP * scaleFactor
    }

    fun dpToPx(context: Context, dp: Int): Int =
        (dp * context.resources.displayMetrics.density).toInt()

    fun createDrawableFromStyle(context: Context, style: ButtonStyle): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(style.fillColor.toColorInt())
            cornerRadius = dpToPx(context, style.cornerRadiusDp).toFloat()
            setStroke(dpToPx(context, style.borderWidthDp), style.borderColor.toColorInt())
        }
    }

    fun loadAssetDrawable(context: Context, assetPath: String): Drawable? {
        return try {
            context.assets.open(assetPath).use { input ->
                val bitmap = BitmapFactory.decodeStream(input)
                bitmap.toDrawable(context.resources)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun getLetterButtonStyle_Normal(context: Context, showTranscription: Boolean = false): ButtonStyle {
        val textSizeSp = if (!showTranscription || MyKeyboardService.currentAlphabet == "latin")
            getLetterButtonTextSize_NoTranscription(context)
        else
            getLetterButtonTextSize(context)

        return ButtonStyle(
            fillColor = getColor(1),
            borderColor = getColor( 1),
            borderWidthDp = 0,
            cornerRadiusDp = 10,
            textColor = getColor( 2),
            textSizeSp = textSizeSp
        )
    }

    fun getLetterButtonStyle_UpperCase(context: Context, showTranscription: Boolean = false): ButtonStyle {
        val textSizeSp = if (!showTranscription || MyKeyboardService.currentAlphabet == "latin")
            getLetterButtonTextSize_NoTranscription(context)
        else
            getLetterButtonTextSize(context)

        val textColorHex =
            if (MyKeyboardService.currentAlphabet == "latin") getColor(2) // normal text color for Latin
            else getColor( 3) // accent when Bitik uppercase

        return ButtonStyle(
            fillColor = getColor(1),
            borderColor = getColor(1),
            borderWidthDp = 0,
            cornerRadiusDp = 10,
            textColor = textColorHex,
            textSizeSp = textSizeSp
        )
    }

    fun getSystemButtonStyle(context: Context): ButtonStyle {
        return ButtonStyle(
            fillColor = getColor( 1),
            borderColor = getColor( 1),
            borderWidthDp = 0,
            cornerRadiusDp = 10,
            textColor = getColor(2),
            textSizeSp = getSystemButtonTextSize(context)
        )
    }
}