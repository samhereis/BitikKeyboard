package com.shoktuk.shoktukkeyboard.keyboard

import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.shoktuk.shoktukkeyboard.project.screens.settings.LocalInlinePickers
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.project.data.BitikTranscriptionAlphabets
import com.shoktuk.shoktukkeyboard.project.data.Coloring
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.coloring
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionsEnabled
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.setSuggestionTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionTranscriptionScript
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.personalizeT9
import androidx.compose.material3.FilterChip
import com.shoktuk.shoktukkeyboard.project.screens.settings.SuggestionsSettingsScreen
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.setSuggestionsEnabled
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikTranscriptionAlphabets
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.letterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.textTranscription
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.project.screens.settings.AppearanceSettingsScreen
import com.shoktuk.shoktukkeyboard.project.screens.settings.MainSettingsScreen
import com.shoktuk.shoktukkeyboard.project.screens.settings.OtherAlphabetsSettingsScreen
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_Settings
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@Composable
fun KeyboardSettingsPanel(onClose: () -> Unit) {
    val context = LocalContext.current
    var page by remember { mutableStateOf<Int?>(null) }
    var autoBitik by remember { mutableStateOf(MyKeyboardService.isAutoWriteBitikMode) }

    ShoktukKeyboardTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (page != null) page = null else onClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (MyKeyboardService.current_writingSystem == WritingSystem.Bitik) {
                            // Bitik has no Auto Bitik; the suggestions switch takes its place up here.
                            var on by remember { mutableStateOf(context.suggestionsEnabled(WritingSystem.Bitik)) }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(Loc_Settings.suggestions.localizedTitle(context), style = MaterialTheme.typography.titleSmall)
                                Switch(checked = on, onCheckedChange = { on = it; context.setSuggestionsEnabled(WritingSystem.Bitik, it) })
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(Loc_Settings.autoBitik.localizedTitle(context), style = MaterialTheme.typography.titleSmall)
                                Switch(checked = autoBitik, onCheckedChange = {
                                    autoBitik = it
                                    MyKeyboardService.isAutoWriteBitikMode = it
                                    onBitikModeChanged.invoke(it)
                                })
                            }
                        }
                    }
                    // Keeps the Auto Bitik switch centred against the back button.
                    Spacer(Modifier.size(48.dp))
                }

                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (page) {
                        0 -> MainSettingsScreen()
                        1 -> AppearanceSettingsScreen()
                        2 -> OtherAlphabetsSettingsScreen()
                        3 -> SuggestionsSettingsScreen()
                        else -> Column(
                            Modifier.verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // The four pages share one card, split by thin dividers.
                            Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, tonalElevation = 2.dp) {
                                Column {
                                    listOf(Loc_Settings.mainSettings, Loc_Settings.appearance, Loc_Settings.otherAlphabets, Loc_Settings.suggestions).forEachIndexed { index, title ->
                                        if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                                        Row(
                                            Modifier.fillMaxWidth().clickable { page = index }.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(title.localizedTitle(context), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                                        }
                                    }
                                }
                            }

                            // Quick access; the same two settings stay in the full Main Settings page.
                            var transcription by remember { mutableStateOf(context.suggestionTranscription(MyKeyboardService.current_writingSystem)) }
                            var textTr by remember { mutableStateOf(context.textTranscription) }
                            var letterTr by remember { mutableStateOf(context.letterTranscription) }
                            var toBitik by remember { mutableStateOf(context.bitikTranscriptionAlphabets) }
                            var coloringState by remember { mutableStateOf(context.coloring) }
                            val ws = MyKeyboardService.current_writingSystem
                            Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, tonalElevation = 2.dp) {
                                Column {
                                    if (ws == WritingSystem.Bitik) {
                                        QuickToggle(Loc_Settings.textTranscription.localizedTitle(context), textTr == TextTranscription.On, Modifier.fillMaxWidth()) {
                                            context.textTranscription = if (it) TextTranscription.On else TextTranscription.Off
                                            textTr = context.textTranscription
                                        }
                                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                                    } else {
                                        QuickToggle("Transcription to Bitik", toBitik == BitikTranscriptionAlphabets.On, Modifier.fillMaxWidth()) {
                                            context.bitikTranscriptionAlphabets = if (it) BitikTranscriptionAlphabets.On else BitikTranscriptionAlphabets.Off
                                            toBitik = context.bitikTranscriptionAlphabets
                                        }
                                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                                    }
                                    QuickToggle(Loc_Settings.letterTranscription.localizedTitle(context), letterTr == LetterTranscription.On, Modifier.fillMaxWidth()) {
                                        context.letterTranscription = if (it) LetterTranscription.On else LetterTranscription.Off
                                        letterTr = context.letterTranscription
                                    }
                                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                                    QuickToggle(Loc_Settings.coloring.localizedTitle(context), coloringState == Coloring.On, Modifier.fillMaxWidth()) {
                                        context.coloring = if (it) Coloring.On else Coloring.Off
                                        coloringState = context.coloring
                                    }
                                }
                            }

                            // Everything about suggestions in a section of its own.
                            Text(Loc_Settings.suggestions.localizedTitle(context), Modifier.padding(start = 16.dp, top = 8.dp), style = MaterialTheme.typography.titleSmall)
                            var sugg by remember { mutableStateOf(context.suggestionsEnabled(ws)) }
                            var script by remember { mutableStateOf(context.suggestionTranscriptionScript) }
                            var personalize by remember { mutableStateOf(context.personalizeT9) }
                            Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, tonalElevation = 2.dp) {
                                Column {
                                    QuickToggle(Loc_Settings.personalizeT9.localizedTitle(context), personalize, Modifier.fillMaxWidth()) {
                                        context.personalizeT9 = it
                                        personalize = it
                                    }
                                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                                    QuickToggle(Loc_Settings.suggestions.localizedTitle(context), sugg, Modifier.fillMaxWidth()) {
                                        context.setSuggestionsEnabled(ws, it)
                                        sugg = it
                                    }
                                    if (ws != WritingSystem.Kiril) {
                                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                                        QuickToggle(Loc_Settings.suggestionTranscription.localizedTitle(context), transcription, Modifier.fillMaxWidth()) {
                                            context.setSuggestionTranscription(ws, it)
                                            transcription = it
                                        }
                                    }
                                    if (ws == WritingSystem.Bitik) {
                                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(Loc_Settings.transcriptionScript.localizedTitle(context), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                            listOf("latin" to Loc_Settings.scriptLatin.localizedTitle(context), "kiril" to Loc_Settings.scriptCyrillic.localizedTitle(context)).forEach { (id, label) ->
                                                FilterChip(selected = script == id, onClick = { script = id; context.suggestionTranscriptionScript = id }, label = { Text(label) }, modifier = Modifier.padding(start = 8.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickToggle(title: String, checked: Boolean, modifier: Modifier, onChange: (Boolean) -> Unit) {
    Row(modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f).padding(end = 8.dp), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

object SettingsPanelBuilder {
    fun create(service: MyKeyboardService, height: Int, onClose: () -> Unit): ComposeView {
        service.lifecycleOwner.attachToDecorView(service.window?.window?.decorView)
        return ComposeView(service).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            setContent { CompositionLocalProvider(LocalInlinePickers provides true) { KeyboardSettingsPanel(onClose) } }
        }
    }
}
