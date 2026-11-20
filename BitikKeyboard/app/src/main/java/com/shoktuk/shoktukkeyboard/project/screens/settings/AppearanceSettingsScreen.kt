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

@Composable
fun AppearanceSettingsScreen() {
    val context = LocalContext.current

    var coloring by remember { mutableStateOf(context.coloring) }
    var coloringPreviewExpanded by remember { mutableStateOf(false) }
    var bitikFont by remember { mutableStateOf(context.bitikFont) }
    var bitikFontPreviewExpanded by remember { mutableStateOf(false) }
    var vibrations by remember { mutableStateOf(context.vibrations) }

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
                    label = Loc_Settings.coloring.localizedTitle(LocalContext.current),
                    selected = coloring,
                    optionOn = Coloring.On,
                    optionOff = Coloring.Off,
                    onSelect = {
                        context.coloring = if (it == Coloring.On) Coloring.On else Coloring.Off
                        coloring = context.coloring
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onLabelClick = { coloringPreviewExpanded = !coloringPreviewExpanded },
                    extraContent = {
                        HowItLooksPreviewRow(
                            items = Coloring.entries,
                            selection = coloring,
                            expanded = coloringPreviewExpanded,
                            onExpandedChange = { coloringPreviewExpanded = it },
                            label = { if (it == Coloring.On) "setting_on".localized("settings", context) else "setting_off".localized("settings", context) },
                            imageName = { it.settingImageName(Coloring.KEY) }
                        )
                    }
                )

                CenteredDropdownPopup(
                    label = Loc_Settings.bitikFont.localizedTitle(LocalContext.current),
                    options = BitikFont.entries,
                    selected = bitikFont,
                    onSelect = { choice ->
                        bitikFont = choice
                        context.bitikFont = choice
                    },
                    optionLabel = { it.id.localized("settings", context) },
                    modifier = Modifier.fillMaxWidth(),
                    onLabelClick = { bitikFontPreviewExpanded = !bitikFontPreviewExpanded },
                    extraContent = {
                        HowItLooksPreviewRow(
                            items = BitikFont.entries,
                            selection = bitikFont,
                            expanded = bitikFontPreviewExpanded,
                            onExpandedChange = { bitikFontPreviewExpanded = it },
                            label = { it.id.localized("settings", context) },
                            imageName = { it.settingImageName(BitikFont.KEY) }
                        )
                    }
                )

                EnumSwitchSetting(
                    label = Loc_Settings.vibration.localizedTitle(LocalContext.current), selected = vibrations, optionOn = Vibrations.On, optionOff = Vibrations.Off, onSelect = {
                        context.vibrations = if (it == Vibrations.On) Vibrations.On else Vibrations.Off
                        vibrations = context.vibrations
                    }, modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// MARK: - Other Alphabets (latin, arab, kiril)
