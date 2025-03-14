import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import com.shoktuk.shoktukkeyboard.R

@Composable
fun SideMenuHeader(modifier: Modifier) {
    Row(
        modifier = modifier, verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp)) // This clips the content with rounded corners
                .background(color = Color.Blue)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.shoktuk_icon), contentDescription = "User Icon", modifier = Modifier.size(48.dp), // Match the Box size for a neat fit
                tint = Color.Unspecified // Display the image's original colors
            )
        }

        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = "Shoktuk Keyboard"
            )
            Text(
                text = "by Samagan Davlatbek uulu", color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SideMenuHeaderPreview() {
    ShoktukKeyboardTheme {
        SideMenuHeader(Modifier.padding(vertical = 16.dp, horizontal = 16.dp))
    }
}
