package com.shoktuk.shoktukkeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun LetterKeyboardView(
    row1: List<KeyboardKey>,
    row2: List<KeyboardKey>,
    row3: List<KeyboardKey>,
    isShiftEnabled: MutableState<Boolean>,
    onKeyPress: (String) -> Unit,
    onShiftLongPress: () -> Unit = {}
) {
    val keyWidth = KeyboardStyle.keyWidth()

    FixedKeyboardRow(keys = row1, keyWidth = keyWidth, onKeyPress = onKeyPress, isShiftEnabled = isShiftEnabled)
    FixedKeyboardRow(keys = row2, keyWidth = keyWidth, onKeyPress = onKeyPress, isShiftEnabled = isShiftEnabled)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        // Shift — takes equal share of remaining space after letter keys
        KeyButton(
            modifier = Modifier.weight(1f),
            icon = {
                AssetIcon(
                    assetPath = if (isShiftEnabled.value) "icons/shiftfilled_icon.png" else "icons/shift_icon.png",
                    tint = KeyboardStyle.getColor(2)
                )
            },
            isSystem = true,
            backgroundColorIndex = 6,
            isShiftEnabled = isShiftEnabled,
            onKeyPress = { isShiftEnabled.value = !isShiftEnabled.value },
            onLongPress = onShiftLongPress
        )

        row3.forEach { key ->
            KeyButton(
                key = key,
                isSystem = false,
                width = keyWidth,
                backgroundColorIndex = if (isShiftEnabled.value) key.backgroundColorIndexUppercase ?: 1 else key.backgroundColorIndexLowercase ?: 1,
                previewOnTap = true,
                isShiftEnabled = isShiftEnabled,
                onKeyPress = {
                    onKeyPress(if (isShiftEnabled.value) key.uppercase else key.lowercase)
                },
                onLongPress = {
                    val hold = if (isShiftEnabled.value) key.upperCaseHold else key.lowerCaseHold
                    if (hold != null) onKeyPress(hold)
                }
            )
        }

        // Delete — takes equal share of remaining space after letter keys
        KeyButton(
            modifier = Modifier.weight(1f),
            icon = { AssetIcon(assetPath = "icons/delete_icon.png", tint = KeyboardStyle.getColor(2)) },
            isSystem = true,
            backgroundColorIndex = 6,
            isShiftEnabled = isShiftEnabled,
            onKeyPress = { onKeyPress("delete") },
            whilePressing = { onKeyPress("delete") }
        )
    }
}

@Composable
fun FixedKeyboardRow(
    keys: List<KeyboardKey>,
    keyWidth: Dp,
    onKeyPress: (String) -> Unit,
    isShiftEnabled: MutableState<Boolean>
) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f, fill = false))
        keys.forEach { key ->
            KeyButton(
                key = key,
                isSystem = false,
                width = keyWidth,
                backgroundColorIndex = if (isShiftEnabled.value) key.backgroundColorIndexUppercase ?: 1 else key.backgroundColorIndexLowercase ?: 1,
                previewOnTap = true,
                isShiftEnabled = isShiftEnabled,
                onKeyPress = {
                    onKeyPress(if (isShiftEnabled.value) key.uppercase else key.lowercase)
                },
                onLongPress = {
                    val hold = if (isShiftEnabled.value) key.upperCaseHold else key.lowerCaseHold
                    if (hold != null) onKeyPress(hold)
                }
            )
        }
        Spacer(Modifier.weight(1f, fill = false))
    }
}
