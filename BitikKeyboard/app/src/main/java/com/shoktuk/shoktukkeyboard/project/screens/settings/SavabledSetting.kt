package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.R

@Composable
fun SavabledSetting(
    onOpenSavedStrings: () -> Unit,
) {
    var showHint by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)), verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = { showHint = true }) {
            Text("Сакталмалар", style = MaterialTheme.typography.bodyLarge)
            Icon(
                imageVector = Icons.Default.Info, contentDescription = null
            )
        }
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.Filled.Edit, modifier = Modifier.clickable { onOpenSavedStrings() }, contentDescription = null, tint = MaterialTheme.colorScheme.primary
        )
    }

    if (showHint) {
        InfoFullScreen(
            drawables = mapOf(
                R.drawable.settings_savables_0 to "Shift басып туруңуз", R.drawable.settings_savables_1 to ""
            ), onClick = { showHint = false })
    }
}