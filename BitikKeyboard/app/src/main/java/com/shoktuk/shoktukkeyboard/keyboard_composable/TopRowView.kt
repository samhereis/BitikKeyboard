package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TopRowFrame(
    alphabetLabel: String,
    onAlphabetChange: () -> Unit,
    centerModifier: Modifier = Modifier,
    centerContent: @Composable BoxScope.() -> Unit
) {
    val ctx = LocalContext.current
    val textColor = KeyboardStyle.getColor(2)
    val keyFeedback = rememberKeyFeedback()

    Surface(color = Color.Transparent) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .height(KeyboardStyle.rowHeight())
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(KeyboardStyle.getColor(6), shape = MaterialTheme.shapes.small)
                    .pointerInput(Unit) { detectTapGestures { keyFeedback(); onAlphabetChange() } }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(alphabetLabel, color = textColor, style = MaterialTheme.typography.titleMedium)
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp)
                    .then(centerModifier)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KeyboardStyle.getColor(0).copy(alpha = 0.01f))
                )
                centerContent()
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(KeyboardStyle.getColor(6), shape = MaterialTheme.shapes.small)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            keyFeedback()
                            (ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                                .showInputMethodPicker()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("⌨", color = textColor, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

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

    val textColor = KeyboardStyle.getColor(2)
    val keyFeedback = rememberKeyFeedback()

    TopRowFrame(
        alphabetLabel = KeyboardViewControllerBase.alphabetLabelState.value,
        onAlphabetChange = onAlphabetChange
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
                            TwoFloorText(top.toString(), bot.toString(), textColor)
                        }
                    }
                }
                Text(" ~", style = MaterialTheme.typography.bodySmall, color = textColor)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                strings.forEach { item ->
                    Text(
                        text = item.ifEmpty { " " },
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .background(KeyboardStyle.getColor(1), MaterialTheme.shapes.small)
                            .pointerInput(item) { detectTapGestures { keyFeedback(); onKeyPress(item) } }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TwoFloorText(top: String, bottom: String, textColor: Color = Color.Unspecified) {
    val full = MaterialTheme.typography.bodySmall
    val half = full.copy(fontSize = full.fontSize * 0.5f, lineHeight = full.fontSize * 0.55f)
    if (top.isBlank() || top == bottom) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(bottom, style = full, color = textColor, maxLines = 1)
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
               verticalArrangement = Arrangement.Center) {
            Text(top,     style = half.copy(fontWeight = FontWeight.Normal), color = textColor, maxLines = 1)
            Text(bottom,  style = half,                                       color = textColor, maxLines = 1)
        }
    }
}

@Preview(name = "TopRowView – Light", showBackground = true)
@Composable
private fun TopRowViewPreview_Light() {
    ShoktukKeyboardTheme(darkTheme = false) { TopRowView() }
}

@Preview(name = "TopRowView – Dark", showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopRowViewPreview_Dark() {
    ShoktukKeyboardTheme(darkTheme = true) { TopRowView() }
}

private suspend fun loadSavedStrings(ctx: Context): List<String> = withContext(Dispatchers.IO) {
    val prefs = ctx.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
    val raw   = prefs.getString("savedStringsJSON", "") ?: ""
    if (raw.isBlank()) return@withContext emptyList()
    return@withContext try {
        Gson().fromJson<List<String>>(raw, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    } catch (_: Throwable) { emptyList() }
}
