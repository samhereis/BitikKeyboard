package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
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
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.R
import com.shoktuk.shoktukkeyboard.project.data.BitikFont
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikFont
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

object KeyboardStyle {
    val keySpacingDp: Dp = 0.dp
    val rowSpacingDp: Dp = 0.dp
    val keyTopPadding: Dp = 6.dp
    val keySidePadding: Dp = 2.dp
    val buttonCornerRadius: Dp = 9.dp
    val buttonFont: TextStyle = TextStyle(fontSize = 20.sp)

    @Composable
    fun nonScaledSp(dp: Float): TextUnit = with(LocalDensity.current) { dp.dp.toSp() }

    /// The user's chosen Bitik font (System/Zamanchak), or null (system default) — for any
    /// composable rendering bitik glyphs outside the main key grid (top-bar transcription,
    /// savable chips, etc). `alwaysBitik` is for content that is always bitik text even while
    /// a non-bitik script is the active keyboard (e.g. the "transcribe to bitik" pill).
    @Composable
    fun bitikFontFamily(alwaysBitik: Boolean = false): FontFamily? {
        val context = LocalContext.current
        if (context.bitikFont != BitikFont.Zamanchak) return null

        val isBitikContent = alwaysBitik || KeyboardViewControllerBase.writingSystemState.value == WritingSystem.Bitik
        return if (isBitikContent) FontFamily(Font(R.font.zamanchak)) else null
    }

    @Composable
    fun buttonFontStyle(): TextStyle = TextStyle(
        fontSize = nonScaledSp(20f), fontFamily = bitikFontFamily(), platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
    )

    @Composable
    fun hintFontSize(scale: Float = 1f): TextUnit = nonScaledSp(8.5f * scale)

    @Composable
    fun rowHeight(): Dp {
        val cfg = LocalConfiguration.current
        val h = cfg.screenHeightDp.dp
        return (h / 4f) / 4.5f
    }

    @Composable
    fun keyWidth(): Dp {
        val cfg = LocalConfiguration.current
        val w = cfg.screenWidthDp.dp
        return w / KeyboardViewControllerBase.maxRowElementsCount.toFloat()
    }

    // Sync with the old solution: resolve the exact same palette used by the
    // legacy View keyboard (KeyboardTheme's dynamic Material You colors).
    @Composable
    fun colors(): List<Color> {
        val context = LocalContext.current
        val dark = isDark()
        return remember(dark, context) {
            KeyboardTheme.getDynamicColorPalette(dark, context).map { Color(it.toColorInt()) }
        }
    }

    @Composable
    fun colors_universal(): List<Color> = listOf(
        MaterialTheme.colorScheme.surfaceContainer,
        MaterialTheme.colorScheme.onSecondary,
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondaryContainer
    )

    @Composable
    fun colors_light(): List<Color> {
        val context = LocalContext.current
        val newVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        var scheme = if (newVersion) dynamicDarkColorScheme(context) else MaterialTheme.colorScheme

        val vowel = scheme.surfaceTint
        val special = scheme.tertiary

        return listOf(
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.surfaceContainerLowest,
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.onSurface,
            vowel,
            special,
            MaterialTheme.colorScheme.secondaryContainer
        )
    }

    @Composable
    fun colors_dark(): List<Color> {
        val context = LocalContext.current
        val newVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        var scheme = if (newVersion) dynamicLightColorScheme(context) else MaterialTheme.colorScheme

        val vowel = scheme.surfaceTint
        val special = scheme.tertiary

        return listOf(
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.onSurface,
            vowel,
            special,
            MaterialTheme.colorScheme.secondaryContainer
        )
    }

    @Composable
    fun getColor(index: Int): Color {
        val list = colors()
        return list[index.coerceIn(0, list.lastIndex)]
    }

    @Composable
    fun getColor_ForKey(index: Int, coloringOn: Boolean): Color {
        return if (coloringOn) getColor(index) else getColor(1)
    }

    @Composable
    fun getButtonTextColor(isAlternate: Boolean, isSystem: Boolean): Color {
        return if (isAlternate && !isSystem) getColor(3) else getColor(2)
    }

    @Composable
    fun isDark(): Boolean {
        val cfg = LocalConfiguration.current
        val nightMode = cfg.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        if (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            return true
        } else {
            return false
        }
    }
}

private val colorLabels = listOf(
    "0 – keyboard bg", "1 – letter key bg", "2 – key text", "3 – key text caps", "4 – vowel key bg", "5 – special key bg", "6 – function keys"
)

@Composable
private fun KeyboardColorsPreview(colors: List<Color>) {
    val bg = colors[0]
    val textColor = colors[2]
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.forEachIndexed { index, color ->
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
                        text = "#%08X".format(color.value.toLong() shr 32), style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Preview(name = "Colors – Light", showBackground = true)
@Composable
fun KeyboardStyleColorsPreview_Light() {
    ShoktukKeyboardTheme(darkTheme = false) { KeyboardColorsPreview(KeyboardStyle.colors_light()) }
}

@Preview(name = "Colors – Light", showBackground = true)
@Composable
fun KeyboardStyleColorsPreview_Dark() {
    ShoktukKeyboardTheme(darkTheme = true) { KeyboardColorsPreview(KeyboardStyle.colors_dark()) }
}
