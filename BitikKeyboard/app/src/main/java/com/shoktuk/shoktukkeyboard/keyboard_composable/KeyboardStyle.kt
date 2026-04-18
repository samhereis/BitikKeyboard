package com.shoktuk.shoktukkeyboard.keyboard

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object KeyboardStyle {
    val keySpacingDp: Dp = 0.dp
    val rowSpacingDp: Dp = 0.dp
    val keyTopPadding: Dp = 4.dp
    val keySidePadding: Dp = 2.dp
    val buttonCornerRadius: Dp = 9.dp
    val buttonFont: TextStyle = TextStyle(fontSize = 20.sp)

    @Composable
    fun nonScaledSp(dp: Float): TextUnit = with(LocalDensity.current) { dp.dp.toSp() }

    @Composable
    fun buttonFontStyle(): TextStyle = TextStyle(
        fontSize = nonScaledSp(20f),
        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
    )

    @Composable
    fun hintFontSize(scale: Float = 1f): TextUnit = nonScaledSp(8.5f * scale)

    @Composable
    fun rowHeight(): Dp {
        val cfg = LocalConfiguration.current
        val h = cfg.screenHeightDp.dp
        return (h / 4f) / 4f
    }

    @Composable
    fun keyWidth(): Dp {
        val cfg = LocalConfiguration.current
        val w = cfg.screenWidthDp.dp
        return w / KeyboardViewControllerBase.maxRowElementsCount.toFloat()
    }

    @Composable
    fun colors(): List<Color> = listOf(
        MaterialTheme.colorScheme.surfaceContainer,             // 0 keyboard background (like Gboard tinted tray)
        MaterialTheme.colorScheme.surfaceContainerHighest,      // 1 letter key face
        MaterialTheme.colorScheme.onSurface,                    // 2 key text
        MaterialTheme.colorScheme.tertiary,                     // 3 accent text (caps / vowels)
        MaterialTheme.colorScheme.tertiary,                     // 4 vowel / soft key highlight bg
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),   // 5 subtle divider / hold indicator
        MaterialTheme.colorScheme.secondaryContainer            // 6 function keys (shift, delete, space, switcher)
    )

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
        return (cfg.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}
