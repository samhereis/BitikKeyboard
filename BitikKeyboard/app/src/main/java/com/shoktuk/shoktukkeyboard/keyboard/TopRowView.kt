package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TopRowView(
    onKeyPress: (String) -> Unit = {}
) {
    val ctx = LocalContext.current
    var displayText by remember { mutableStateOf("") }
    var displayTextAlternative by remember { mutableStateOf("") }
    var strings by remember {
        mutableStateOf(
            listOf(
                "𐰀𐰺𐰃𐰉𐰬𐰕", "𐰽𐰞𐰢𐱄𐰽𐰕𐰉𐰃", "𐰶𐰺𐰍𐰕𐰽𐱄𐰣", "𐰌𐰄𐱅𐰚", "𐱀𐰹𐱄𐰸"
            )
        )
    }

    LaunchedEffect(Unit) {
        strings = loadSavedStrings(ctx).ifEmpty {
            listOf(
                "𐰀𐰺𐰃𐰉𐰬𐰕", "𐰽𐰞𐰢𐱄𐰽𐰕𐰉𐰃", "𐰶𐰺𐰍𐰕𐰽𐱄𐰣", "𐰌𐰄𐱅𐰚", "𐱀𐰹𐱄𐰸"
            )
        }
    }

    val transcription = KeyboardViewControllerBase.current_bitikVariant != BitikVariant.SAMAGAN && KeyboardViewControllerBase.showTextTranscription

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (transcription) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text("~ ", style = MaterialTheme.typography.bodySmall)
                if (displayText.isNotEmpty()) {
                    val pairs = displayText.zip(displayTextAlternative)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                        items(pairs) { p ->
                            TwoFloorText(top = p.second.toString(), bottom = p.first.toString())
                        }
                    }
                }
                Text(" ~", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = KeyboardStyle.rowHeight() / 2, max = KeyboardStyle.rowHeight())
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    strings.forEach { item ->
                        Text(
                            text = if (item.isEmpty()) " " else item, style = MaterialTheme.typography.titleMedium, modifier = Modifier
                                .padding(horizontal = 5.dp, vertical = 0.dp)
                                .background(
                                    color = KeyboardStyle.getColor(1), shape = MaterialTheme.shapes.small
                                )
                                .then(Modifier.clickable { onKeyPress(item) }.padding(horizontal = 8.dp, vertical = 4.dp)))
                    }
                }
            }
        }
    }
}

@Composable
fun TwoFloorText(top: String, bottom: String) {
    if (top == bottom) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 0.dp)
        ) {
            Text(
                text = top, style = MaterialTheme.typography.bodySmall, maxLines = 1
            )
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 0.dp)
        ) {
            Text(
                text = top, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), maxLines = 1
            )
            Text(
                text = bottom, style = MaterialTheme.typography.bodySmall, maxLines = 1
            )
        }
    }
}

private suspend fun loadSavedStrings(ctx: Context): List<String> = withContext(Dispatchers.IO) {
    val prefs = ctx.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
    val raw = prefs.getString("savedStringsJSON", "") ?: ""
    if (raw.isBlank()) return@withContext emptyList()
    return@withContext try {
        val type = object : TypeToken<List<String>>() {}.type
        Gson().fromJson<List<String>>(raw, type) ?: emptyList()
    } catch (e: Throwable) {
        emptyList()
    }
}