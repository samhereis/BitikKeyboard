package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.shoktuk.shoktukkeyboard.MainActivity
import com.shoktuk.shoktukkeyboard.project.data.SettingScreens
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
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
    val keyFeedback = rememberKeyFeedback()
    var strings by remember { mutableStateOf(stringsInit) }

    LaunchedEffect(Unit) {
        val loaded = loadSavedStrings(ctx)
        strings = if (loaded.isNotEmpty()) loaded else listOf(
            "𐰀𐰺𐰃𐰉𐰬𐰕", "𐰽𐰞𐰢𐱄𐰽𐰕𐰉𐰃", "𐰶𐰺𐰍𐰕𐰽𐱄𐰣", "𐰌𐰄𐱅𐰚", "𐰀𐰞𐰀-𐱄𐰆𐰆"
        )
    }

    val scrollAreaHeight = KeyboardStyle.rowHeight() * 3
    val chipShape = RoundedCornerShape(12.dp)
    val accent = MaterialTheme.colorScheme.primary

    Surface(modifier = modifier.height(scrollAreaHeight)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp), horizontalArrangement = Arrangement.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.12f))
                        .clickable { keyFeedback(); openSavedStringsEditor(ctx) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(editTitle(), color = accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Box(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 6.dp, vertical = 6.dp)) {
                FlowLayout(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                    strings.forEach { item ->
                        Text(
                            text = item,
                            maxLines = 1,
                            fontSize = 18.sp,
                            fontFamily = KeyboardStyle.bitikFontFamily(),
                            modifier = Modifier
                                .clip(chipShape)
                                .background(accent.copy(alpha = 0.18f))
                                .border(0.5.dp, accent.copy(alpha = 0.4f), chipShape)
                                .clickable { keyFeedback(); onKeyPress(item) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun editTitle(): String {
    val titles = mapOf("ky" to "Өзгөртүү", "tr" to "Düzenle", "kk" to "Өзгерту", "az" to "Redaktə et", "ru" to "Изменить")
    return titles[Locale.getDefault().language] ?: "Edit"
}

private fun openSavedStringsEditor(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
        .putExtra(MainActivity.EXTRA_DESTINATION, SettingScreens.SavedStrings.id)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    context.startActivity(intent)
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
