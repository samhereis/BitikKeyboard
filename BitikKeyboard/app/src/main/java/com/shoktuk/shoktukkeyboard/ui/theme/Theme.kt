package com.shoktuk.shoktukkeyboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable

@Composable
fun ShoktukKeyboardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme, typography = Typography, content = content
    )
}