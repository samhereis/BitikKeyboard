package com.shoktuk.shoktukkeyboard.project.screens.settings

import NavBarPaddingSolutionView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.keyboard.onSettingChanged
import com.shoktuk.shoktukkeyboard.project.data.AJ_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ANG_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.AS_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.Arabic_Status
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.Coloring
import com.shoktuk.shoktukkeyboard.project.data.EB_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EK_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EN_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ESH_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.HoldabilityColoring
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Variant
import com.shoktuk.shoktukkeyboard.project.data.Latin_ZH
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ajVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.angVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.arabicStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.asVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.coloring
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ebVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ekVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.enVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.eshVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.freeTamga_Click
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.freeTamga_Hold
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.holdabilityColoring
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardHeight
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.kirilisaStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinZH
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.letterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.sounds
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.textTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.vibrations
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.data.Vibrations
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_Settings
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_SideMenu
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import localized

@Composable
fun SettingsScreen(onOpenSavedStrings: () -> Unit) {
    val context = LocalContext.current

    var buttonHeight by remember { mutableStateOf(context.keyboardHeight) }

    var keyboardVariant by remember { mutableStateOf(context.keyboardVariant) }
    var bitikDialect by remember { mutableStateOf(context.bitikDialect) }
    var textTranscription by remember { mutableStateOf(context.textTranscription) }
    var letterTranscription by remember { mutableStateOf(context.letterTranscription) }
    var wordSeparator by remember { mutableStateOf(context.wordSeparator) }

    var ajVariant by remember { mutableStateOf(context.ajVariant) }
    var angVariant by remember { mutableStateOf(context.angVariant) }
    var ebVariant by remember { mutableStateOf(context.ebVariant) }
    var eNariant by remember { mutableStateOf(context.enVariant) }
    var asVariant by remember { mutableStateOf(context.asVariant) }
    var ekVariant by remember { mutableStateOf(context.ekVariant) }
    var eshVariant by remember { mutableStateOf(context.eshVariant) }

    var coloring by remember { mutableStateOf(context.coloring) }
    var holdabilityColoring by remember { mutableStateOf(context.holdabilityColoring) }
    var vibrations by remember { mutableStateOf(context.vibrations) }
    var sounds by remember { mutableStateOf(context.sounds) }

    var freeTamga_Click by remember { mutableStateOf(context.freeTamga_Click) }
    var freeTamga_Hold by remember { mutableStateOf(context.freeTamga_Hold) }

    var latinStatus by remember { mutableStateOf(context.latinStatus) }
    var latinVariant by remember { mutableStateOf(context.latinVariant) }
    var latinZH by remember { mutableStateOf(context.latinZH) }

    var arabStatus by remember { mutableStateOf(context.arabicStatus) }
    var kirilisaStatus by remember { mutableStateOf(context.kirilisaStatus) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                SliderSetting(
                    label = Loc_Settings.buttonHeight.localizedTitle(context), value = buttonHeight, valueRange = 100f..300f, onValueChange = {
                        buttonHeight = it
                        context.keyboardHeight = buttonHeight
                        onSettingChanged.invoke()
                    })

                Text(
                    text = Loc_SideMenu.SETTINGS.localizedTitle(LocalContext.current), style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )

                if (keyboardVariant == BitikVariant.CLASSIC) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), tonalElevation = 2.dp, shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = Loc_Settings.keyboardVariant_Classic_Note.localizedTitle(LocalContext.current),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                } else if (keyboardVariant == BitikVariant.SAMAGAN) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 2.dp,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onErrorContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = Loc_Settings.keyboardVariant_Modern_Note.localizedTitle(LocalContext.current), style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 2.dp,
                        shadowElevation = 4.dp,
                        color = MaterialTheme.colorScheme.background,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = Loc_Settings.keyboardVariant_Standart_Note.localizedTitle(LocalContext.current),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                CenteredDropdownPopup(
                    label = Loc_Settings.keyboardVariant.localizedTitle(LocalContext.current), options = BitikVariant.entries, selected = keyboardVariant, onSelect = { variant ->
                        keyboardVariant = variant
                        context.keyboardVariant = variant
                    }, optionLabel = { it.id.localized("settings", context) }, modifier = Modifier.fillMaxWidth()
                )

                CenteredDropdownPopup(
                    label = Loc_Settings.keyboardDialect.localizedTitle(LocalContext.current), options = BitikDialect.entries, selected = bitikDialect, onSelect = { alpha ->
                        bitikDialect = alpha
                        context.bitikDialect = alpha
                    }, optionLabel = { it.id.localized("settings", context) }, modifier = Modifier.fillMaxWidth()
                )

                if (keyboardVariant != BitikVariant.SAMAGAN) {
                    EnumSwitchSetting(
                        label = Loc_Settings.textTranscription.localizedTitle(LocalContext.current),
                        selected = textTranscription,
                        optionOn = TextTranscription.On,
                        optionOff = TextTranscription.Off,
                        onSelect = {
                            context.textTranscription = if (it == TextTranscription.On) TextTranscription.On else TextTranscription.Off
                            textTranscription = context.textTranscription
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                EnumSwitchSetting(
                    label = Loc_Settings.letterTranscription.localizedTitle(LocalContext.current),
                    selected = letterTranscription,
                    optionOn = LetterTranscription.On,
                    optionOff = LetterTranscription.Off,
                    onSelect = {
                        context.letterTranscription = if (it == LetterTranscription.On) LetterTranscription.On else LetterTranscription.Off
                        letterTranscription = context.letterTranscription
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                CenteredDropdownPopup(
                    label = Loc_Settings.colon.localizedTitle(LocalContext.current), options = WordSeparator.entries, selected = wordSeparator, onSelect = { alpha ->
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
                    text = Loc_Settings.experience.localizedTitle(LocalContext.current), style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )

                EnumSwitchSetting(
                    label = Loc_Settings.coloring.localizedTitle(LocalContext.current), selected = coloring, optionOn = Coloring.On, optionOff = Coloring.Off, onSelect = {
                        context.coloring = if (it == Coloring.On) Coloring.On else Coloring.Off
                        coloring = context.coloring
                    }, modifier = Modifier.fillMaxWidth()
                )

                EnumSwitchSetting(
                    label = Loc_Settings.holdabilityIndicator.localizedTitle(LocalContext.current),
                    selected = holdabilityColoring,
                    optionOn = HoldabilityColoring.On,
                    optionOff = HoldabilityColoring.Off,
                    onSelect = {
                        context.holdabilityColoring = if (it == HoldabilityColoring.On) HoldabilityColoring.On else HoldabilityColoring.Off
                        holdabilityColoring = context.holdabilityColoring
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                EnumSwitchSetting(
                    label = Loc_Settings.vibration.localizedTitle(LocalContext.current), selected = vibrations, optionOn = Vibrations.On, optionOff = Vibrations.Off, onSelect = {
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
                    text = Loc_Settings.letters.localizedTitle(LocalContext.current), style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )

                if (keyboardVariant != BitikVariant.CLASSIC) {
                    CenteredDropdownPopup(
                        label = Loc_Settings.ajLetter.localizedTitle(LocalContext.current), options = AJ_Letter_Variant.entries, selected = ajVariant, onSelect = { alpha ->
                            ajVariant = alpha
                            context.ajVariant = alpha
                        }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
                    )
                }

                CenteredDropdownPopup(
                    label = Loc_Settings.ebLetter.localizedTitle(LocalContext.current), options = EB_Letter_Variant.entries, selected = ebVariant, onSelect = { alpha ->
                        ebVariant = alpha
                        context.ebVariant = alpha
                    }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
                )

                CenteredDropdownPopup(
                    label = Loc_Settings.enLetter.localizedTitle(LocalContext.current), options = EN_Letter_Variant.entries, selected = eNariant, onSelect = { alpha ->
                        eNariant = alpha
                        context.enVariant = alpha
                    }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
                )

                if (bitikDialect == BitikDialect.Altay) {
                    Text(Loc_Settings.forAltayDialekt.localizedTitle(LocalContext.current))
                    CenteredDropdownPopup(
                        label = Loc_Settings.asLetter.localizedTitle(LocalContext.current), options = AS_Letter_Variant.entries, selected = asVariant, onSelect = { alpha ->
                            asVariant = alpha
                            context.asVariant = alpha
                        }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
                    )

                    CenteredDropdownPopup(
                        label = Loc_Settings.ekLetter.localizedTitle(LocalContext.current), options = EK_Letter_Variant.entries, selected = ekVariant, onSelect = { alpha ->
                            ekVariant = alpha
                            context.ekVariant = alpha
                        }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
                    )

                    if (keyboardVariant != BitikVariant.CLASSIC) {
                        CenteredDropdownPopup(
                            label = Loc_Settings.eshLetter.localizedTitle(LocalContext.current), options = ESH_Letter_Variant.entries, selected = eshVariant, onSelect = { alpha ->
                                eshVariant = alpha
                                context.eshVariant = alpha
                            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (bitikDialect == BitikDialect.Orkon) {
                    Text(Loc_Settings.forOrhonDialekt.localizedTitle(LocalContext.current))

                    EnumSwitchSetting(
                        label = Loc_Settings.angLetter.localizedTitle(LocalContext.current), selected = angVariant, optionOn = ANG_Letter_Variant.On, optionOff = ANG_Letter_Variant.Off, onSelect = {
                            context.angVariant = it
                            angVariant = context.angVariant
                        }, modifier = Modifier.fillMaxWidth()
                    )
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
                    text = Loc_Settings.accessability.localizedTitle(LocalContext.current),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
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
                    text = Loc_Settings.otherAlphabets.localizedTitle(LocalContext.current),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                EnumSwitchSetting(
                    label = Loc_Settings.latinAlphabet.localizedTitle(LocalContext.current), selected = latinStatus, optionOn = Latin_Status.On, optionOff = Latin_Status.Off, onSelect = {
                        context.latinStatus = it
                        latinStatus = context.latinStatus
                    }, modifier = Modifier.fillMaxWidth()
                )

                CenteredDropdownPopup(
                    label = Loc_Settings.latinAlphabet.localizedTitle(LocalContext.current), selected = latinVariant, options = Latin_Variant.entries, onSelect = {
                        context.latinVariant = it
                        latinVariant = context.latinVariant
                    }, optionLabel = { it.id.localized("settings", context) }, modifier = Modifier.fillMaxWidth()
                )

                CenteredDropdownPopup(
                    label = "zh (ж)", selected = latinZH, options = Latin_ZH.entries, onSelect = {
                        context.latinZH = it
                        latinZH = context.latinZH
                    }, modifier = Modifier.fillMaxWidth()
                )

                EnumSwitchSetting(
                    label = Loc_Settings.arabAlphabet.localizedTitle(LocalContext.current), selected = arabStatus, optionOn = Arabic_Status.On, optionOff = Arabic_Status.Off, onSelect = {
                        context.arabicStatus = it
                        arabStatus = context.arabicStatus
                    }, modifier = Modifier.fillMaxWidth()
                )

                EnumSwitchSetting(
                    label = Loc_Settings.kirilAlphabet.localizedTitle(LocalContext.current), selected = kirilisaStatus, optionOn = Kirilisa_Status.On, optionOff = Kirilisa_Status.Off, onSelect = {
                        context.kirilisaStatus = if (it == Kirilisa_Status.On) Kirilisa_Status.On else Kirilisa_Status.Off
                        kirilisaStatus = context.kirilisaStatus
                    }, modifier = Modifier.fillMaxWidth()
                )
            }
        }

        NavBarPaddingSolutionView()
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