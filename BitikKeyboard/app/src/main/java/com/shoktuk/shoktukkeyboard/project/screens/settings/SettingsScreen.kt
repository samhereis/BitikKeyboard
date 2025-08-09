package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shoktuk.shoktukkeyboard.project.data.AS_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.E_Letter_Variannt
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager
import com.shoktuk.shoktukkeyboard.project.data.TamgaTranscription
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import localized

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var keyboardVariant by remember { mutableStateOf(SettingsManager.getKeyboardVariant(context)) }
    var eVariant by remember { mutableStateOf(SettingsManager.getEVariant(context)) }
    var textTranscription by remember { mutableStateOf(SettingsManager.getTextTranscription(context)) }
    var letterTranscription by remember { mutableStateOf(SettingsManager.getLeterTranscription(context)) }
    var asVariant by remember { mutableStateOf(SettingsManager.getASVariant(context)) }
    var bitikDialect by remember { mutableStateOf(SettingsManager.getBitikDialect(context)) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CenteredDropdownPopup(
            label = "Битик түрү".localized("loc_settings", context), options = KeyboardVariant.entries, selected = keyboardVariant, onSelect = { variant ->
                keyboardVariant = variant
                SettingsManager.setKeyboardVariant(context, variant)
            }, optionLabel = { it.id.localized("loc_settings", context) }, modifier = Modifier.fillMaxWidth()
        )

        CenteredDropdownPopup(
            label = "Битик диалект", options = BitikDialect.entries, selected = bitikDialect, onSelect = { alpha ->
                bitikDialect = alpha
                SettingsManager.setBitikDialect(context, alpha)
            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
        )

        CenteredDropdownPopup(
            label = "Э тамга", options = E_Letter_Variannt.entries, selected = eVariant, onSelect = { alpha ->
                eVariant = alpha
                SettingsManager.setEVariant(context, alpha)
            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
        )

        CenteredDropdownPopup(
            label = "Жазуу транскрипция", options = TextTranscription.entries, selected = textTranscription, onSelect = { alpha ->
                textTranscription = alpha
                SettingsManager.setTextTranscription(context, alpha)
            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
        )

        CenteredDropdownPopup(
            label = "Тамга транскрипция", options = TamgaTranscription.entries, selected = letterTranscription, onSelect = { alpha ->
                letterTranscription = alpha
                SettingsManager.setLeterTranscription(context, alpha)
            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        Text("Алтай варианты үчүн:")
        CenteredDropdownPopup(
            label = "аС тамга", options = AS_Letter_Variant.entries, selected = asVariant, onSelect = { alpha ->
                asVariant = alpha
                SettingsManager.setASVariant(context, alpha)
            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ShoktukKeyboardTheme {
        SettingsScreen()
    }
}

@Composable
fun <T> CenteredDropdownPopup(
    modifier: Modifier = Modifier, label: String, options: List<T>, selected: T, onSelect: (T) -> Unit, optionLabel: (T) -> String = { it.toString() }
) {
    var showPopup by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            showPopup = true
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = "", onValueChange = {}, interactionSource = interactionSource, readOnly = true, label = {
                Text(modifier = modifier.offset(y = (-0).dp), text = label, fontSize = 15.sp)
            }, trailingIcon = {
                Row(modifier = Modifier.offset(y = 0.dp)) {
                    Text(optionLabel(selected), fontSize = 15.sp)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }, modifier = Modifier
                .fillMaxWidth()
                .clickable { showPopup = true }, colors = TextFieldDefaults.colors(), shape = RoundedCornerShape(8.dp), singleLine = true
        )
    }

    if (showPopup) {
        Dialog(onDismissRequest = { showPopup = false }) {
            Card(
                shape = RoundedCornerShape(12.dp), modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(option)
                                    showPopup = false
                                }
                                .padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = option == selected, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(optionLabel(option), style = MaterialTheme.typography.bodyLarge)
                        }
                        if (option != options.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}