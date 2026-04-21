package com.shoktuk.shoktukkeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SymbolKeyboardView(
    onKeyPress: (String) -> Unit,
    isShiftEnabled: MutableState<Boolean>
) {
    val ctx = LocalContext.current
    val row1 = remember { KeyboardSymbols.row1 }
    val row2 = remember { KeyboardSymbols.row2 }
    val row3 = remember { KeyboardSymbols.row3 }

    var freeButtonLeftString by remember { mutableStateOf("🇰🇬") }
    var freeButtonRightString by remember { mutableStateOf("☀️") }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val keyWidth = screenWidth / 11.25f

    LaunchedEffect(Unit) {
        val p = ctx.getSharedPreferences("keyboard_prefs", android.content.Context.MODE_PRIVATE)
        freeButtonLeftString = p.getString("freeButon_Click", "🇰🇬") ?: "🇰🇬"
        freeButtonRightString = p.getString("freeButon_Hold", "☀️") ?: "☀️"
    }

    Column(verticalArrangement = Arrangement.spacedBy(KeyboardStyle.rowSpacingDp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.width(0.dp))
            row1.forEach { key ->
                KeyButton(
                    key = key,
                    isSystem = true,
                    width = keyWidth,
                    backgroundColorIndex = (if (isShiftEnabled.value) key.backgroundColorIndexUppercase else key.backgroundColorIndexLowercase) ?: 1,
                    isShiftEnabled = isShiftEnabled,
                    onKeyPress = { onKeyPress(if (isShiftEnabled.value) key.uppercase else key.lowercase) },
                    onLongPress = {
                        val item = if (isShiftEnabled.value) key.upperCaseHold else key.lowerCaseHold
                        if (item != null) onKeyPress(item)
                    }
                )
            }
            Spacer(modifier = Modifier.width(0.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.width(0.dp))
            row2.forEach { key ->
                KeyButton(
                    key = key,
                    isSystem = false,
                    width = keyWidth,
                    backgroundColorIndex = (if (isShiftEnabled.value) key.backgroundColorIndexUppercase else key.backgroundColorIndexLowercase) ?: 1,
                    previewOnTap = true,
                    isShiftEnabled = isShiftEnabled,
                    onKeyPress = { onKeyPress(if (isShiftEnabled.value) key.uppercase else key.lowercase) },
                    onLongPress = {
                        val item = if (isShiftEnabled.value) key.upperCaseHold else key.lowerCaseHold
                        if (item != null) onKeyPress(item)
                    }
                )
            }
            Spacer(modifier = Modifier.width(0.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.width(0.dp))

            // Free/custom tamga button
            KeyButton(
                key = KeyboardKey(
                    name = "kg",
                    lowercase = freeButtonLeftString,
                    lowerCaseRomanization = freeButtonRightString,
                    lowerCaseHold = freeButtonRightString,
                    uppercase = freeButtonLeftString,
                    upperCaseRomanization = freeButtonRightString,
                    upperCaseHold = freeButtonRightString,
                ),
                isSystem = true,
                width = keyWidth * 1.5f,
                backgroundColorIndex = 1,
                previewOnTap = true,
                isShiftEnabled = isShiftEnabled,
                onKeyPress = { onKeyPress(freeButtonLeftString) },
                onLongPress = { onKeyPress(freeButtonRightString) }
            )

            Spacer(modifier = Modifier.width(0.dp))

            row3.forEach { key ->
                KeyButton(
                    key = key,
                    isSystem = false,
                    width = keyWidth,
                    backgroundColorIndex = (if (isShiftEnabled.value) key.backgroundColorIndexUppercase else key.backgroundColorIndexLowercase) ?: 1,
                    previewOnTap = true,
                    isShiftEnabled = isShiftEnabled,
                    onKeyPress = { onKeyPress(if (isShiftEnabled.value) key.uppercase else key.lowercase) },
                    onLongPress = {
                        val item = if (isShiftEnabled.value) key.upperCaseHold else key.lowerCaseHold
                        if (item != null) onKeyPress(item)
                    }
                )
            }

            Spacer(modifier = Modifier.width(0.dp))

            // Delete key
            KeyButton(
                title = "⌫",
                isSystem = true,
                width = keyWidth * 1.5f,
                backgroundColorIndex = 6,
                isShiftEnabled = isShiftEnabled,
                onKeyPress = { onKeyPress("delete") },
                whilePressing = { onKeyPress("delete") }
            )

            Spacer(modifier = Modifier.width(0.dp))
        }
    }
}
