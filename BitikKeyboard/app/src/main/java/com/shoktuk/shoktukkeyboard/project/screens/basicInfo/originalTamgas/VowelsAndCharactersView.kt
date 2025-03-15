import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.keyboard.KeyboardTheme
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
    val colLeft = listOf(
        BulletItem(text = "А/E - 𐰁/𐰀"),
        BulletItem(text = "E - 𐰅"),
        BulletItem(text = "Ə - 𐰂"),
        BulletItem(text = "И/Ы - 𐰄 ,𐰃"),
        BulletItem(text = "О/U - 𐰆"),
        BulletItem(text = "Ө/Ү - 𐰇/𐰈")
    )
    val colRight = listOf(
        BulletItem(text = "Su - 𐰽"),
        BulletItem(text = "Oq, Oq - 𐰹, 𐰸"),
        BulletItem(text = "Qı, Ιq - 𐰷 ,𐰶"),
        BulletItem(text = "Ük, Ök - 𐰝, 𐰜"),
        BulletItem(text = "Lt - 𐰡"),
        BulletItem(text = "Nt - 𐰦, 𐰧"),
        BulletItem(text = "Nç - 𐰩 ,𐰨"),
        BulletItem(text = "Ot - 𐱇"),
        BulletItem(text = "Baş - 𐱈"),
        BulletItem(text = "An, En - 𐰪, 𐰫")
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, androidx.compose.ui.graphics.Color.Gray, shape = RoundedCornerShape(25.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Each column gets equal horizontal space.
        BulletColumnView(
            items = colLeft,
            headline = "Vowels:",
            textColor = Color(0xFF0B84FE), // #ac8f68 converted to ARGB format
            modifier = Modifier.weight(1f)
        )
        BulletColumnView(
            items = colRight,
            headline = "Characters:",
            textColor = Color(0xFFFFA500),
            modifier = Modifier.weight(1f)
        )
    }
}