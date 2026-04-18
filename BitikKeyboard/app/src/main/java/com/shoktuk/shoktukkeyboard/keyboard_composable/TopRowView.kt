package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
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
    onKeyPress: (String) -> Unit = {},
    onAlphabetChange: () -> Unit = {},
    transcription: State<Pair<String, String>> = KeyboardViewControllerBase.transcriptionState
) {
    val ctx = LocalContext.current
    var strings by remember {
        mutableStateOf(
            listOf("𐰀𐰺𐰃𐰉𐰬𐰕", "𐰽𐰞𐰢𐱄𐰽𐰕𐰉𐰃", "𐰶𐰺𐰍𐰕𐰽𐱄𐰣", "𐰌𐰄𐱅𐰚", "𐱀𐰹𐱄𐰸")
        )
    }

    LaunchedEffect(Unit) {
        strings = loadSavedStrings(ctx).ifEmpty {
            listOf("𐰀𐰺𐰃𐰉𐰬𐰕", "𐰽𐰞𐰢𐱄𐰽𐰕𐰉𐰃", "𐰶𐰺𐰍𐰕𐰽𐱄𐰣", "𐰌𐰄𐱅𐰚", "𐱀𐰹𐱄𐰸")
        }
    }

    val showTranscription =
        KeyboardViewControllerBase.current_bitikVariant != BitikVariant.SAMAGAN &&
                KeyboardViewControllerBase.showTextTranscription

    val alphabetLabel = KeyboardViewControllerBase.alphabetLabelState.value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = KeyboardStyle.rowHeight() / 2, max = KeyboardStyle.rowHeight()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Language/alphabet switcher
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(
                    color = KeyboardStyle.getColor(6),
                    shape = MaterialTheme.shapes.small
                )
                .clickable { onAlphabetChange() }
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = alphabetLabel,
                color = KeyboardStyle.getColor(2),
                style = MaterialTheme.typography.titleMedium
            )
        }

        val textColor = KeyboardStyle.getColor(2)

        // Middle content: transcription or saved strings
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showTranscription) {
                val (primary, alt) = transcription.value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("~ ", style = MaterialTheme.typography.bodySmall, color = textColor)
                    if (primary.isNotEmpty()) {
                        val pairs = primary.zip(alt.padEnd(primary.length, ' '))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                            items(pairs) { (bot, top) ->
                                TwoFloorText(top = top.toString(), bottom = bot.toString(), textColor = textColor)
                            }
                        }
                    }
                    Text(" ~", style = MaterialTheme.typography.bodySmall, color = textColor)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    strings.forEach { item ->
                        Text(
                            text = if (item.isEmpty()) " " else item,
                            color = textColor,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .background(
                                    color = KeyboardStyle.getColor(1),
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable { onKeyPress(item) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // IME picker
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(
                    color = KeyboardStyle.getColor(6),
                    shape = MaterialTheme.shapes.small
                )
                .clickable {
                    val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showInputMethodPicker()
                }
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "⌨",
                color = KeyboardStyle.getColor(2),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun TwoFloorText(top: String, bottom: String, textColor: Color = Color.Unspecified) {
    val fullStyle = MaterialTheme.typography.bodySmall
    val halfStyle = fullStyle.copy(
        fontSize = fullStyle.fontSize * 0.5f,
        lineHeight = fullStyle.fontSize * 0.55f
    )

    if (top.isBlank() || top == bottom) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = bottom, style = fullStyle, color = textColor, maxLines = 1)
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = top,
                style = halfStyle.copy(fontWeight = FontWeight.Normal),
                color = textColor,
                maxLines = 1
            )
            Text(
                text = bottom,
                style = halfStyle,
                color = textColor,
                maxLines = 1
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
    } catch (_: Throwable) {
        emptyList()
    }
}
