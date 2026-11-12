package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.personalizeT9
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.setSuggestionTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.setSuggestionsEnabled
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionTranscriptionScript
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionsEnabled
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_Settings

/** The T9 page, laid out like the other settings pages: a bordered card per topic, each setting a bordered switch row. */
@Composable
fun SuggestionsSettingsScreen(onOpenLearnedWords: (() -> Unit)? = null) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsCard(Loc_Settings.personalizeT9.localizedTitle(context)) {
            var personalize by remember { mutableStateOf(context.personalizeT9) }
            EnumSwitchSetting(
                label = Loc_Settings.personalizeT9.localizedTitle(context),
                selected = personalize, optionOn = true, optionOff = false,
                onSelect = { personalize = it; context.personalizeT9 = it },
                extraContent = {
                    Text(Loc_Settings.personalizeT9Note.localizedTitle(context), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (onOpenLearnedWords != null) {
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(Loc_Settings.personalizedT9.localizedTitle(context), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            FilledTonalButton(onClick = onOpenLearnedWords, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
                                Text(Loc_Settings.personalizedT9.localizedTitle(context), style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            )
        }

        SettingsCard("Bitik") {
            ScriptSwitches(WritingSystem.Bitik, Loc_Settings.suggestionsBitikNote.localizedTitle(context))
            var script by remember { mutableStateOf(context.suggestionTranscriptionScript) }
            CenteredDropdownPopup(
                label = Loc_Settings.transcriptionScript.localizedTitle(context),
                selected = script,
                options = listOf("latin", "kiril"),
                onSelect = { script = it; context.suggestionTranscriptionScript = it },
                optionLabel = { if (it == "kiril") Loc_Settings.scriptCyrillic.localizedTitle(context) else Loc_Settings.scriptLatin.localizedTitle(context) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        SettingsCard(Loc_Settings.scriptLatin.localizedTitle(context)) {
            ScriptSwitches(WritingSystem.Latin, Loc_Settings.suggestionsCyrillicNote.localizedTitle(context))
        }
        SettingsCard("Кириллица") {
            ScriptSwitches(WritingSystem.Kiril, null)
        }
        SettingsCard("عربى") {
            ScriptSwitches(WritingSystem.Arab, Loc_Settings.suggestionsArabNote.localizedTitle(context))
        }
    }
}

/** A bordered card with a centred title, as every settings page uses. */
@Composable
fun SettingsCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.inverseOnSurface
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            content()
        }
    }
}

/** The T9 switch of one script and, when it has one, its transcription switch with a note on what it shows. */
@Composable
private fun ScriptSwitches(script: WritingSystem, note: String?) {
    val context = LocalContext.current
    var on by remember { mutableStateOf(context.suggestionsEnabled(script)) }
    EnumSwitchSetting(
        label = Loc_Settings.suggestions.localizedTitle(context),
        selected = on, optionOn = true, optionOff = false,
        onSelect = { on = it; context.setSuggestionsEnabled(script, it) }
    )
    if (note != null) {
        var tr by remember { mutableStateOf(context.suggestionTranscription(script)) }
        EnumSwitchSetting(
            label = Loc_Settings.transcription.localizedTitle(context),
            selected = tr, optionOn = true, optionOff = false,
            onSelect = { tr = it; context.setSuggestionTranscription(script, it) },
            extraContent = { Text(note, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        )
    }
}
