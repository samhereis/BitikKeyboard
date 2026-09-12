package com.shoktuk.shoktukkeyboard.ui.theme

import android.content.Context
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color as ComposeColor
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikFont
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.R
import com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService
import com.shoktuk.shoktukkeyboard.project.data.BitikFont
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardHeight
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem

data class ButtonStyle(
    var fillColor: String, val borderColor: String, val borderWidthDp: Int, val cornerRadiusDp: Int, val textColor: String, val textSizeSp: Float
)

object KeyboardTheme {

    private const val BASE_SCREEN_WIDTH_DP = 350f
    private const val MAX_SCALE_FACTOR = 1.5f
    const val KEY_MARGIN_DP = 0
    const val KEY_MARGIN_DP_OnlyVisual_H = 2
    const val KEY_MARGIN_DP_OnlyVisual_V = 6

    private val BASE_LETTER_TEXT_SIZE_SP = 19
    private val BASE_LETTER_TEXT_SIZE_SP_NOHINT = 21
    private val BASE_HINT_TEXT_SIZE_SP = 8.5f
    private val BASE_SYSTEM_TEXT_SIZE_SP = 17

    const val SHIFT_ICON_FILE = "icons/shift_icon.png"
    const val SHIFT_ICON_FILE_Filled = "icons/shiftfilled_icon.png"
    const val BOOKMARK_ICON = "icons/bookmark_icon.png"
    const val DELETE_ICON_FILE = "icons/delete_icon.png"
    const val LANGUAGE_ICON_FILE = "icons/icon_language.png"
    const val SPACE_ICON_FILE = "icons/space_icon.png"

    const val ENTER_ICON_FILE = "icons/enter_icon.png"
    const val ENTER_SEARCH_ICON_FILE = "icons/enter_search_icon.png"
    const val ENTER_GO_ICON_FILE = "icons/enter_enter_go_icon.png"
    const val ENTER_SEND_ICON_FILE = "icons/enter_send_icon.png"
    const val ENTER_DONE_ICON_FILE = "icons/enter_done_icon.png"
    const val ENTER_NEXT_ICON_FILE = "icons/enter_next_icon.png"

    private var cachedPalette: List<String>? = null
    private var cachedZamanchakTypeface: android.graphics.Typeface? = null

    fun invalidateColorCache() {
        cachedPalette = null
    }

    /**
     * The bundled Zamanchak typeface if the user picked it in Appearance settings, loaded once
     * and cached; null (system default) otherwise. Callers still need to check whether the
     * currently active script is actually Bitik before applying this to a TextView.
     */
    fun bitikTypeface(context: Context): android.graphics.Typeface? {
        if (context.bitikFont != BitikFont.Zamanchak) return null
        return cachedZamanchakTypeface ?: androidx.core.content.res.ResourcesCompat.getFont(context, R.font.zamanchak)
            .also { cachedZamanchakTypeface = it }
    }

    fun getCachedPalette(): List<String> {
        if (cachedPalette == null) {
            cachedPalette = getDynamicColorPalette(isNight())
        }

        return cachedPalette!!
    }

    fun getDynamicColorPalette(isDark: Boolean, context: Context = MyKeyboardService.context): List<String> {
        val isS = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S

        val scheme = when {
            isS && isDark -> dynamicDarkColorScheme(context)
            isS && !isDark -> dynamicLightColorScheme(context)
            isDark -> darkColorScheme()
            else -> lightColorScheme()
        }

        val scheme_alt = when {
            isS && isDark -> dynamicLightColorScheme(context)
            isS && !isDark -> dynamicDarkColorScheme(context)
            isDark -> lightColorScheme()
            else -> darkColorScheme()
        }

        return if (!isDark) {
            listOf(
                scheme.surfaceContainer.toArgb().toHexColorString(),
                scheme.surfaceContainerLowest.toArgb().toHexColorString(),
                scheme.onSurface.toArgb().toHexColorString(),
                scheme.onSurface.toArgb().toHexColorString(),
                scheme_alt.surfaceTint.toArgb().toHexColorString(),
                scheme_alt.tertiary.toArgb().toHexColorString(),
                scheme.secondaryContainer.toArgb().toHexColorString()
            )
        } else {
            listOf(
                scheme.surfaceContainer.toArgb().toHexColorString(),
                scheme.surfaceContainerHighest.toArgb().toHexColorString(),
                scheme.onSurface.toArgb().toHexColorString(),
                scheme.onSurface.toArgb().toHexColorString(),
                scheme_alt.surfaceTint.toArgb().toHexColorString(),
                scheme_alt.tertiary.toArgb().toHexColorString(),
                scheme.secondaryContainer.toArgb().toHexColorString()
            )
        }
    }

    fun getColor(index: Int): String {
        val palette = getCachedPalette()
        return palette.getOrElse(index) { "#FF00FF" }
    }

    fun Int.toHexColorString(): String = String.format("#%08X", this or 0xFF000000.toInt())

