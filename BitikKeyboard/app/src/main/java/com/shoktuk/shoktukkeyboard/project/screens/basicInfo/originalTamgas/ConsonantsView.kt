import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@Preview(showBackground = true)
@Composable
fun ConsonantsViewPreview() {
    ShoktukKeyboardTheme {
        ConsonantsView()
    }
}

@Composable
fun ConsonantsView() {
    val topLeft = listOf(
        BulletItem(text = "Añ - 𐰧 ,𐰬"),
        BulletItem(text = "Ar - 𐰺"),
        BulletItem(text = "At - 𐱃 ,𐱄"),
        BulletItem(text = "Ay - 𐰗 ,𐰖"),
        BulletItem(text = "As - 𐰽 ,𐱂"),
        BulletItem(text = "Ad - 𐰒 ,𐰑"),
        BulletItem(text = "Ağ - 𐰍 ,𐰎"),
        BulletItem(text = "Aq - 𐰴"),
        BulletItem(text = "Al - 𐰟 ,𐰞"),
        BulletItem(text = "Aş - 𐱁 ,𐱀"),
        BulletItem(text = "Aç - 𐰱 ,𐰲"),
        BulletItem(text = "Ab - 𐰊 ,𐰉"),
        BulletItem(text = "An - 𐰣"),
        BulletItem(text = "", needBullet = false),
        BulletItem(text = "P - 𐰯"),
        BulletItem(text = "Z - 𐰕"),
        BulletItem(text = "M - 𐰢")
    )
    val topRight = listOf(
        BulletItem(text = "Eñ - 𐰮 ,𐰭"),
        BulletItem(text = "Er - 𐰼"),
        BulletItem(text = "Et - 𐱅"),
        BulletItem(text = "Ey - 𐰙 ,𐰘"),
        BulletItem(text = "Es - 𐰾"),
        BulletItem(text = "Ed - 𐰓"),
        BulletItem(text = "Eg - 𐰐 ,𐰏"),
        BulletItem(text = "Ek - 𐰛 ,𐰚"),
        BulletItem(text = "El - 𐰠"),
        BulletItem(text = "Eş - 𐰿"),
        BulletItem(text = "Eç - 𐰳 ,𐰲"),
        BulletItem(text = "Eb - 𐰌 ,𐰋"),
        BulletItem(text = "En - 𐰤 ,𐰥")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(25.dp))
            .padding(16.dp)
    ) {
        Text(text = "Consonants:", fontSize = 30.sp)

        Spacer(modifier = Modifier.height(15.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            // Add weight so both columns share available space
            BulletColumnView(
                items = topLeft, headline = "Hard tamgas:", modifier = Modifier.weight(1f)
            )
            BulletColumnView(
                items = topRight, headline = "Soft tamgas:", modifier = Modifier.weight(1f)
            )
        }
    }
}
