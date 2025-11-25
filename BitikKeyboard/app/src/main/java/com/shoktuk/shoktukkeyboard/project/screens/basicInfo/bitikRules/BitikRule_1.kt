package com.shoktuk.bittik.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_BasicInfo
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_BitikRules
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitikRule1() {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp), horizontalAlignment = Alignment.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp), horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = Loc_BasicInfo.RULE1.localizedTitle(LocalContext.current), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = Loc_BitikRules.R_1_RIGHT_TO_LEFT.localizedTitle(LocalContext.current),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BitikRule1_ScreenPreview() {

    ShoktukKeyboardTheme {
        BitikRule1()
    }
}