    private fun isNight(): Boolean = (MyKeyboardService.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    fun getColorInt(index: Int): Int = getColor(index).toColorInt()

    fun containerBg(): Int = getColorInt(0)

    private fun getScaleFactor(context: Context): Float {
        val density = context.resources.displayMetrics.density
        val screenWidthDp = context.resources.displayMetrics.widthPixels / density
        val scaleFactor = screenWidthDp / BASE_SCREEN_WIDTH_DP
        return scaleFactor.coerceAtMost(MAX_SCALE_FACTOR)
    }

    fun getLetterButtonWidth(context: Context, maxKeyCount: Int = 10): Int {
        val density = context.resources.displayMetrics.density
        val screenWidthDp = context.resources.displayMetrics.widthPixels / density
        val totalMarginDp = (maxKeyCount + KEY_MARGIN_DP) * KEY_MARGIN_DP
        val availableWidthDp = screenWidthDp - totalMarginDp
        val buttonWidthDp = availableWidthDp / maxKeyCount
        return (buttonWidthDp * density).toInt() - KEY_MARGIN_DP
    }

    fun getSystemButtonWidth(context: Context, maxKeyCount: Int = 10): Int {
        return getLetterButtonWidth(context, maxKeyCount) + (getLetterButtonWidth(context) / 4)
    }

    fun getButtonHeight(): Int {
        var setting = MyKeyboardService.context.keyboardHeight
        var settingDP = dpToPx(MyKeyboardService.context, setting)
        var screenHeight = MyKeyboardService.context.resources.displayMetrics.heightPixels

        if (settingDP > screenHeight / 2) {
            var neededHeight = pxToDp(MyKeyboardService.context, screenHeight / 3)
            return neededHeight
        } else {
            return setting
        }
    }

    private fun getLetterButtonTextSize(context: Context): Float {
        val scaleFactor = getScaleFactor(context)
        return BASE_LETTER_TEXT_SIZE_SP * scaleFactor
    }

    private fun getLetterButtonTextSize_NoTranscription(context: Context): Float {
        val scaleFactor = getScaleFactor(context)
        return BASE_LETTER_TEXT_SIZE_SP_NOHINT * scaleFactor
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
    fun pxToDp(context: Context, px: Int): Int {
        var density = context.resources.displayMetrics.density
        return (px / context.resources.displayMetrics.density).toInt()
    }

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
        val textSizeSp = if (!showTranscription || MyKeyboardService.current_writingSystem == WritingSystem.Latin) getLetterButtonTextSize_NoTranscription(context)
        else getLetterButtonTextSize(context)

        return ButtonStyle(
            fillColor = getColor(1), borderColor = getColor(1), borderWidthDp = 0, cornerRadiusDp = 10, textColor = getColor(2), textSizeSp = textSizeSp
        )
    }

    fun getLetterButtonStyle_UpperCase(context: Context, showTranscription: Boolean = false): ButtonStyle {
        val textSizeSp = if (!showTranscription || MyKeyboardService.current_writingSystem == WritingSystem.Latin) getLetterButtonTextSize_NoTranscription(context)
        else getLetterButtonTextSize(context)

        val textColorHex = if (MyKeyboardService.current_writingSystem == WritingSystem.Latin) getColor(2)
        else getColor(3)

        return ButtonStyle(
            fillColor = getColor(1), borderColor = getColor(1), borderWidthDp = 0, cornerRadiusDp = 10, textColor = textColorHex, textSizeSp = textSizeSp
        )
    }

    fun getSystemButtonStyle(context: Context, backgroundColor: Int = 6, textColor: Int = 1): ButtonStyle {
        return ButtonStyle(
            fillColor = getColor(6), borderColor = getColor(1), borderWidthDp = 0, cornerRadiusDp = 10, textColor = getColor(2), textSizeSp = getSystemButtonTextSize(context)
        )
    }
}

private val colorLabels = listOf(
    "0 – keyboard bg", "1 – letter key bg", "2 – key text", "3 – key text caps", "4 – vowel key bg", "5 – special key bg", "6 – function keys"
)

@Composable
private fun KeyboardThemeColorsPreview(colors: List<String>) {
    val composeColors = colors.map { hex ->
        runCatching { ComposeColor(hex.toColorInt()) }.getOrElse { ComposeColor.Gray }
    }
    val bg = composeColors.getOrElse(0) { ComposeColor.White }
    val textColor = composeColors.getOrElse(2) { ComposeColor.Black }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        composeColors.forEachIndexed { index, color ->
            Row(
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                ) {
                    Text(
                        text = "a", color = textColor, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Column {
                    Text(
                        text = colorLabels.getOrElse(index) { "[$index]" }, style = MaterialTheme.typography.bodySmall, color = textColor
                    )
                    Text(
                        text = colors.getOrElse(index) { "" }, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Preview(name = "KeyboardTheme – Light", showBackground = true)
@Composable
private fun KeyboardThemePreview_Light() {
    val context = LocalContext.current
    KeyboardThemeColorsPreview(KeyboardTheme.getDynamicColorPalette(false))
}

@Preview(name = "KeyboardTheme – Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun KeyboardThemePreview_Dark() {
    val context = LocalContext.current
    KeyboardThemeColorsPreview(KeyboardTheme.getDynamicColorPalette(true))
}