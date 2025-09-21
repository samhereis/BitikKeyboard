package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SavedStringsScreen() {
    val ctx = LocalContext.current
    var strings by remember { mutableStateOf(loadSavedStrings(ctx)) }
    var newString by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(strings, key = { i, _ -> i }) { index, value ->
                EditableRow(
                    value = value,
                    onChange = { nv ->
                        strings = strings.toMutableList().also {
                            it[index] = nv
                            persistSavedStrings(ctx, it)
                        }
                    },
                    onDelete = if (value.isNotEmpty()) {
                        {
                            strings = strings.toMutableList().also {
                                it.removeAt(index)
                                persistSavedStrings(ctx, it)
                            }
                        }
                    } else null
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newString,
                onValueChange = { newString = it },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("𐰕𐰬𐰃𐰕𐰳") }
            )
            Button(
                onClick = {
                    val t = newString.trim()
                    if (t.isNotEmpty()) {
                        strings = (strings + t).toMutableList().also { persistSavedStrings(ctx, it) }
                        newString = ""
                    }
                },
                enabled = newString.trim().isNotEmpty()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Кош")
            }
        }
    }
}

@Composable
private fun EditableRow(
    value: String,
    onChange: (String) -> Unit,
    onDelete: (() -> Unit)?
) {
    val bringer = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            singleLine = false,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Сакталмалар") },
            modifier = Modifier
                .weight(1f)
                .bringIntoViewRequester(bringer)
                .onFocusEvent { ev ->
                    if (ev.isFocused) scope.launch { bringer.bringIntoView() }
                }
        )
        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = null)
            }
        }
    }
}