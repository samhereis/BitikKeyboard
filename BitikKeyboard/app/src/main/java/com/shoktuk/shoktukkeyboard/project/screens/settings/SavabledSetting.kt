package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.R
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_Settings

private enum class SavablesHintStep {
    HoldShift, ChooseSavable
}

@Composable
fun SavabledSetting(
    onOpenSavedStrings: () -> Unit,
) {
    var showHint by remember { mutableStateOf(false) }
    var whatIsItExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), tonalElevation = 0.dp, shadowElevation = 0.dp, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showHint = true }) {
                    Text(Loc_Settings.savables.localizedTitle(LocalContext.current), style = MaterialTheme.typography.bodyLarge)
                    Icon(
                        imageVector = Icons.Default.Info, contentDescription = null
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Filled.Edit, modifier = Modifier.clickable { onOpenSavedStrings() }, contentDescription = null, tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp)) {
                HowItLooksPreviewRow(
                    items = SavablesHintStep.entries,
                    selection = null,
                    expanded = whatIsItExpanded,
                    onExpandedChange = { whatIsItExpanded = it },
                    label = {
                        if (it == SavablesHintStep.HoldShift) Loc_Settings.holdShift.localizedTitle(context)
                        else Loc_Settings.andChooseSavables.localizedTitle(context)
                    },
                    imageName = { if (it == SavablesHintStep.HoldShift) "settings_savables_0" else "settings_savables_1" },
                    headerLabel = Loc_Settings.whatIsIt.localizedTitle(context)
                )
            }
        }
    }

    if (showHint) {
        InfoFullScreen(
            drawables = mapOf(
                R.drawable.settings_savables_0 to Loc_Settings.holdShift.localizedTitle(LocalContext.current), R.drawable.settings_savables_1 to ""
            ), onClick = { showHint = false })
    }
}