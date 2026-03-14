package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_Settings
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class RowItem(val id: Long, var text: String)

@Composable
fun SavedStringsScreen() {
    val ctx = LocalContext.current

    val items = remember {
        mutableStateListOf<RowItem>().apply {
            loadSavedStrings(ctx).forEachIndexed { i, s -> add(RowItem(id = i.toLong(), text = s)) }
        }
    }
    var idCounter by remember { mutableStateOf(items.size.toLong()) }
    var newString by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val heights = remember { mutableStateMapOf<Long, Int>() }
    var draggingId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    fun persist() = persistSavedStrings(ctx, items.map { it.text })
    fun indexOf(id: Long) = items.indexOfFirst { it.id == id }
    fun swapByIndex(from: Int, to: Int) {
        if (from == to || from !in items.indices || to !in items.indices) return
        val moving = items.removeAt(from)
        items.add(to, moving)
        persist()
    }

    fun maybeSwap() {
        val id = draggingId ?: return
        val from = indexOf(id)
        if (from == -1) return
        val h = heights[id] ?: return

        if (dragOffsetY > h / 2f && from < items.lastIndex) {
            dragOffsetY -= (heights[items[from + 1].id] ?: 0)
            swapByIndex(from, from + 1)
        } else if (dragOffsetY < -h / 2f && from > 0) {
            dragOffsetY += (heights[items[from - 1].id] ?: 0)
            swapByIndex(from, from - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LazyColumn(
            state = listState, modifier = Modifier
                .weight(1f)
                .fillMaxWidth(), contentPadding = PaddingValues(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(items, key = { it.id }) { row ->
                val isDragging = draggingId == row.id
                EditableRow(
                    value = row.text, onChange = { nv ->
                    row.text = nv; persist()
                }, onDelete = {
                    items.remove(row)
                    persist()
                }, onDragHandle = {
                    draggingId = row.id; dragOffsetY = 0f
                }, onDragDelta = { dy ->
                    if (draggingId == row.id) {
                        dragOffsetY += dy
                        scope.launch { listState.scrollBy(dy) }
                        maybeSwap()
                    }
                }, onDragEnd = {
                    if (draggingId == row.id) {
                        draggingId = null; dragOffsetY = 0f
                    }
                }, modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(if (isDragging) 1f else 0f)
                        .offset {
                            if (isDragging) IntOffset(0, dragOffsetY.roundToInt()) else IntOffset.Zero
                        }
                        .onSizeChanged { sz -> heights[row.id] = sz.height }, dragging = isDragging
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newString,
                onValueChange = { newString = it },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                placeholder = { Text("...", fontSize = 15.sp) })

            Button(
                onClick = {
                    val t = newString.trim()
                    if (t.isNotEmpty()) {
                        items.add(RowItem(id = idCounter++, text = t))
                        newString = ""; persist()
                    }
                }, enabled = newString.trim().isNotEmpty(), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(Loc_Settings.add.localizedTitle(LocalContext.current), fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun EditableRow(
    value: String,
    onChange: (String) -> Unit,
    onDelete: (() -> Unit)?,
    onDragHandle: () -> Unit,
    onDragDelta: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
    dragging: Boolean = false
) {
    val bringer = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    Surface(
        shape = RoundedCornerShape(10.dp), tonalElevation = if (dragging) 2.dp else 0.dp, shadowElevation = if (dragging) 4.dp else 0.dp, modifier = modifier.clip(RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.DragHandle, contentDescription = "Reorder", modifier = Modifier
                    .size(20.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onDragHandle() }, onDrag = { _, delta -> onDragDelta(delta.y) }, onDragEnd = onDragEnd, onDragCancel = onDragEnd
                        )
                    })

            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                minLines = 1,
                maxLines = 1,
                textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, lineHeight = 16.sp),
                shape = RoundedCornerShape(25.dp),
                placeholder = { Text(Loc_Settings.savables.localizedTitle(LocalContext.current), fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 5.dp)
                    .bringIntoViewRequester(bringer)
                    .onFocusEvent { ev ->
                        if (ev.isFocused) scope.launch { bringer.bringIntoView() }
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            if (onDelete != null) {
                IconButton(
                    onClick = onDelete, modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun SavedStringsScreenPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SavedStringsScreen()
        }
    }
}