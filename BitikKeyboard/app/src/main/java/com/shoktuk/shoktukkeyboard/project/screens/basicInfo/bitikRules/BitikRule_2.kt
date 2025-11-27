package com.shoktuk.bittik.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun BitikRule2() {
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
                text = Loc_BasicInfo.RULE2.localizedTitle(LocalContext.current), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = Loc_BitikRules.R_2_HARD_SOFT.localizedTitle(LocalContext.current),
                style = MaterialTheme.typography.bodyLarge
            )

            Text(Loc_BitikRules.R_2_HARD_VOWEL.localizedTitle(LocalContext.current), style = MaterialTheme.typography.bodyLarge)
            Text(Loc_BitikRules.R_2_SOFT_VOWEL.localizedTitle(LocalContext.current), style = MaterialTheme.typography.bodyLarge)

            Column {
                Text("aQ  + aL + aT =  Qalat", color = MaterialTheme.colorScheme.error)
                Text("eK + eL + eT = Kelet", color = MaterialTheme.colorScheme.error)
            }

            // SectionCard 1
            SectionCard {
                Column {
                    Text("𐰴𐰞𐱄 - Qalat ✅")
                    Text("𐰚𐰠𐱅 - Kelet ✅")
                    Text("")
                    Text("𐰴𐰠𐱄 - Kaelat 🚫")
                    Text("𐰚𐰞𐱅 - Kealet 🚫")
                }
            }

            Text(
                text = Loc_BitikRules.R_2_SOFT_WITH_SOFT_HARD_WITH_HARD.localizedTitle(LocalContext.current), style = MaterialTheme.typography.bodyLarge
            )

            Column {
                Text("O  + aY + aD + O = Oydo", color = MaterialTheme.colorScheme.error)
                Text("Ö + eR + ÜK = Örük", color = MaterialTheme.colorScheme.error)
            }

            SectionCard {
                Column {
                    Text("𐰆𐰖𐰑𐰆 - Oydo ✅")
                    Text("𐰇𐰼𐰰 - Örük ✅")
                    Text("")
                    Text("𐰇𐰖𐰑𐰇 - Öayadö 🚫")
                    Text("𐰆𐰼𐰰 - Oerük 🚫")
                }
            }
        }
    }
}

@Composable
fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(25.dp)
            )
            .border(
                width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant ?: MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(25.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.Start, content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BitikRule2_ScreenPreview() {

    ShoktukKeyboardTheme {
        BitikRule2()
    }
}