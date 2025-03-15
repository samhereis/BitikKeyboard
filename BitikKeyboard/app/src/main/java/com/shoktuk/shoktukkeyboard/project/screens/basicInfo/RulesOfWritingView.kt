import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@Preview(showBackground = true)
@Composable
fun RulesOfWritingViewPreview() {
    ShoktukKeyboardTheme {
        RulesOfWritingView()
    }
}

@Composable
fun RulesOfWritingView() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Rule 1
        Text("1. Read and write from right to left.", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        // Rule 2
        Column {
            Text("2. If a letter starts with a vowel, always write the vowel:", fontWeight = FontWeight.SemiBold)

            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text("• Alaç : 𐰁𐰞𐰱", color = Color(0xFFFFA500))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Rule 3
        Column {
            Text("3. If the next vowel is the same as the previous, skip it:", fontWeight = FontWeight.SemiBold)

            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text("• Taratar - 𐱄𐰺𐱄𐰺", color = Color(0xFFFFA500))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Rule 4
        Column {
            Text("4. If the next vowel is different than the previous, write it:", fontWeight = FontWeight.SemiBold)

            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text("• Aytılğan - 𐰁𐰖'𐱄𐰃𐰞'𐰎𐰁𐰣", color = Color(0xFFFFA500))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Rule 5
        Column {
            Text("5. If there are two consonants, add a ‘ between them for a pause:", fontWeight = FontWeight.SemiBold)

            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text("• Turmuş   - 𐱄𐰩𐰺'𐰢𐱀", color = Color(0xFFFFA500))
                Text("• Turumuş - 𐱄𐰩𐰺𐰢𐱀", color = Color(0xFFFFA500))
                Text("• Atmış   - 𐰁𐱄'𐰢𐰃𐱀", color = Color(0xFFFFA500))
                Text("• Atamış - 𐰁𐱄𐰢𐰃𐱀", color = Color(0xFFFFA500))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Rule 6
        Column {
            Text("6. Certain characters never change, so use them for shortening:", fontWeight = FontWeight.SemiBold)

            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text("• Apalıq - 𐰁𐰯𐰞𐰷 → 𐰁𐰯𐰞𐰃𐰴", color = Color(0xFFFFA500))
                Text("• Oqusun - 𐰹𐰩𐰽𐰣 → 𐰆𐰴𐰩𐱂𐰣", color = Color(0xFFFFA500))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Rule 7
        Column {
            Text("7. There are hard and soft tamgas.", fontWeight = FontWeight.SemiBold)

            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(
                    "• Hard ones are used with: A, U, Ι (Ы), and O.\n• Soft ones are used with: E, Ü, İ (И), and Ö.", color = Color(0xFFFFA500)
                )
            }

            Column(modifier = Modifier.padding(start = 40.dp)) {
                Text("• Qatar - 𐰴𐱄𐰺", color = Color(0xFFFFA500))
                Text("• Keter - 𐰚𐱅𐰼", color = Color(0xFFFFA500))
                Text("• Qotor - 𐰴𐰆𐱄𐰺", color = Color(0xFFFFA500))
                Text("• Kötör - 𐰚𐰈𐱅𐰼", color = Color(0xFFFFA500))
            }
        }
    }
}