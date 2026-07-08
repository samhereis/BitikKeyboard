package com.shoktuk.shoktukkeyboard.keyboard

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.shoktuk.shoktukkeyboard.emoji.EmojisData
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import kotlin.math.absoluteValue

@Composable
fun StandardKeyboardView(
    rowsModel: KeyboardRowsModel,
    onKeyPress: (String) -> Unit,
    onAlphabetChange: () -> Unit = {},
    onModeChange: (KeyboardState) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isShiftEnabled = remember { mutableStateOf(false) }
    val isSymbolsEnabled = remember { mutableStateOf(false) }

    val keyboardMode = KeyboardViewControllerBase.keyboardModeState.value
    val writingSystem = KeyboardViewControllerBase.writingSystemState.value
    val bottomPaddingPx = KeyboardViewControllerBase.bottomPaddingState.value.absoluteValue
    val bottomPaddingDp = with(LocalDensity.current) { bottomPaddingPx.toDp() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = bottomPaddingDp)
            .background(KeyboardStyle.getColor(0)),
        verticalArrangement = Arrangement.spacedBy(KeyboardStyle.rowSpacingDp)
    ) {
        when (keyboardMode) {
            KeyboardState.Emojis -> {
                EmojisView(
                    onKeyPress = onKeyPress,
                    onABC = {
                        isShiftEnabled.value = false
                        isSymbolsEnabled.value = false
                        KeyboardViewControllerBase.keyboardMode = KeyboardState.Main
                        onModeChange(KeyboardState.Main)
                    },
                    onBackspace = { onKeyPress("delete") },
                    emojiByType = EmojisData.defaultCategories()
                )
            }

            KeyboardState.SavedStrings -> {
                if (writingSystem == WritingSystem.Bitik) {
                    TopRowView(onKeyPress = onKeyPress, onAlphabetChange = onAlphabetChange)
                } else {
                    TopRowView_Alphabet(onAlphabetChange = onAlphabetChange)
                }

                HorizontalDivider()

                SavedStringsView(
                    stringsInit = emptyList(),
                    onKeyPress = onKeyPress,
                    modifier = Modifier.fillMaxWidth()
                )

                BottomRowView(
                    isSymbolsEnabled = isSymbolsEnabled,
                    isShiftEnabled = isShiftEnabled,
                    onKeyPress = onKeyPress,
                    onSwitcherClick = {
                        KeyboardViewControllerBase.keyboardMode = KeyboardState.Main
                        onModeChange(KeyboardState.Main)
                    },
                    onSwitcherHold = {
                        KeyboardViewControllerBase.keyboardMode = KeyboardState.Main
                        onModeChange(KeyboardState.Main)
                    }
                )
            }

            else -> {
                if (writingSystem == WritingSystem.Bitik) {
                    TopRowView(onKeyPress = onKeyPress, onAlphabetChange = onAlphabetChange)
                } else {
                    TopRowView_Alphabet(onAlphabetChange = onAlphabetChange)
                }

                key(KeyboardViewControllerBase.reloadGenerationState.value) {
                    if (keyboardMode == KeyboardState.Symbols || isSymbolsEnabled.value) {
                        SymbolKeyboardView(
                            onKeyPress = onKeyPress,
                            isShiftEnabled = isShiftEnabled
                        )
                    } else {
                        LetterKeyboardView(
                            row1 = rowsModel.row1,
                            row2 = rowsModel.row2,
                            row3 = rowsModel.row3,
                            isShiftEnabled = isShiftEnabled,
                            onKeyPress = { key ->
                                onKeyPress(key)
                                if (KeyboardViewControllerBase.autoDisableShift &&
                                    !key.startsWith("sys") && key != "delete"
                                ) {
                                    isShiftEnabled.value = false
                                }
                            },
                            onShiftLongPress = {
                                KeyboardViewControllerBase.keyboardMode = KeyboardState.SavedStrings
                                onModeChange(KeyboardState.SavedStrings)
                            }
                        )
                    }
                }

                BottomRowView(
                    isSymbolsEnabled = isSymbolsEnabled,
                    isShiftEnabled = isShiftEnabled,
                    onKeyPress = onKeyPress,
                    onSwitcherClick = {
                        val newSymbols = !isSymbolsEnabled.value
                        isSymbolsEnabled.value = newSymbols
                        val newMode = if (newSymbols) KeyboardState.Symbols else KeyboardState.Main
                        KeyboardViewControllerBase.keyboardMode = newMode
                        onModeChange(newMode)
                    },
                    onSwitcherHold = {
                        KeyboardViewControllerBase.keyboardMode = KeyboardState.Emojis
                        onModeChange(KeyboardState.Emojis)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun StandardKeyboardViewPreview() {
    val rows = KeyboardRowsModel()
    MaterialTheme {
        Column(Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(1f))
            StandardKeyboardView(rowsModel = rows, onKeyPress = {})
        }
    }
}
