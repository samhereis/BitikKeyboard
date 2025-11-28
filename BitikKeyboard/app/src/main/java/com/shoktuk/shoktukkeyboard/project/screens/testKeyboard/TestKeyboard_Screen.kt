package com.shoktuk.shoktukkeyboard.project.screens.testKeyboard

import android.view.WindowInsets
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.keyboard.onSettingChanged
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardHeight
import com.shoktuk.shoktukkeyboard.project.screens.settings.SliderSetting
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_Settings

@Composable
fun TestKeyboard_Screen() {
    val context = LocalContext.current

    var text by remember { mutableStateOf("") }
    var buttonHeight by remember { mutableStateOf(context.keyboardHeight) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.Side.TOP.dp)
            .padding(10.dp)
    ) {
        SliderSetting(
            label = Loc_Settings.buttonHeight.localizedTitle(context), value = buttonHeight, valueRange = 100f..300f, onValueChange = {
                buttonHeight = it
                context.keyboardHeight = buttonHeight
                onSettingChanged.invoke()
            })

        TextField(
            value = text, onValueChange = { newValue -> text = newValue }, modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TestKeyboard_ScreenPreview() {
    TestKeyboard_Screen()
}