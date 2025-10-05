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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                text = "3 Кыска жазылышы", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Тамгалар өзүнчө аР/эР, аЛ/эЛ болуп окулгандан, сөздөр кыска жазылат. " + "Азырынча сөздүн тамырын гана кыска жазып, калганын толук жазса болот. " + "Бирок негизи, битикташтарда баары кыска жазылган.",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Азыр ыңгайлуу болуш үчүн, автордун сунушу: сөздүн тамырын гана кыска жазуу.", style = MaterialTheme.typography.bodyLarge
            )

            SectionCard {
                Column {
                    Text("Бар - 𐰉𐰺")
                    Text("Барыш - 𐰉𐰺𐰃𐱀")
                    Text("Барышат - 𐰉𐰺𐰃𐱀𐰀𐱄")
                }
            }

            HorizontalDivider()

            Text(
                text = "3.1 Атайын тамгалар", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Кошумча, кыска жазууга атайын тамгалар бар. Алардын тыбышы эч качан өзгөрбөйт.", style = MaterialTheme.typography.bodyLarge
            )
            Text("Артыкчылык:", style = MaterialTheme.typography.bodyLarge)

            Column {
                Text("𐱈𐰷𐰱𐰞𐰷 ✅", color = MaterialTheme.colorScheme.error)
                Text("𐰀𐰺𐱄𐰃𐰴𐰲𐰃𐰞𐰃𐰴 🚫", color = MaterialTheme.colorScheme.error)
            }

            HorizontalDivider()

            Text(
                text = "3.2 - Сингармонизм менен атайын тамгага туш келсе.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Анда эмки тамга түпкү окулушу менен окулат.", style = MaterialTheme.typography.bodyLarge
            )

            SectionCard {
                Column {
                    Text("Жырак - 𐰳𐰃𐰺𐰴")
                    Text("Себеби “жырык” мындай жазылмак:")
                    Text("𐰳𐰃𐰺𐰷 - Жырык")
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