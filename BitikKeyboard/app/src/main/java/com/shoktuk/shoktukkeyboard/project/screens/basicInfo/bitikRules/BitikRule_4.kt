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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_BasicInfo
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_BitikRules
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitikRule4() {
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
                text = Loc_BasicInfo.RULE4.localizedTitle((LocalContext.current)), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = Loc_BitikRules.R_4_DOUBLE_WORDS.localizedTitle((LocalContext.current)), style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Aybek, Biroq, Beytarap.", color = Color(0xFF2E7D32), // green-ish
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = Loc_BitikRules.R_4_DOUBLE_WORDS_2.localizedTitle((LocalContext.current)), style = MaterialTheme.typography.bodyLarge
            )

            SectionCard {
                Column {
                    Text("Aybek - 𐰀𐰖𐰌𐰚")
                    Text("Biroq - 𐰌𐰄𐰼𐰹")
                    Text("Beytarap - 𐰌𐰘𐱄𐰺𐰯")
                    Text("Erketay - 𐰅𐰼𐰚𐰅𐱄𐰖")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BasicInfo_ScreenPreview() {

    ShoktukKeyboardTheme {
        BitikRule4()
    }
}