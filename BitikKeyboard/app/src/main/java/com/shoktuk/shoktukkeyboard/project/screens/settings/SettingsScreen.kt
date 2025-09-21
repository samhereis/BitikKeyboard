package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.project.data.AS_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.EB_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EN_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ESH_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.asVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ebVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.enVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.eshVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.freeTamga_Click
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.freeTamga_Hold
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.kirilisaStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.letterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.sounds
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.textTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.vibrations
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.data.Vibrations
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import localized

@Composable
fun SettingsScreen(onOpenSavedStrings: () -> Unit) {
    val context = LocalContext.current
    var keyboardVariant by remember { mutableStateOf(context.keyboardVariant) }
    var bitikDialect by remember { mutableStateOf(context.bitikDialect) }
    var textTranscription by remember { mutableStateOf(context.textTranscription) }
    var letterTranscription by remember { mutableStateOf(context.letterTranscription) }
    var wordSeparator by remember { mutableStateOf(context.wordSeparator) }

    var ebVariant by remember { mutableStateOf(context.ebVariant) }
    var eNariant by remember { mutableStateOf(context.enVariant) }
    var asVariant by remember { mutableStateOf(context.asVariant) }
    var eshVariant by remember { mutableStateOf(context.eshVariant) }

    var vibrations by remember { mutableStateOf(context.vibrations) }
    var sounds by remember { mutableStateOf(context.sounds) }

    var freeTamga_Click by remember { mutableStateOf(context.freeTamga_Click) }
    var freeTamga_Hold by remember { mutableStateOf(context.freeTamga_Hold) }


    var kirilisaStatus by remember { mutableStateOf(context.kirilisaStatus) }
    var latinStatus by remember { mutableStateOf(context.latinStatus) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color = MaterialTheme.colorScheme.tertiary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Жөндөө", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )

                CenteredDropdownPopup(
                    label = "Битик түрү".localized("loc_settings", context), options = BitikVariant.entries, selected = keyboardVariant, onSelect = { variant ->
                    keyboardVariant = variant
                    context.keyboardVariant = variant
                }, optionLabel = { it.id.localized("loc_settings", context) }, modifier = Modifier.fillMaxWidth()
                )

                CenteredDropdownPopup(
                    label = "Битик диалект", options = BitikDialect.entries, selected = bitikDialect, onSelect = { alpha ->
                    bitikDialect = alpha
                    context.bitikDialect = alpha
                }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
                )

                if (keyboardVariant == BitikVariant.CLASSIC) {
                    EnumSwitchSetting(
                        label = "Жазуу транскрипция", selected = textTranscription, optionOn = TextTranscription.On, optionOff = TextTranscription.Off, onSelect = {
                            context.textTranscription = if (it == TextTranscription.On) TextTranscription.On else TextTranscription.Off
                            textTranscription = context.textTranscription
                        }, modifier = Modifier.fillMaxWidth()
                    )
                }

                EnumSwitchSetting(
                    label = "Тамга транскрипция", selected = letterTranscription, optionOn = LetterTranscription.On, optionOff = LetterTranscription.Off, onSelect = {
                        context.letterTranscription = if (it == LetterTranscription.On) LetterTranscription.On else LetterTranscription.Off
                        letterTranscription = context.letterTranscription
                    }, modifier = Modifier.fillMaxWidth()
                )

                CenteredDropdownPopup(
                    label = "Эки чекит", options = WordSeparator.entries, selected = wordSeparator, onSelect = { alpha ->
                    context.wordSeparator = alpha
                    wordSeparator = context.wordSeparator
                }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color = MaterialTheme.colorScheme.primary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Сезилиш", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )

                EnumSwitchSetting(
                    label = "Дирилдөө", selected = vibrations, optionOn = Vibrations.On, optionOff = Vibrations.Off, onSelect = {
                        context.vibrations = if (it == Vibrations.On) Vibrations.On else Vibrations.Off
                        vibrations = context.vibrations
                    }, modifier = Modifier.fillMaxWidth()
                )

                //EnumSwitchSetting(
                //    label = "Тыбыш", selected = sounds, optionOn = Sounds.On, optionOff = Sounds.Off, onSelect = {
                //        context.sounds = if (it == Sounds.On) Sounds.On else Sounds.Off
                //        sounds = context.sounds
                //    }, modifier = Modifier.fillMaxWidth()
                //)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color = MaterialTheme.colorScheme.secondary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Тамгалар", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )
                CenteredDropdownPopup(
                    label = "эБ тамга", options = EB_Letter_Variant.entries, selected = ebVariant, onSelect = { alpha ->
                    ebVariant = alpha
                    context.ebVariant = alpha
                }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
                )

                CenteredDropdownPopup(
                    label = "эН тамга", options = EN_Letter_Variant.entries, selected = eNariant, onSelect = { alpha ->
                    eNariant = alpha
                    context.enVariant = alpha
                }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
                )

                if (bitikDialect == BitikDialect.Altay) {
                    Text("Алтай варианты үчүн:")
                    CenteredDropdownPopup(
                        label = "аС тамга", options = AS_Letter_Variant.entries, selected = asVariant, onSelect = { alpha ->
                        asVariant = alpha
                        context.asVariant = alpha
                    }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
                    )

                    if (keyboardVariant == BitikVariant.SAMAGAN && bitikDialect == BitikDialect.Altay) {
                        CenteredDropdownPopup(
                            label = "эШ тамга", options = ESH_Letter_Variant.entries, selected = eshVariant, onSelect = { alpha ->
                            eshVariant = alpha
                            context.eshVariant = alpha
                        }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color = MaterialTheme.colorScheme.inverseOnSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Жеткиликтүүлүк", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )

                HorizontalDivider()
                FreeButtonStringsSection(freeTamga_Click, {
                    if (!it.isEmpty()) {
                        context.freeTamga_Click = it
                    }
                    freeTamga_Click = it
                }, freeTamga_Hold, {
                    if (!it.isEmpty()) {
                        context.freeTamga_Hold = it
                    }
                    freeTamga_Hold = it
                })

                HorizontalDivider()
                SavabledSetting({ onOpenSavedStrings() })
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color = MaterialTheme.colorScheme.inverseOnSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Кошумча ариптер", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )

                EnumSwitchSetting(
                    label = "Латын жазуусу", selected = latinStatus, optionOn = Latin_Status.On, optionOff = Latin_Status.Off, onSelect = {
                        context.latinStatus = if (it == Latin_Status.On) Latin_Status.On else Latin_Status.Off
                        latinStatus = context.latinStatus
                    }, modifier = Modifier.fillMaxWidth()
                )

                EnumSwitchSetting(
                    label = "Кирил жазуусу", selected = kirilisaStatus, optionOn = Kirilisa_Status.On, optionOff = Kirilisa_Status.Off, onSelect = {
                        context.kirilisaStatus = if (it == Kirilisa_Status.On) Kirilisa_Status.On else Kirilisa_Status.Off
                        kirilisaStatus = context.kirilisaStatus
                    }, modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ShoktukKeyboardTheme {
        SettingsScreen(onOpenSavedStrings = {

        })
    }
}