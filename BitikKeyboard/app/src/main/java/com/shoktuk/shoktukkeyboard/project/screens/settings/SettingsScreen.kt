package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager
import com.shoktuk.shoktukkeyboard.project.data.TranscriptionAlphabet
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

// ViewModel
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var keyboardVariant by remember { mutableStateOf(SettingsManager.getKeyboardVariant(context)) }
    var transcriptionAlphabet by remember { mutableStateOf(SettingsManager.getTranscriptionAlphabet(context)) }
    var showVariantDialog by remember { mutableStateOf(false) }
    var showAlphabetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Keyboard Variant Setting
            ListItem(headlineContent = { Text("Keyboard Variant") }, supportingContent = { Text(keyboardVariant.name) }, modifier = Modifier.clickable { showVariantDialog = true })

            // Transcription Alphabet Setting
            ListItem(headlineContent = { Text("Transcription Alphabet") }, supportingContent = { Text(transcriptionAlphabet.name) }, modifier = Modifier.clickable { showAlphabetDialog = true })
        }

        if (showVariantDialog) {
            AlertDialog(onDismissRequest = { showVariantDialog = false }, title = { Text("Select Keyboard Variant") }, text = {
                Column {
                    KeyboardVariant.values().forEach { variant ->
                        RadioButton(
                            selected = variant == keyboardVariant, onClick = {
                                keyboardVariant = variant
                                SettingsManager.setKeyboardVariant(context, variant)
                                showVariantDialog = false
                            })
                        Text(variant.name)
                    }
                }
            }, confirmButton = { /* No need for confirm button */ })
        }

        if (showAlphabetDialog) {
            AlertDialog(onDismissRequest = { showAlphabetDialog = false }, title = { Text("Select Transcription Alphabet") }, text = {
                Column {
                    TranscriptionAlphabet.values().forEach { alphabet ->
                        RadioButton(
                            selected = alphabet == transcriptionAlphabet, onClick = {
                                transcriptionAlphabet = alphabet
                                SettingsManager.setTranscriptionAlphabet(context, alphabet)
                                showAlphabetDialog = false
                            })
                        Text(alphabet.name)
                    }
                }
            }, confirmButton = { /* No need for confirm button */ })
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ShoktukKeyboardTheme {
        SettingsScreen()
    }
}