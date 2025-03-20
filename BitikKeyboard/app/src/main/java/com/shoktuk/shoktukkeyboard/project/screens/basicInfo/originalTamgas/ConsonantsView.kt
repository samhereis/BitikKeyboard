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
import androidx.compose.ui.platform.LocalContext
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
    var context = LocalContext.current

    val topLeft = listOf(
        BulletItem(text = "aÑ - 𐰧 ,𐰬"),
        BulletItem(text = "aR - 𐰺"),
        BulletItem(text = "aT - 𐱃 ,𐱄"),
        BulletItem(text = "aY - 𐰗 ,𐰖"),
        BulletItem(text = "aS - 𐰽 ,𐱂"),
        BulletItem(text = "aD - 𐰒 ,𐰑"),
        BulletItem(text = "aĞ - 𐰍 ,𐰎"),
        BulletItem(text = "aQ - 𐰴"),
        BulletItem(text = "aL - 𐰟 ,𐰞"),
        BulletItem(text = "aŞ - 𐱁 ,𐱀"),
        BulletItem(text = "aÇ - 𐰱 ,𐰲"),
        BulletItem(text = "aB - 𐰊 ,𐰉"),
        BulletItem(text = "aN - 𐰣"),
        BulletItem(text = "", needBullet = false),
        BulletItem(text = "P - 𐰯"),
        BulletItem(text = "Z - 𐰕"),
        BulletItem(text = "M - 𐰢")
    )
    val topRight = listOf(
        BulletItem(text = "eÑ - 𐰮 ,𐰭"),
        BulletItem(text = "eR - 𐰼"),
        BulletItem(text = "eT - 𐱅"),
        BulletItem(text = "eY - 𐰙 ,𐰘"),
        BulletItem(text = "eS - 𐰾"),
        BulletItem(text = "eD - 𐰓"),
        BulletItem(text = "eĞ - 𐰐 ,𐰏"),
        BulletItem(text = "eQ - 𐰛 ,𐰚"),
        BulletItem(text = "eL - 𐰠"),
        BulletItem(text = "eŞ - 𐰿"),
        BulletItem(text = "üÇ - 𐰳 ,𐰲"),
        BulletItem(text = "eB - 𐰌 ,𐰋"),
        BulletItem(text = "eN - 𐰤 ,𐰥")
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(25.dp))
            .padding(16.dp)
    ) {
        Text(text = "ot_consonants".localized("loc_originalTamgas", context), fontSize = 30.sp)

        Spacer(modifier = Modifier.height(15.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            BulletColumnView(
                items = topLeft, headline = "ot_hardTamgas".localized("loc_originalTamgas", context), modifier = Modifier.weight(1f)
            )
            BulletColumnView(
                items = topRight, headline = "ot_softTamgas".localized("loc_originalTamgas", context), modifier = Modifier.weight(1f)
            )
        }
    }
}
