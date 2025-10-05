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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                text = "Катуу/Жумшак", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Битиктин эң алгачкы эрежеси - тамгалардын катуу/жумшака, же башкача: жөөн/ничкеге бөлүнүшү. " + "Битикте к, л, т тамлалар жок, битикте аК/эК, аЛ/эЛ жана аТ/эТ тамгалар бар.",
                style = MaterialTheme.typography.bodyLarge
            )

            Text("Бул үндүүлөр жоон - АУОЫ.", style = MaterialTheme.typography.bodyLarge)
            Text("Булар ничке - ЭЕҮӨИ.", style = MaterialTheme.typography.bodyLarge)

            Column {
                Text("аК  + аЛ + аТ = Калат", color = MaterialTheme.colorScheme.error)
                Text("эК + эЛ + эТ = Келет", color = MaterialTheme.colorScheme.error)
            }

            // SectionCard 1
            SectionCard {
                Column {
                    Text("𐰴𐰞𐱄 - Калат ✅")
                    Text("𐰚𐰠𐱅 - Келет ✅")
                    Text("")
                    Text("𐰴𐰠𐱄 - Каэлат 🚫")
                    Text("𐰚𐰞𐱅 - Кеалэт 🚫")
                }
            }

            Text(
                text = "Жоон үндүүлөр тек катуу үнсүздөр менен. Ничке үндүүлөр тек жушмак үнсүздөр менен.", style = MaterialTheme.typography.bodyLarge
            )

            Column {
                Text("О  + аЙ + аД + О = Ойдо", color = MaterialTheme.colorScheme.error)
                Text("Ө + эР + ҮК = Өрүк", color = MaterialTheme.colorScheme.error)
            }

            SectionCard {
                Column {
                    Text("𐰆𐰖𐰑𐰆 - Ойдо ✅")
                    Text("𐰇𐰼𐰰 - Өрүк ✅")
                    Text("")
                    Text("𐰇𐰖𐰑𐰇 - Өайдаьө 🚫")
                    Text("𐰆𐰼𐰰 - Оэрүк 🚫")
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