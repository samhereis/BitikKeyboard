package com.shoktuk.shoktukkeyboard.project.screens.settings

import BeautifulNavigationItem
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.shoktuk.shoktukkeyboard.keyboard.onSettingChanged
import com.shoktuk.shoktukkeyboard.project.data.AJ_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ANG_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.CLOUD_SYNC_ENABLED
import com.shoktuk.shoktukkeyboard.project.data.AS_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.Arabic_Status
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikFont
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.Coloring
import com.shoktuk.shoktukkeyboard.project.data.EB_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EK_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EN_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ESH_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Variant
import com.shoktuk.shoktukkeyboard.project.data.Latin_CH
import com.shoktuk.shoktukkeyboard.project.data.Latin_NG
import com.shoktuk.shoktukkeyboard.project.data.Latin_O
import com.shoktuk.shoktukkeyboard.project.data.Latin_ZH
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingScreens
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ajVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.angVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.arabicStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.asVariant
import com.shoktuk.shoktukkeyboard.project.data.BitikTranscriptionAlphabets
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikTranscriptionAlphabets
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikFont
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bottomOffset
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.coloring
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ebVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ekVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.enVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.eshVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.freeTamga_Click
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.freeTamga_Hold
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardHeight
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.kirilisaStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinCH
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinNG
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinO
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinZH
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.letterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionsEnabled
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.setSuggestionTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.setSuggestionsEnabled
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
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

