package com.shoktuk.shoktukkeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem

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
    // Use reactive keyboardModeState so recomposition happens on mode change
    val keyboardMode = KeyboardViewControllerBase.keyboardModeState.value
    val isBitikMain = keyboardMode == KeyboardState.Main &&
            KeyboardViewControllerBase.current_writingSystem == WritingSystem.Bitik
    val density = LocalDensity.current

    Row {
        KeyButton(
            title = if (isSymbolsEnabled.value) switcherTitle else switcherTitle_Alt,
            isSystem = true,
            width = keyWidth * 1.5f,
            backgroundColorIndex = 6,
            circular = true,
            isShiftEnabled = isShiftEnabled,
            onKeyPress = { onSwitcherClick() },
            onLongPress = { onSwitcherHold() }
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

        // Space bar with cursor drag support
        SpaceButton(
            modifier = Modifier.weight(1f),
            isBitikMain = isBitikMain,
            isShiftEnabled = isShiftEnabled,
            onSpace = { onKeyPress("sys ") },
            onCursorMove = { delta ->
                try {
                    KeyboardViewControllerBase.context.moveCursor(delta)
                } catch (_: Exception) {}
            }
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
            title = if (keyboardMode == KeyboardState.SavedStrings) "⌫" else "",
            icon = if (keyboardMode != KeyboardState.SavedStrings) {
                { AssetIcon(assetPath = "icons/enter_icon.png", tint = KeyboardStyle.getColor(2)) }
            } else null,
            isSystem = true,
            width = keyWidth * 1.5f,
            backgroundColorIndex = 6,
            circular = true,
            isShiftEnabled = isShiftEnabled,
            whilePressing = { lastButton_WhileHolding("\n") },
            onKeyPress = {
                if (keyboardMode == KeyboardState.SavedStrings) {
                    onKeyPress("delete")
                } else {
                    onKeyPress("\n")
                }
            }
        )
    }
}

@Composable
private fun SpaceButton(
    modifier: Modifier = Modifier,
    isBitikMain: Boolean,
    isShiftEnabled: MutableState<Boolean>,
    onSpace: () -> Unit,
    onCursorMove: (Int) -> Unit
) {
    val density = LocalDensity.current
    val keyHeight = KeyboardStyle.rowHeight()
    val bg = KeyboardStyle.getColor(1)
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(
                start = KeyboardStyle.keySidePadding,
                end = KeyboardStyle.keySidePadding,
                top = KeyboardStyle.keyTopPadding,
                bottom = KeyboardStyle.keyTopPadding
            )
            .height(keyHeight)
            .pointerInput(Unit) {
                val stepPx = with(density) { 12.dp.toPx() }
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    var lastStep = 0
                    var accumulated = 0f
                    var didDrag = false

                    try {
                        drag(down.id) { change ->
                            accumulated += (change.position - change.previousPosition).x
                            val step = (accumulated / stepPx).toInt()
                            if (step != lastStep) {
                                val delta = step - lastStep
                                lastStep = step
                                didDrag = true
                                onCursorMove(delta)
                            }
                            change.consume()
                        }
                    } catch (_: Exception) {}

                    isPressed = false
                    if (!didDrag) {
                        onSpace()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(KeyboardStyle.buttonCornerRadius))
                .background(bg)
        )
    }
}
