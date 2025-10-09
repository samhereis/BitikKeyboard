package com.shoktuk.shoktukkeyboard.keyboard

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator

@Composable
fun BottomRowView(
    isSymbolsEnabled: MutableState<Boolean>,
    isShiftEnabled: MutableState<Boolean>,
    showEmojis: MutableState<Boolean>,
    onKeyPress: (String) -> Unit = {},
    lastButton_WhileHolding: (String) -> Unit = {},
    switcherTitle: String = "🅰😀",
    switcherTitle_Alt: String = "⓬😀",
    lastButton: String = "arrow.turn.down.left",
    onAdjustCursor: (Int) -> Unit = {}
) {
    var wordSeparatorSettings by remember { mutableStateOf(KeyboardViewControllerBase.context.wordSeparator) }
    var didDragSpace by remember { mutableStateOf(false) }
    var lastSpaceDragX by remember { mutableStateOf(0f) }
    var spaceDragAccumulated by remember { mutableStateOf(0f) }
    val spaceDragStep = 10f
    val keyWidth = KeyboardStyle.keyWidth()

    Row {

        //TOdo: handle if needed language key
        //if (KeyboardViewControllerBase.needLanguageKey) {
        //    KeyButton(
        //        iconName = "globe",
        //        isSystem = true,
        //        width = keyWidth * 1.5f,
        //        backgroundColorIndex = 6,
        //        isShiftEnabled = isShiftEnabled
        //    ) {
        //        onKeyPress("language")
        //    }
        //}

        if (isSymbolsEnabled.value) {
            KeyButton(
                title = switcherTitle, isSystem = true, width = keyWidth * 1.5f, backgroundColorIndex = 6, isShiftEnabled = isShiftEnabled, onKeyPress = {
                    KeyboardViewControllerBase.keyboardMode = KeyboardState.Symbols
                    isSymbolsEnabled.value = false
                })

            KeyButton(
                title = ".", isSystem = true, width = keyWidth, backgroundColorIndex = 1, isShiftEnabled = isShiftEnabled
            )
        } else {
            KeyButton(
                title = switcherTitle_Alt, isSystem = true, width = keyWidth * 1.5f, backgroundColorIndex = 6, isShiftEnabled = isShiftEnabled
            )

            KeyButton(
                title = ".", isSystem = true, width = keyWidth, backgroundColorIndex = 1, isShiftEnabled = isShiftEnabled
            )

        }

        KeyButton(isSystem = true, backgroundColorIndex = 1, isShiftEnabled = isShiftEnabled)

        if (KeyboardViewControllerBase.context.wordSeparator != WordSeparator.NoSpace) {
            if (KeyboardViewControllerBase.keyboardMode == KeyboardState.Main) {
                KeyButton(
                    title = "⁚", isSystem = true, backgroundColorIndex = 1, isShiftEnabled = isShiftEnabled, onKeyPress = {
                        val wordSep = when (wordSeparatorSettings) {
                            WordSeparator.NoSpace -> "⁚"
                            WordSeparator.SpaceBefore -> "⁚ "
                            WordSeparator.ArroundSpace -> " ⁚ "
                            WordSeparator.Off -> "⁚"
                        }
                        onKeyPress("sys$wordSep")
                    })
            }
        }

        KeyButton(
            title = if (KeyboardViewControllerBase.keyboardMode == KeyboardState.Main) "⹁" else ",",
            isSystem = true,
            width = keyWidth,
            backgroundColorIndex = 1,
            isShiftEnabled = isShiftEnabled,
            onKeyPress = {
                onKeyPress("sys" + if (KeyboardViewControllerBase.keyboardMode == KeyboardState.Main) "⹁ " else ", ")
            }

        )

        KeyButton(isSystem = true, width = keyWidth * 1.5f, backgroundColorIndex = 6, isShiftEnabled = isShiftEnabled, whilePressing = { lastButton_WhileHolding("\n") }, onKeyPress = {
            onKeyPress("\n")
        })
    }
}