/** The store banners pass false for both: [showSliders] the keyboard height / bottom offset sliders, [showVariantNote] the note under the variant picker. */
@Composable
fun MainSettingsScreen(showSliders: Boolean = true, showVariantNote: Boolean = true) {
    val context = LocalContext.current

    var buttonHeight by remember { mutableStateOf(context.keyboardHeight) }
    var bottomOffset by remember { mutableStateOf(context.bottomOffset) }

    var keyboardVariant by remember { mutableStateOf(context.keyboardVariant) }
    var keyboardVariantPreviewExpanded by remember { mutableStateOf(false) }
    var bitikDialect by remember { mutableStateOf(context.bitikDialect) }
    var bitikDialectPreviewExpanded by remember { mutableStateOf(false) }
    var textTranscription by remember { mutableStateOf(context.textTranscription) }
    var textTranscriptionPreviewExpanded by remember { mutableStateOf(false) }
    var letterTranscription by remember { mutableStateOf(context.letterTranscription) }
    var letterTranscriptionPreviewExpanded by remember { mutableStateOf(false) }
    var wordSeparator by remember { mutableStateOf(context.wordSeparator) }
    var wordSeparatorPreviewExpanded by remember { mutableStateOf(false) }

    var ajVariant by remember { mutableStateOf(context.ajVariant) }
    var angVariant by remember { mutableStateOf(context.angVariant) }
    var ebVariant by remember { mutableStateOf(context.ebVariant) }
    var eNariant by remember { mutableStateOf(context.enVariant) }
    var asVariant by remember { mutableStateOf(context.asVariant) }
    var ekVariant by remember { mutableStateOf(context.ekVariant) }
    var eshVariant by remember { mutableStateOf(context.eshVariant) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                if (showSliders) {
                SliderSetting(
                    label = Loc_Settings.buttonHeight.localizedTitle(context), value = buttonHeight, valueRange = 100f..300f, onValueChange = {
                        buttonHeight = it
                        context.keyboardHeight = buttonHeight
                        onSettingChanged.invoke()
                    })
                SliderSetting(
                    label = Loc_Settings.bottomOffset.localizedTitle(context), value = bottomOffset, valueRange = -200f..200f, onValueChange = {
                        bottomOffset = it
                        context.bottomOffset = bottomOffset
                        onSettingChanged.invoke()
                    })
                }

                Text(
                    text = Loc_SideMenu.SETTINGS.localizedTitle(LocalContext.current), style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )

                if (showVariantNote) {
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
                }

                CenteredDropdownPopup(
                    label = Loc_Settings.keyboardVariant.localizedTitle(LocalContext.current),
                    options = BitikVariant.entries,
                    selected = keyboardVariant,
                    onSelect = { variant ->
                        keyboardVariant = variant
                        context.keyboardVariant = variant
                    },
                    optionLabel = { it.id.localized("settings", context) },
                    modifier = Modifier.fillMaxWidth(),
                    onLabelClick = { keyboardVariantPreviewExpanded = !keyboardVariantPreviewExpanded },
                    extraContent = {
                        HowItLooksPreviewRow(
                            items = BitikVariant.entries,
                            selection = keyboardVariant,
                            expanded = keyboardVariantPreviewExpanded,
                            onExpandedChange = { keyboardVariantPreviewExpanded = it },
                            label = { it.id.localized("settings", context) },
                            imageName = { it.settingImageName(BitikVariant.KEY) }
                        )
                    }
                )

                CenteredDropdownPopup(
                    label = Loc_Settings.keyboardDialect.localizedTitle(LocalContext.current),
                    options = BitikDialect.entries,
                    selected = bitikDialect,
                    onSelect = { alpha ->
                        bitikDialect = alpha
                        context.bitikDialect = alpha
                    },
                    optionLabel = { it.id.localized("settings", context) },
                    modifier = Modifier.fillMaxWidth(),
                    onLabelClick = { bitikDialectPreviewExpanded = !bitikDialectPreviewExpanded },
                    extraContent = {
                        HowItLooksPreviewRow(
                            items = BitikDialect.entries,
                            selection = bitikDialect,
                            expanded = bitikDialectPreviewExpanded,
                            onExpandedChange = { bitikDialectPreviewExpanded = it },
                            label = { it.id.localized("settings", context) },
                            imageName = { it.settingImageName(BitikDialect.KEY) }
                        )
                    }
                )

                run {
                    EnumSwitchSetting(
                        label = Loc_Settings.textTranscription.localizedTitle(LocalContext.current),
                        selected = textTranscription,
                        optionOn = TextTranscription.On,
                        optionOff = TextTranscription.Off,
                        onSelect = {
                            context.textTranscription = if (it == TextTranscription.On) TextTranscription.On else TextTranscription.Off
                            textTranscription = context.textTranscription
                        },
                        modifier = Modifier.fillMaxWidth(),
                        onLabelClick = { textTranscriptionPreviewExpanded = !textTranscriptionPreviewExpanded },
                        extraContent = {
                            HowItLooksPreviewRow(
                                items = TextTranscription.entries,
                                selection = textTranscription,
                                expanded = textTranscriptionPreviewExpanded,
                                onExpandedChange = { textTranscriptionPreviewExpanded = it },
                                label = { if (it == TextTranscription.On) "setting_on".localized("settings", context) else "setting_off".localized("settings", context) },
                                imageName = { it.settingImageName(TextTranscription.KEY) }
                            )
                        }
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
                    modifier = Modifier.fillMaxWidth(),
                    onLabelClick = { letterTranscriptionPreviewExpanded = !letterTranscriptionPreviewExpanded },
                    extraContent = {
                        HowItLooksPreviewRow(
                            items = LetterTranscription.entries,
                            selection = letterTranscription,
                            expanded = letterTranscriptionPreviewExpanded,
                            onExpandedChange = { letterTranscriptionPreviewExpanded = it },
                            label = { if (it == LetterTranscription.On) "setting_on".localized("settings", context) else "setting_off".localized("settings", context) },
                            imageName = { it.settingImageName(LetterTranscription.KEY) }
                        )
                    }
                )

                CenteredDropdownPopup(
                    label = Loc_Settings.colon.localizedTitle(LocalContext.current),
                    options = WordSeparator.entries,
                    selected = wordSeparator,
                    onSelect = { alpha ->
                        context.wordSeparator = alpha
                        wordSeparator = context.wordSeparator
                    },
                    optionLabel = { if (it == WordSeparator.Off) "setting_no".localized("settings", context) else it.id },
                    modifier = Modifier.fillMaxWidth(),
                    onLabelClick = { wordSeparatorPreviewExpanded = !wordSeparatorPreviewExpanded },
                    extraContent = {
                        HowItLooksPreviewRow(
                            items = WordSeparator.entries,
                            selection = wordSeparator,
                            expanded = wordSeparatorPreviewExpanded,
                            onExpandedChange = { wordSeparatorPreviewExpanded = it },
                            label = { if (it == WordSeparator.Off) "setting_no".localized("settings", context) else it.id },
                            imageName = { it.settingImageName(WordSeparator.KEY) }
                        )
                    }
                )
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
    }
}

// MARK: - Appearance (coloring, vibration)
