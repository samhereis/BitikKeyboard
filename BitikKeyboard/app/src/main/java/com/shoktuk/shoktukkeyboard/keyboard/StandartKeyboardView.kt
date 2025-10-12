package com.shoktuk.shoktukkeyboard.keyboard

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.emoji.EmojisData
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.writingSystem
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem

@Composable
fun StandardKeyboardView(
    rowsModel: KeyboardRowsModel, keyboardState: MutableState<KeyboardState>, onKeyPress: (String) -> Unit, modifier: Modifier = Modifier
) {
    var isShiftEnabled by remember { mutableStateOf(false) }
    var isSymbolsEnabled by remember { mutableStateOf(false) }
    var showEmojis by remember { mutableStateOf(false) }
    var showSavedStrings by remember { mutableStateOf(false) }

    val cfg = LocalWindowInfo.current
    val savedGridHeight = remember(cfg.containerSize.height) {
        (cfg.containerSize.width / 5.5f).dp
    }

    LaunchedEffect(keyboardState.value) { /* no-op to mirror onChange(of:) */ }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KeyboardStyle.getColor(0)), verticalArrangement = Arrangement.spacedBy(KeyboardStyle.rowSpacingDp)
    ) {
        when {
            showEmojis -> {
                EmojisView(
                    onKeyPress = onKeyPress, onABC = {
                        isShiftEnabled = false
                        isSymbolsEnabled = false
                        showEmojis = false
                    }, onBackspace = { onKeyPress("delete") }, emojiByType = EmojisData.defaultCategories()
                )
            }

            showSavedStrings -> {
                TopRowView(onKeyPress = onKeyPress)

                Divider()

                SavedStringsView(
                    stringsInit = listOf(
                        "Hello", "World", "SwiftUI", "Keyboard", "Premade", "Texts", "Grid", "Buttons", "Click", "Tap", "Sample", "Preview"
                    ), onKeyPress = onKeyPress, modifier = Modifier
                        .fillMaxWidth()
                        .height(savedGridHeight)
                        .padding(5.dp)
                )

                BottomRowView(
                    isSymbolsEnabled = remember { mutableStateOf(showSavedStrings) },
                    isShiftEnabled = remember { mutableStateOf(isShiftEnabled) },
                    showEmojis = remember { mutableStateOf(showEmojis) },
                    onKeyPress = { send ->
                        if (send == " ") onKeyPress(" delete ") else onKeyPress(send)
                    },
                    lastButton_WhileHolding = { send ->
                        if (send == " ") onKeyPress(" delete ") else onKeyPress(send)
                    },
                    switcherTitle = "𐱈𐰴𐰀",
                    switcherTitle_Alt = "𐱈𐰴𐰀",
                    lastButton = "delete.backward"
                )
            }

            else -> {
                if (KeyboardViewControllerBase.context.writingSystem == WritingSystem.Bitik) {
                    TopRowView(onKeyPress = onKeyPress)
                } else {
                    TopRowView_Alphabet(textContent = "", isBitikMode = remember { mutableStateOf(KeyboardViewControllerBase.context.writingSystem == WritingSystem.Bitik) }, onReplaceText = {})
                }

                if (isSymbolsEnabled) {
                    SymbolKeyboardView(
                        onKeyPress = onKeyPress, isShiftEnabled = remember { mutableStateOf(isShiftEnabled) })
                } else {
                    LetterKeyboardView(row1 = rowsModel.row1, row2 = rowsModel.row2, row3 = rowsModel.row3, isShiftEnabled = remember { mutableStateOf(isShiftEnabled) }, onKeyPress = { key ->
                        onKeyPress(key)
                        if (KeyboardViewControllerBase.autoDisableShift) isShiftEnabled = false
                    }, onShiftLongPress = { showSavedStrings = true })
                }

                BottomRowView(
                    isSymbolsEnabled = remember { mutableStateOf(isSymbolsEnabled) },
                    isShiftEnabled = remember { mutableStateOf(isShiftEnabled) },
                    showEmojis = remember { mutableStateOf(showEmojis) },
                    onKeyPress = onKeyPress
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun StandardKeyboardViewPreview() {
    val rows = KeyboardRowsModel()
    val ks = remember { mutableStateOf(KeyboardState.Main) }
    MaterialTheme {
        Column(Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(1f))
            StandardKeyboardView(
                rowsModel = rows, keyboardState = ks, onKeyPress = {})
        }
    }
}