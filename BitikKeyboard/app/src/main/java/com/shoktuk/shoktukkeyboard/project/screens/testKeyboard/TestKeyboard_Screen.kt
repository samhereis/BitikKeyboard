package com.shoktuk.shoktukkeyboard.project.screens.testKeyboard

import android.view.WindowInsets
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TestKeyboard_Screen() {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.Side.TOP.dp)
            .padding(10.dp)
    ) {
        Text(
            text = "Test your custom keyboard below:", modifier = Modifier.padding(bottom = 8.dp)
        )
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