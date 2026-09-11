package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@Composable
fun <T> CenteredDropdownPopup(
    modifier: Modifier = Modifier,
    label: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    optionLabel: (T) -> String = { it.toString() },
    onLabelClick: (() -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null
) {
    var showPopup by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)

    Surface(
        modifier = modifier.fillMaxWidth(), shape = shape, tonalElevation = 0.dp, shadowElevation = 0.dp, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (extraContent == null) Modifier.heightIn(min = 25.dp) else Modifier)
                    .padding(start = 12.dp, end = 12.dp, bottom = 0.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    label,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .then(if (onLabelClick != null) Modifier.clickable { onLabelClick() } else Modifier)
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                    .clickable { showPopup = true }
                        .padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(optionLabel(selected), fontSize = 15.sp)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            if (extraContent != null) {
                Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp, top = 0.dp)) {
                    extraContent()
                }
            }
        }
    }

    if (showPopup) {
        Dialog(onDismissRequest = { showPopup = false }) {
            Card(
                shape = RoundedCornerShape(14.dp), modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .heightIn(max = 520.dp)
            ) {

                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(label, style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider()

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    items(options.size) { idx ->
                        val option = options[idx]
                        val isSel = option == selected

                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(option)
                                showPopup = false
                            }
                            .padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isSel, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                optionLabel(option), style = MaterialTheme.typography.bodyLarge, color = if (isSel) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (idx != options.lastIndex) HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Preview(name = "CenteredDropdownPopup - plain", showBackground = true)
@Composable
private fun CenteredDropdownPopupPreview_Plain() {
    ShoktukKeyboardTheme {
        var selected by remember { mutableStateOf("Default") }
        Column(Modifier.padding(16.dp)) {
            CenteredDropdownPopup(
                label = "Bitik variant", options = listOf("Default", "Classic", "Modernized"), selected = selected, onSelect = { selected = it }
            )
        }
    }
}

@Preview(name = "CenteredDropdownPopup - with How it looks (collapsed)", showBackground = true)
@Composable
private fun CenteredDropdownPopupPreview_WithPreview_Collapsed() {
    ShoktukKeyboardTheme {
        var selected by remember { mutableStateOf("Default") }
        var expanded by remember { mutableStateOf(false) }
        Column(Modifier.padding(16.dp)) {
            CenteredDropdownPopup(
                label = "Bitik variant",
                options = listOf("Default", "Classic", "Modernized"),
                selected = selected,
                onSelect = { selected = it },
                onLabelClick = { expanded = !expanded },
                extraContent = {
                    HowItLooksPreviewRow(
                        items = listOf("Default", "Classic", "Modernized"),
                        selection = selected,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        label = { it },
                        imageName = { "keyboard_variant_0" }
                    )
                }
            )
        }
    }
}

@Preview(name = "CenteredDropdownPopup - with How it looks (expanded)", showBackground = true)
@Composable
private fun CenteredDropdownPopupPreview_WithPreview_Expanded() {
    ShoktukKeyboardTheme {
        var selected by remember { mutableStateOf("Default") }
        var expanded by remember { mutableStateOf(true) }
        Column(Modifier.padding(16.dp)) {
            CenteredDropdownPopup(
                label = "Bitik variant",
                options = listOf("Default", "Classic", "Modernized"),
                selected = selected,
                onSelect = { selected = it },
                onLabelClick = { expanded = !expanded },
                extraContent = {
                    HowItLooksPreviewRow(
                        items = listOf("Default", "Classic", "Modernized"),
                        selection = selected,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        label = { it },
                        imageName = { "keyboard_variant_0" }
                    )
                }
            )
        }
    }
}