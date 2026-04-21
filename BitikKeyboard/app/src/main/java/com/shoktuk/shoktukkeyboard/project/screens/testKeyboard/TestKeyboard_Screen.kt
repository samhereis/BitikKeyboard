package com.shoktuk.shoktukkeyboard.project.screens.testKeyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.shoktuk.shoktukkeyboard.keyboard.onSettingChanged
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bottomOffset
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardHeight
import com.shoktuk.shoktukkeyboard.project.screens.settings.SliderSetting
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_Settings

@Composable
fun TestKeyboard_Screen() {
    val context = LocalContext.current

    var text by remember { mutableStateOf("") }
    var buttonHeight by remember { mutableStateOf(context.keyboardHeight) }
    var bottomOffset by remember { mutableStateOf(context.bottomOffset) }

    LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 10.dp)
    ) {

        Column(
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            SliderSetting(
                modifier = Modifier.padding(bottom = 15.dp), label = Loc_Settings.buttonHeight.localizedTitle(context), value = buttonHeight, valueRange = 100f..300f, onValueChange = {
                    buttonHeight = it
                    context.keyboardHeight = buttonHeight
                    onSettingChanged.invoke()
                })

            SliderSetting(
                modifier = Modifier.padding(bottom = 15.dp), label = Loc_Settings.bottomOffset.localizedTitle(context), value = bottomOffset, valueRange = -200f..200f, onValueChange = {
                    bottomOffset = it
                    context.bottomOffset = bottomOffset
                    onSettingChanged.invoke()
                })

            TextField(
                value = text, onValueChange = { newValue -> text = newValue }, modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }

        //ResponsibleSearchBar(
        //    modifier = Modifier
        //        .align(Alignment.BottomCenter)
        //        .imePadding()
        //)
    }
}

@Preview(showBackground = true)
@Composable
fun TestKeyboard_ScreenPreview() {
    TestKeyboard_Screen()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResponsibleSearchBar(modifier: Modifier = Modifier) {
    val isKeyboardVisible = WindowInsets.isImeVisible
    var query by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var showPicker by remember { mutableStateOf(false) }
    val controller = rememberColorPickerController()

    Row(
        modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape)
                .background(selectedColor, CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .clickable { showPicker = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Palette, contentDescription = null, tint = if (selectedColor.luminance() < 0.5f) Color.White else Color.Black
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .weight(1f)
                .shadow(4.dp, RoundedCornerShape(28.dp)),
            placeholder = { Text("Search...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(color = selectedColor),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
    }

    if (showPicker) {
        ModalBottomSheet(onDismissRequest = { showPicker = false }) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Text Color", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(20.dp))

                HsvColorPicker(
                    modifier = Modifier.size(250.dp), controller = controller, onColorChanged = { envelope -> selectedColor = envelope.color })

                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .height(35.dp), controller = controller
                )

                Button(
                    onClick = { showPicker = false }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue