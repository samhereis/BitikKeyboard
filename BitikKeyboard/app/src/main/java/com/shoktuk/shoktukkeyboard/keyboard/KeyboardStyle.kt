package com.shoktuk.shoktukkeyboard.keyboard

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object KeyboardStyle {
    val keySpacingDp: Dp = 0.dp
    val rowSpacingDp: Dp = 0.dp
    val keyTopPadding: Dp = 4.dp
    val keySidePadding: Dp = 3.dp
    val buttonCornerRadius: Dp = 9.dp
    val buttonFont: TextStyle = TextStyle(fontSize = 20.sp)
    val buttonFont_NoHint: TextStyle = TextStyle(fontSize = 24.sp)
    val buttonFont_NonBitik_Shift: TextStyle = TextStyle(fontSize = 20.sp)
    var maxRowElementsCount: Double = 10.25

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
        return w / maxRowElementsCount.toFloat()
    }

    @Composable
    fun colors(): List<Color> = listOf(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.outlineVariant
    )

    @Composable
    fun getColor(index: Int): Color {
        val list = colors()
        return list[index.coerceIn(0, list.lastIndex)]
    }

    @Composable
    fun getColor_ForKey(index: Int, coloringOn: Boolean): Color {
        return getColor(index)
    }

    @Composable
    fun getButtonTextColor(
        isAlternate: Boolean, isSystem: Boolean
    ): Color {
        return if (isAlternate && !isSystem) getColor(3) else getColor(2)
    }

    @Composable
    fun isDark(): Boolean {
        val cfg = LocalConfiguration.current
        return (cfg.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}