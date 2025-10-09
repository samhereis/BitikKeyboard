package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.emoji.EmojiCategory
import kotlinx.coroutines.launch

private object RecentsStore {
    private const val key = "emojiRecents.v1"
    fun load(ctx: Context): List<String> {
        val prefs = ctx.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString(key, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split('|').filter { it.isNotEmpty() }
    }

    fun save(ctx: Context, list: List<String>) {
        val prefs = ctx.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(key, list.joinToString("|")).apply()
    }
}

@Composable
fun EmojisView(
    onKeyPress: (String) -> Unit,
    onABC: () -> Unit = {},
    onBackspace: () -> Unit = {},
    emojiByType: List<EmojiCategory>,
    tabOrder: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6, 7),
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    var selectedType by rememberSaveable { mutableIntStateOf(tabOrder.firstOrNull() ?: 0) }
    var recents by remember { mutableStateOf(RecentsStore.load(ctx)) }
    val rowState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val rowsCount = 4
    val keyboardRowH = remember { 44.dp }
    val gridHeight = keyboardRowH * 3
    val cellSide = remember(gridHeight) { gridHeight / rowsCount }

    Column(
        modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .height(keyboardRowH)
                .fillMaxWidth()
                .padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            if (recents.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                ) {
                    recents.forEach { e ->
                        Text(
                            text = e, style = MaterialTheme.typography.titleMedium, modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { onKeyPress(e) })
                    }
                }
            } else {
                Text(
                    text = "𐰁𐰶𐰺𐰶 𐰅𐰢𐰈𐰙𐰄𐰠𐰅𐰼 𐰢𐰃𐰣𐰑𐰁 𐰉𐰆𐰞𐱇", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier
                        .weight(1f)
                        .basicMarquee()
                )
            }
        }

        Box(
            modifier = Modifier
                .height(gridHeight)
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            EmojiSectionsRow(state = rowState, categories = tabOrder.mapNotNull { t -> emojiByType.find { it.type == t } }, rowsCount = rowsCount, cellSide = cellSide, onTapEmoji = { e ->
                onKeyPress(e)
                var list = recents.toMutableList()
                list.remove(e)
                list.add(0, e)
                if (list.size > 30) list = list.take(30).toMutableList()
                recents = list
                RecentsStore.save(ctx, list)
            }, onFirstVisibleTypeChanged = { t -> selectedType = t })
        }

        Divider(Modifier.padding(vertical = 4.dp))

        val tabH = keyboardRowH / 2
        Row(
            modifier = Modifier
                .height(tabH)
                .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ABC", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .clickable { onABC() })

            Row(
                modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically
            ) {
                tabOrder.forEach { t ->
                    val label = emojiByType.find { it.type == t }?.displayName ?: "?"
                    TabChip(
                        label = label, active = selectedType == t, onClick = {
                            selectedType = t
                            val index = tabOrder.indexOf(t).coerceAtLeast(0)
                            scope.launch { rowState.animateScrollToItem(index) }
                        })
                }
            }

            Text(
                text = "⌫", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .clickable { onBackspace() })
        }
    }
}

@Composable
private fun TabChip(label: String, active: Boolean, onClick: () -> Unit) {
    val a by animateFloatAsState(if (active) 1f else 0f, label = "tab-anim")
    val bg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f + 0.35f * a)
    val fg = if (active) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        color = bg, shape = MaterialTheme.shapes.small, modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .size(width = 42.dp, height = 28.dp)
            .clickable { onClick() }) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = label, color = fg, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
private fun EmojiSectionsRow(
    state: LazyListState,                // ← was LazyRowState
    categories: List<EmojiCategory>, rowsCount: Int, cellSide: Dp, onTapEmoji: (String) -> Unit, onFirstVisibleTypeChanged: (Int) -> Unit
) {
    LaunchedEffect(state.firstVisibleItemIndex) {
        val idx = state.firstVisibleItemIndex.coerceIn(0, categories.lastIndex)
        onFirstVisibleTypeChanged(categories[idx].type)
    }
    LazyRow(
        state = state, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top
    ) {
        items(categories.size, key = { i -> categories[i].type }) { i ->
            val cat = categories[i]
            Box(
                modifier = Modifier
                    .fillParentMaxHeight()
                    .padding(horizontal = 2.dp)
            ) {
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(rowsCount),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier
                        .width(cellSide * 8)
                        .height(cellSide * rowsCount)
                ) {
                    items(cat.emojis, key = { it }) { e ->
                        Box(
                            contentAlignment = Alignment.Center, modifier = Modifier
                                .size(cellSide)
                                .clickable { onTapEmoji(e) }) {
                            Text(text = e, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}