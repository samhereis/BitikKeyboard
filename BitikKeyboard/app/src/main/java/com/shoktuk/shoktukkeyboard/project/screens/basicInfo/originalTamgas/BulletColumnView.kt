import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

data class BulletItem(
    val text: String, val needBullet: Boolean = true
)

@Preview(showBackground = true)
@Composable
fun BulletColumnViewPreview() {
    ShoktukKeyboardTheme {
        BulletItem(text = "Añ - 𐰧 ,𐰬")
    }
}

@Composable
fun BulletColumnView(
    items: List<BulletItem>,
    headline: String? = null,
    textColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(25.dp))
            .padding(16.dp)
    ) {
        headline?.let {
            Text(text = it, fontSize = 20.sp, color = textColor)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                if (item.needBullet) {
                    Text(
                        text = "•",
                        fontSize = 18.sp,
                        modifier = Modifier.width(12.dp),
                        color = textColor
                    )
                }
                Text(
                    text = item.text,
                    modifier = Modifier.weight(1f),
                    color = textColor
                )
            }
            Divider(color = Color.Gray, thickness = 1.dp)
        }
    }
}