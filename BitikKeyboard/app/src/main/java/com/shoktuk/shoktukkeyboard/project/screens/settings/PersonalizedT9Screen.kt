package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_Settings
import com.shoktuk.shoktukkeyboard.suggestions.LearnedWords

/** Every word Personalize T9 has learned (in Cyrillic) with how many times it was used, most used first. */
@Composable
fun PersonalizedT9Screen() {
    val context = LocalContext.current
    var rows by remember { mutableStateOf(sorted(LearnedWords.all(context))) }
    var search by remember { mutableStateOf("") }
    var confirmClear by remember { mutableStateOf(false) }
    val shown = if (search.isBlank()) rows else rows.filter { it.first.contains(search.trim(), ignoreCase = true) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      SettingsCard(Loc_Settings.personalizedT9.localizedTitle(context), Modifier.weight(1f)) {
        Text(Loc_Settings.personalizeT9Note.localizedTitle(context), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(search, { search = it }, Modifier.weight(1f), singleLine = true, label = { Text(String.format(Loc_Settings.t9WordsCount.localizedTitle(context), rows.size)) })
            IconButton(onClick = { confirmClear = true }, enabled = rows.isNotEmpty()) { Icon(Icons.Default.Delete, contentDescription = null) }
        }
        if (rows.isEmpty()) Text(Loc_Settings.t9Empty.localizedTitle(context), style = MaterialTheme.typography.bodyMedium)
        LazyColumn(Modifier.fillMaxSize()) {
            items(shown, key = { it.first }) { (word, count) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(word, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Text("$count", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = {
                        LearnedWords.remove(context, word)
                        rows = rows.filter { it.first != word }
                    }) { Icon(Icons.Default.Delete, contentDescription = null) }
                }
                HorizontalDivider()
            }
        }
      }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(Loc_Settings.t9ClearConfirm.localizedTitle(context)) },
            text = { Text(Loc_Settings.t9ClearNote.localizedTitle(context)) },
            confirmButton = { TextButton(onClick = { LearnedWords.clear(context); rows = emptyList(); confirmClear = false }) { Text(Loc_Settings.delete.localizedTitle(context)) } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(Loc_Settings.cancel.localizedTitle(context)) } }
        )
    }
}

private fun sorted(map: Map<String, Int>): List<Pair<String, Int>> =
    map.entries.map { it.key to it.value }.sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
