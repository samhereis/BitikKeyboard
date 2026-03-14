package com.shoktuk.shoktukkeyboard.keyboard

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator

@Composable
fun BottomRowView(
    isSymbolsEnabled: MutableState<Boolean>,
    isShiftEnabled: MutableState<Boolean>,
    onKeyPress: (String) -> Unit = {},
    lastButton_WhileHolding: (String) -> Unit = {},
    onSwitcherClick: () -> Unit = {},
    onSwitcherHold: () -> Unit = {},
    switcherTitle: String = "🅰😀",
    switcherTitle_Alt: String = "⓬😀",
    lastButton: String = "arrow.turn.down.left",
    onAdjustCursor: (Int) -> Unit = {}
) {
    val keyWidth = KeyboardStyle.keyWidth()
    val isBitikMain = KeyboardViewControllerBase.keyboardMode == KeyboardState.Main

    Row {
        KeyButton(
            title = if (isSymbolsEnabled.value) switcherTitle else switcherTitle_Alt,
            isSystem = true,
            width = keyWidth * 1.5f,
            backgroundColorIndex = 6,
            isShiftEnabled = isShiftEnabled,
            onKeyPress = {
                onSwitcherClick()
            },
            onLongPress = {
                onSwitcherHold()
            }
        )

        KeyButton(
            title = if (isBitikMain) "·" else ".",
            isSystem = true,
            width = keyWidth,
            backgroundColorIndex = 1,
            isShiftEnabled = isShiftEnabled,
            onKeyPress = {
                onKeyPress(if (isBitikMain) "sys·" else "sys.")
            },
            onLongPress = {
                onKeyPress(if (isBitikMain) "sys." else "sys·")
            }
        )

        KeyButton(
            modifier = Modifier.weight(1f),
            isSystem = true,
            backgroundColorIndex = 1,
            isShiftEnabled = isShiftEnabled,
            onKeyPress = { onKeyPress("sys ") }
        )

        if (isBitikMain && KeyboardViewControllerBase.context.wordSeparator != WordSeparator.Off) {
            val ws = when (KeyboardViewControllerBase.context.wordSeparator) {
                WordSeparator.NoSpace -> "⁚"
                WordSeparator.SpaceBefore -> "⁚ "
                WordSeparator.ArroundSpace -> " ⁚ "
                WordSeparator.Off -> "⁚"
            }
            KeyButton(
                title = "⁚",
                isSystem = true,
                backgroundColorIndex = 1,
                isShiftEnabled = isShiftEnabled,
                onKeyPress = { onKeyPress("sys$ws") }
            )
        }

        KeyButton(
            title = if (isBitikMain) "⹁" else ",",
            isSystem = true,
            width = keyWidth,
            backgroundColorIndex = 1,
            isShiftEnabled = isShiftEnabled,
            onKeyPress = {
                onKeyPress("sys" + if (isBitikMain) "⹁ " else ", ")
            },
            onLongPress = {
                onKeyPress("sys" + if (isBitikMain) ", " else "⹁ ")
            }
        )

        KeyButton(
            title = if (KeyboardViewControllerBase.keyboardMode == KeyboardState.SavedStrings) "⌫" else "↵",
            isSystem = true,
            width = keyWidth * 1.5f,
            backgroundColorIndex = 6,
            isShiftEnabled = isShiftEnabled,
            whilePressing = { lastButton_WhileHolding("\n") },
            onKeyPress = {
                if (KeyboardViewControllerBase.keyboardMode == KeyboardState.SavedStrings) {
                    onKeyPress("delete")
                } else {
                    onKeyPress("\n")
                }
            }
        )
    }
}
