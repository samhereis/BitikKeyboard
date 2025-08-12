package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt

data class ButtonStyle(
    var fillColor: String, val borderColor: String, val borderWidthDp: Int, val cornerRadiusDp: Int, val textColor: String, val textSizeSp: TextUnit
)

private const val i = 343537

object KeyboardTheme {
    val colorIndexes: List<String> = listOf("#545658", "#283e3d", "#313643", "#3cd3fe")

    private const val BASE_SCREEN_WIDTH_DP = 350f

    private const val MAX_SCALE_FACTOR = 1.5f

    private const val BUTTON_HEIGHT_DP = 140
    const val KEY_MARGIN_DP = 4

    private val BASE_LETTER_TEXT_SIZE_SP = 20.sp
    private val BASE_LETTER_TEXT_SIZE_SP_NOHINT = 25.sp
    private val BASE_HINT_TEXT_SIZE_SP = 8.sp
    private val BASE_SYSTEM_TEXT_SIZE_SP = 17.sp

    const val CONTAINER_BACKGROUND_COLOR = "#1b1b17"

    const val SHIFT_ICON_FILE = "icons/shift_icon.png"
    const val DELETE_ICON_FILE = "icons/delete_icon.png"
    const val LANGUAGE_ICON_FILE = "icons/icon_language.png"
    const val ENTER_ICON_FILE = "icons/enter_icon.png"
    const val SPACE_ICON_FILE = "icons/space_icon.png"

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

    fun getButtonHeight(): Int {
        return BUTTON_HEIGHT_DP
    }

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

    fun dpToPx(context: Context, dp: Int): Int = (dp * context.resources.displayMetrics.density).toInt()

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
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getLetterButtonStyle_Normal(context: Context, showTranscription: Boolean = false): ButtonStyle {
        var textSizeSp = getLetterButtonTextSize(context)
        if (showTranscription == false || MyKeyboardService.currentAlphabet == "latin") {
            textSizeSp = getLetterButtonTextSize_NoTranscription(context)
        }

        return ButtonStyle(
            fillColor = "#57595b", borderColor = "#57595b", borderWidthDp = 0, cornerRadiusDp = 10, textColor = "#FFFFFF", textSizeSp = textSizeSp
        )
    }

    fun getLetterButtonStyle_UpperCase(context: Context, showTranscription: Boolean = false): ButtonStyle {
        var textSizeSp = getLetterButtonTextSize(context)
        if (showTranscription == false || MyKeyboardService.currentAlphabet == "latin") {
            textSizeSp = getLetterButtonTextSize_NoTranscription(context)
        }

        var color = "#3cd3fe"
        if (MyKeyboardService.currentAlphabet == "latin") {
            color = "#FFFFFF"
        }

        return ButtonStyle(
            fillColor = "#57595b", borderColor = "#57595b", borderWidthDp = 0, cornerRadiusDp = 10, textColor = color, textSizeSp = textSizeSp
        )
    }

    fun getSystemButtonStyle(context: Context): ButtonStyle {
        return ButtonStyle(
            fillColor = "#57595b", borderColor = "#57595b", borderWidthDp = 0, cornerRadiusDp = 10, textColor = "#FFFFFF", textSizeSp = getSystemButtonTextSize(context)
        )
    }
}
