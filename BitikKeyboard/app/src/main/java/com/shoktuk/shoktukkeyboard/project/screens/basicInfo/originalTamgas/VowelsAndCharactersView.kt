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
        BulletItem(text = "А/E - 𐰁/𐰀"), BulletItem(text = "E - 𐰂 ,𐰅"), BulletItem(text = "Ы/И - 𐰄 ,𐰃"), BulletItem(text = "О/U - 𐰆"), BulletItem(text = "Ө/Ү - 𐰇/𐰈")
    )
    val colRight = listOf(
        BulletItem(text = "УҚ/ОҚ - 𐰹, 𐰸"),
        BulletItem(text = "ҚЫ/ЫҚ - 𐰷 ,𐰶"),
        BulletItem(text = "ҮК/ӨК - 𐰝, 𐰜"),
        BulletItem(text = "ЧЫ/ЧИ - 𐰱"),
        BulletItem(text = "РТ/Баш - 𐱈"),
        BulletItem(text = "ЛТ - 𐰡"),
        BulletItem(text = "НТ - 𐰦, 𐰧"),
        BulletItem(text = "НЧ - 𐰩 ,𐰨"),
        BulletItem(text = "ОТ - 𐱇"),
        BulletItem(text = "НЙ - 𐰪, 𐰫")
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, androidx.compose.ui.graphics.Color.Gray, shape = RoundedCornerShape(25.dp))
            .padding(10.dp), horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        BulletColumnView(
            items = colLeft, headline = "ot_vowels".localized("loc_originalTamgas", context), textColor = Color(0xFF0B84FE), // #ac8f68 converted to ARGB format
            modifier = Modifier.weight(1f)
        )
        BulletColumnView(
            items = colRight, headline = "ot_characters".localized("loc_originalTamgas", context), textColor = Color(0xFFFFA500), modifier = Modifier.weight(1f)
        )
    }
}