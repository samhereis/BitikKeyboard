package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shoktuk.shoktukkeyboard.R
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_Settings

@Composable
fun FreeButtonStringsSection(
    left: String,
    onLeftChange: (String) -> Unit,
    right: String,
    onRightChange: (String) -> Unit,
) {
    var showHint by remember { mutableStateOf(false) }

    TextButton(
        onClick = { showHint = true }, modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp), contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = Loc_Settings.freeLetter.localizedTitle(LocalContext.current), style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = Icons.Default.Info, contentDescription = null
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                Loc_Settings.freeLetter_onHold.localizedTitle(LocalContext.current), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = left, onValueChange = onLeftChange, singleLine = false, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), placeholder = { Text("Басууда") })
        }
        Column(
            modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                Loc_Settings.freeLetter_onPress.localizedTitle(LocalContext.current), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = right, onValueChange = onRightChange, singleLine = false, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), placeholder = { Text("Басып турууда") })
        }
    }

    if (showHint) {
        InfoFullScreen(
            drawables = mapOf(R.drawable.settings_freebutton to Loc_Settings.freeLetter.localizedTitle(LocalContext.current)), onClick = { showHint = false })
    }
}

@Composable
fun InfoFullScreen(
    drawables: Map<Int, String>,
    onClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onClick, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onClick() }) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .align(Alignment.Center)
                    .padding(32.dp)
            ) {
                drawables.forEach { (drawable, description) ->
                    if (description.isNotBlank()) {
                        Text(
                            text = description, style = MaterialTheme.typography.bodyLarge.copy(
                                textAlign = TextAlign.Center, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant
                            ), modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }
                    Image(
                        painter = painterResource(drawable),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .clickable { onClick() })
                }
            }
        }
    }
}