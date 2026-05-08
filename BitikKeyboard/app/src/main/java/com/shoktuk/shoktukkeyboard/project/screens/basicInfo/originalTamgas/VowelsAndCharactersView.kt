import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_OriginalBitik
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@Preview(showBackground = true)
@Composable
fun VowelsAndCharactersViewPreview() {
    ShoktukKeyboardTheme {
        VowelsAndCharactersView()
    }
}

@Composable
fun VowelsAndCharactersView() {
    var context = LocalContext.current

    val colLeft = listOf(
        BulletItem(text = "A/E - 𐰁/𐰀"),
        BulletItem(text = "E - 𐰂 ,𐰅"),
        BulletItem(text = "I/İ - 𐰄 ,𐰃"),
        BulletItem(text = "O/U - 𐰆"),
        BulletItem(text = "Ö/Ü - 𐰇/𐰈"),
    )
    val colRight = listOf(
        BulletItem(text = "UQ/OQ - 𐰹, 𐰸"),
        BulletItem(text = "QI/IQ - 𐰷 ,𐰶"),
        BulletItem(text = "ÜK/ÖK - 𐰝, 𐰜"),
        BulletItem(text = "ÇI/Çİ - 𐰱"),
        BulletItem(text = "RT/BAŞ - 𐱈"),
        BulletItem(text = "LT - 𐰡"),
        BulletItem(text = "NT - 𐰦, 𐰧"),
        BulletItem(text = "NÇ - 𐰩 ,𐰨"),
        BulletItem(text = "OT - 𐱇"),
        BulletItem(text = "NY/NÇ - 𐰪, 𐰫"),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, androidx.compose.ui.graphics.Color.Gray, shape = RoundedCornerShape(25.dp))
            .padding(10.dp), horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        BulletColumnView(
            items = colLeft, headline = Loc_OriginalBitik.ot_vowels.localizedTitle(LocalContext.current), textColor = Color(0xFF0B84FE),
            modifier = Modifier.weight(1f)
        )
        BulletColumnView(
            items = colRight, headline = Loc_OriginalBitik.ot_characters.localizedTitle(LocalContext.current), textColor = Color(0xFFFFA500), modifier = Modifier.weight(1f)
        )
    }
}