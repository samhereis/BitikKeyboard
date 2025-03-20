package com.shoktuk.shoktukkeyboard

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import localized

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesOfWritingView() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Rule 1
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(25.dp))
                .padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "1. " + "row_1".localized("loc_rulesOfWriting", context), fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Rule 2
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(25.dp))
                .padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "2. " + "row_2".localized("loc_rulesOfWriting", context), fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "• Alaç : 𐰁𐰞𐰱", color = Color(0xFFFFA500) // Orange
            )
            Text(
                text = "• Laç : 𐰞𐰱", color = Color(0xFFFFA500)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Rule 3
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(25.dp))
                .padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "3. " + "row_3".localized("loc_rulesOfWriting", context), fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "• Soloğoy - ", color = Color(0xFFFFA500)
            )
            Row {
                Text(text = "      🚫 ", color = Color(0xFFFFA500))
                Text(text = "𐰖", color = Color(0xFFFFA500))
                Text(text = "𐰆", color = Color(0xFF03A9F4))
                Text(text = "𐰎", color = Color(0xFFFFA500))
                Text(text = "𐰆", color = Color(0xFF03A9F4))
                Text(text = "𐰞", color = Color(0xFFFFA500))
                Text(text = "𐰆", color = Color(0xFF03A9F4))
                Text(text = "𐱂", color = Color(0xFFFFA500))
            }
            Row {
                Text(text = "      ✅ ")
                Text(text = "𐰖", color = Color(0xFFFFA500))
                Text(text = "𐰎", color = Color(0xFFFFA500))
                Text(text = "𐰞", color = Color(0xFFFFA500))
                Text(text = "𐰆", color = Color(0xFF03A9F4))
                Text(text = "𐱂", color = Color(0xFFFFA500))
            }
            Text(text = " ")
            Text(text = "• Taratar - 𐱄𐰺𐱄𐰺", color = Color(0xFFFFA500))
            Text(text = "• Taaratar - 𐱄𐰁𐰺𐱄𐰺", color = Color(0xFFFFA500))
            Text(text = "• Tarataar - 𐱄𐰺𐱄𐰁𐰺", color = Color(0xFFFFA500))
            Text(text = " ")
            Row {
                Text(text = "• Uruñuz -", color = Color(0xFFFFA500))
                Text(text = "𐰕", color = Color(0xFFFFA500))
                Text(text = "𐰬", color = Color(0xFFFFA500))
                Text(text = "𐰺", color = Color(0xFFFFA500))
                Text(text = "𐰩", color = Color(0xFF03A9F4))
            }
            Row {
                Text(text = "• Uruuñuz -", color = Color(0xFFFFA500))
                Text(text = "𐰕", color = Color(0xFFFFA500))
                Text(text = "𐰬", color = Color(0xFFFFA500))
                Text(text = "𐰩", color = Color(0xFF03A9F4))
                Text(text = "𐰺", color = Color(0xFFFFA500))
                Text(text = "𐰩", color = Color(0xFF03A9F4))
            }
            Row {
                Text(text = "• Uuruñuz -", color = Color(0xFFFFA500))
                Text(text = "𐰕", color = Color(0xFFFFA500))
                Text(text = "𐰬", color = Color(0xFFFFA500))
                Text(text = "𐰺", color = Color(0xFFFFA500))
                Text(text = "𐰩", color = Color(0xFF03A9F4))
                Text(text = "𐰩", color = Color(0xFF03A9F4))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Rule 4
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(25.dp))
                .padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "4. " + "row_4".localized("loc_rulesOfWriting", context), fontWeight = FontWeight.SemiBold
            )
            Text(text = "• Qoşular - ", color = Color(0xFFFFA500))
            Row {
                Text(text = "      🚫 ", color = Color(0xFFFFA500))
                Text(text = "𐰺", color = Color(0xFFFFA500))
                Text(text = "𐰞", color = Color(0xFFFFA500))
                Text(text = "𐱀", color = Color(0xFFFFA500))
                Text(text = "𐰆", color = Color(0xFF03A9F4))
                Text(text = "𐰴", color = Color(0xFFFFA500))
                Text(text = " - Qoşolor", color = Color(0xFFFFA500))
            }
            Row {
                Text(text = "      🚫 ", color = Color(0xFFFFA500))
                Text(text = "𐰺", color = Color(0xFFFFA500))
                Text(text = "𐰞", color = Color(0xFFFFA500))
                Text(text = "𐰩", color = Color(0xFF03A9F4))
                Text(text = "𐱀", color = Color(0xFFFFA500))
                Text(text = "𐰆", color = Color(0xFF03A9F4))
                Text(text = "𐰴", color = Color(0xFFFFA500))
                Text(text = " - Qoşulur", color = Color(0xFFFFA500))
            }
            Row {
                Text(text = "      ✅ ")
                Text(text = "𐰺", color = Color(0xFFFFA500))
                Text(text = "𐰁", color = Color(0xFF03A9F4))
                Text(text = "𐰞", color = Color(0xFFFFA500))
                Text(text = "𐰩", color = Color(0xFF03A9F4))
                Text(text = "𐱀", color = Color(0xFFFFA500))
                Text(text = "𐰆", color = Color(0xFF03A9F4))
                Text(text = "𐰴", color = Color(0xFFFFA500))
                Text(text = " - Qoşular", color = Color(0xFFFFA500))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Rule 5
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(25.dp))
                .padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "5. " + "row_5".localized("loc_rulesOfWriting", context), fontWeight = FontWeight.SemiBold
            )
            Column {
                Row {
                    Text(text = "• Turmuş -", color = Color(0xFFFFA500))
                    Text(text = "𐱀", color = Color(0xFFFFA500))
                    Text(text = "𐰺'𐰢", color = Color(0xFF03A9F4))
                    Text(text = "𐱄𐰩", color = Color(0xFFFFA500))
                }
                Text(text = "• Turumuş - 𐱄𐰩𐰺𐰢𐱀", color = Color(0xFFFFA500))
                Row {
                    Text(text = "• Atmış - ", color = Color(0xFFFFA500))
                    Text(text = "𐰃𐱀", color = Color(0xFFFFA500))
                    Text(text = "𐱄'𐰢", color = Color(0xFF03A9F4))
                    Text(text = "𐰁", color = Color(0xFFFFA500))
                }
                Text(text = "• Atamış - 𐰁𐱄𐰢𐰃𐱀", color = Color(0xFFFFA500))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Rule 6
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(25.dp))
                .padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "6. " + "row_6".localized("loc_rulesOfWriting", context), fontWeight = FontWeight.SemiBold
            )
            Column {
                Text(text = "• Apalıq - 𐰁𐰯𐰞𐰷 → 𐰁𐰯𐰞𐰃𐰴", color = Color(0xFFFFA500))
                Text(text = "• Oqusun - 𐰹𐰩𐰽𐰣 → 𐰆𐰴𐰩𐱂𐰣", color = Color(0xFFFFA500))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Rule 7
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(25.dp))
                .padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "7. " + "row_7".localized("loc_rulesOfWriting", context), fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "• " + "row_7_1".localized("loc_rulesOfWriting", context), color = Color(0xFFFFA500)
            )
            Text(
                text = "• " + "row_7_2".localized("loc_rulesOfWriting", context), color = Color(0xFFFFA500)
            )
            Column(modifier = Modifier.padding(start = 25.dp)) {
                Text(text = "• Qatar - 𐰴𐱄𐰺", color = Color(0xFFFFA500))
                Text(text = "• Keter - 𐰚𐱅𐰼", color = Color(0xFFFFA500))
                Text(text = "• Qotor - 𐰴𐰆𐱄𐰺", color = Color(0xFFFFA500))
                Text(text = "• Kötör - 𐰚𐰈𐱅𐰼", color = Color(0xFFFFA500))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RulesOfWritingViewPreview() {
    ShoktukKeyboardTheme {
        RulesOfWritingView()
    }
}
