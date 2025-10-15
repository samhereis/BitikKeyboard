import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.R
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@Composable
fun SideMenuHeader(
    modifier: Modifier = Modifier, website: String = "http://shoktuk.com", onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(role = Role.Button, onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(website))
                context.startActivity(intent)
            })
            .padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.shoktuk_icon), contentDescription = "Shoktuk Keyboard", modifier = Modifier.size(50.dp), tint = Color.Unspecified
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = "Şoqtuq Bitik", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "by Samagan Davlatbek uulu", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                text = website, style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline
                ), maxLines = 1, overflow = TextOverflow.Ellipsis
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
