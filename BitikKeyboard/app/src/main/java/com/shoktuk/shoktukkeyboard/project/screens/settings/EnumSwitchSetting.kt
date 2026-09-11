package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun <T> EnumSwitchSetting(
    label: String,
    selected: T,
    optionOn: T,
    optionOff: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    onLabelClick: (() -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null
) {
    val isOn = selected == optionOn
    Surface(
        modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), tonalElevation = 0.dp, shadowElevation = 0.dp, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (extraContent == null) Modifier.heightIn(min = 25.dp) else Modifier)
                    .padding(start = 12.dp, end = 12.dp, bottom = 0.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .then(if (onLabelClick != null) Modifier.clickable { onLabelClick() } else Modifier)
                ) {
                    Text(label, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.heightIn(min = 2.dp))
                }
                androidx.compose.material3.Switch(
                    checked = isOn, onCheckedChange = { checked -> onSelect(if (checked) optionOn else optionOff) })
            }

            if (extraContent != null) {
                Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp, top = 0.dp)) {
                    extraContent()
                }
            }
        }
    }
}