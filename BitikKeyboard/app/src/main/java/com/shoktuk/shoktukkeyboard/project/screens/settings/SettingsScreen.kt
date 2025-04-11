package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager
import com.shoktuk.shoktukkeyboard.project.data.TranscriptionAlphabet
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    // Load persisted settings from your existing settings service.
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
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Keyboard Variant Setting Card
            Card(
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showVariantDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                ListItem(
                    headlineContent = { Text("Keyboard Variant", style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text(keyboardVariant.name, style = MaterialTheme.typography.bodyMedium) })
            }

            // Transcription Alphabet Setting Card
            Card(
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAlphabetDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                ListItem(
                    headlineContent = { Text("Transcription Alphabet", style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text(transcriptionAlphabet.name, style = MaterialTheme.typography.bodyMedium) })
            }
        }

        // Dialog for selecting Keyboard Variant.
        if (showVariantDialog) {
            AlertDialog(onDismissRequest = { showVariantDialog = false }, title = { Text("Select Keyboard Variant") }, text = {
                Column {
                    KeyboardVariant.values().forEach { variant ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                keyboardVariant = variant
                                // Save the new setting via your settings service.
                                SettingsManager.setKeyboardVariant(context, variant)
                                showVariantDialog = false
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = variant == keyboardVariant, onClick = null // The Row handles selection.
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = variant.name)
                        }
                    }
                }
            }, confirmButton = { /* Not needed */ })
        }

        // Dialog for selecting Transcription Alphabet.
        if (showAlphabetDialog) {
            AlertDialog(onDismissRequest = { showAlphabetDialog = false }, title = { Text("Select Transcription Alphabet") }, text = {
                Column {
                    TranscriptionAlphabet.values().forEach { alphabet ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                transcriptionAlphabet = alphabet
                                // Save the new setting via your settings service.
                                SettingsManager.setTranscriptionAlphabet(context, alphabet)
                                showAlphabetDialog = false
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = alphabet == transcriptionAlphabet, onClick = null // The Row handles selection.
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = alphabet.name)
                        }
                    }
                }
            }, confirmButton = { /* Not needed */ })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ShoktukKeyboardTheme {
        SettingsScreen()
    }
}