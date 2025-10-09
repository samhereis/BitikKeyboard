package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SavedStringsView(
    stringsInit: List<String>,
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    var strings by remember { mutableStateOf(stringsInit) }

    LaunchedEffect(Unit) {
        val loaded = loadSavedStrings(ctx)
        strings = if (loaded.isNotEmpty()) loaded else listOf(
            "𐰀𐰺𐰃𐰉𐰬𐰕", "𐰽𐰞𐰢𐱄𐰽𐰕𐰉𐰃", "𐰶𐰺𐰍𐰕𐰽𐱄𐰣", "𐰌𐰄𐱅𐰚", "𐰀𐰞𐰀-𐱄𐰆𐰆"
        )
    }

    Surface(modifier = modifier) {
        Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
            FlowLayout(horizontalSpacing = 5.dp, verticalSpacing = 5.dp) {
                strings.forEach { item ->
                    Text(
                        text = item,
                        maxLines = 1,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable { onKeyPress(item) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FlowLayout(
    horizontalSpacing: Dp = 0.dp, verticalSpacing: Dp = 0.dp, content: @Composable () -> Unit
) {
    Layout(content = content) { measurables, constraints ->
        with(this) {
            val h = horizontalSpacing.roundToPx()
            val v = verticalSpacing.roundToPx()
            val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }
            val maxWidth = constraints.maxWidth
            var x = 0
            var y = 0
            var lineHeight = 0
            val positions = ArrayList<androidx.compose.ui.unit.IntOffset>(placeables.size)
            placeables.forEachIndexed { i, p ->
                if (x > 0 && x + p.width > maxWidth) {
                    x = 0
                    y += lineHeight + v
                    lineHeight = 0
                }
                positions.add(androidx.compose.ui.unit.IntOffset(x, y))
                x += p.width
                if (i != placeables.lastIndex) x += h
                if (p.height > lineHeight) lineHeight = p.height
            }
            val w = if (maxWidth != Int.MAX_VALUE) maxWidth else x
            val hTotal = y + lineHeight
            layout(w, hTotal) {
                placeables.forEachIndexed { i, p ->
                    val pos = positions[i]
                    p.place(pos.x, pos.y)
                }
            }
        }
    }
}

private suspend fun loadSavedStrings(ctx: Context): List<String> = withContext(Dispatchers.IO) {
    val prefs = ctx.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
    val raw = prefs.getString("savedStringsJSON", "") ?: ""
    if (raw.isBlank()) return@withContext emptyList()
    try {
        val type = object : TypeToken<List<String>>() {}.type
        Gson().fromJson<List<String>>(raw, type) ?: emptyList()
    } catch (_: Throwable) {
        emptyList()
    }
}