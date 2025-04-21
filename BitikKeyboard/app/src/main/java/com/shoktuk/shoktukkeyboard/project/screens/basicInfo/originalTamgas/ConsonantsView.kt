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
        BulletItem(text = "аҢ - 𐰧 ,𐰬"),
        BulletItem(text = "аР - 𐰺"),
        BulletItem(text = "аТ - 𐱃 ,𐱄"),
        BulletItem(text = "аЙ/аЖ - 𐰖"),
        BulletItem(text = "аС - 𐰽 ,𐱂"),
        BulletItem(text = "аД - 𐰒 ,𐰑"),
        BulletItem(text = "аҒ - 𐰎 ,𐰍"),
        BulletItem(text = "аҚ - 𐰴"),
        BulletItem(text = "аЛ - 𐰟 ,𐰞"),
        BulletItem(text = "аБ - 𐰊 ,𐰉"),
        BulletItem(text = "аН - 𐰣"),
        BulletItem(text = "", needBullet = false),
        BulletItem(text = "П - 𐰯"),
        BulletItem(text = "З - 𐰕"),
        BulletItem(text = "Ш - 𐱁 ,𐰿 ,𐱀"),
        BulletItem(text = "Ч - 𐰳 ,𐰲"),
        BulletItem(text = "М - 𐰢")
    )
    val topRight = listOf(
        BulletItem(text = "еҢ - 𐰮 ,𐰭"),
        BulletItem(text = "еР - 𐰼"),
        BulletItem(text = "еТ - 𐱅"),
        BulletItem(text = "еЙ/еЖ - 𐰙 ,𐰘"),
        BulletItem(text = "еС - 𐰾"),
        BulletItem(text = "еД - 𐰓"),
        BulletItem(text = "еГ - 𐰐 ,𐰏"),
        BulletItem(text = "еК - 𐰛 ,𐰚"),
        BulletItem(text = "еЛ - 𐰠"),
        BulletItem(text = "еБ - 𐰋 ,𐰌"),
        BulletItem(text = "еН - 𐰤 ,𐰥")
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
