package com.shoktuk.bittik.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
fun BitikRule3() {
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
                text = Loc_BasicInfo.RULE3.localizedTitle(LocalContext.current), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = Loc_BitikRules.R_3_SHORT_WRITING.localizedTitle(LocalContext.current),
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text =Loc_BitikRules.R_3_AUTHOR_SUGGESTIONS.localizedTitle(LocalContext.current), style = MaterialTheme.typography.bodyLarge
            )

            SectionCard {
                Column {
                    Text("Bar - 𐰉𐰺")
                    Text("barış - 𐰉𐰺𐰃𐱀")
                    Text("Barışat - 𐰉𐰺𐰃𐱀𐰀𐱄")
                }
            }

            HorizontalDivider()

            Text(
                text = Loc_BitikRules.R_3_SPECIAL_LETTERS.localizedTitle(LocalContext.current), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary
            )
            Text(
                text =Loc_BitikRules.R_3_SPECIAL_LETTERS_DETAILS.localizedTitle(LocalContext.current), style = MaterialTheme.typography.bodyLarge
            )
            Text("Artıqçılıq:", style = MaterialTheme.typography.bodyLarge)

            Column {
                Text("𐱈𐰷𐰱𐰞𐰷 ✅", color = MaterialTheme.colorScheme.error)
                Text("𐰀𐰺𐱄𐰃𐰴𐰲𐰃𐰞𐰃𐰴 🚫", color = MaterialTheme.colorScheme.error)
            }

            HorizontalDivider()

            SectionCard {
                Column {
                    Text("Jıraq - 𐰳𐰃𐰺𐰴")
                    Text(Loc_BitikRules.R_3_SINGARMONISM_JIRAQ.localizedTitle(LocalContext.current))
                    Text("𐰳𐰃𐰺𐰷 - Jırıq")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BitikRule3_ScreenPreview() {

    ShoktukKeyboardTheme {
        BitikRule3()
    }
}