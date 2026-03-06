import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.shoktuk.shoktukkeyboard.R
import com.shoktuk.shoktukkeyboard.project.screens.navigation.ExternalLink_Button
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_BasicInfo
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@Preview(showBackground = true)
@Composable
fun BasicInfo_ScreenPreview() {
    val navController = rememberNavController()

    ShoktukKeyboardTheme {
        BasicInfo_Screen(navController)
    }
}

@Composable
fun BasicInfo_Screen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BeautifulNavigationItem(
            title = Loc_BasicInfo.USING_THE_KEYBOARD.localizedTitle(context), onClick = {
                navController.navigate(Loc_BasicInfo.USING_THE_KEYBOARD.titleKey)
            }, subtitle = null, leading = Icons.Default.Info, tint = MaterialTheme.colorScheme.primary
        )

        BeautifulNavigationItem(
            title = Loc_BasicInfo.ORIGINAL_BITIK.localizedTitle(context), onClick = {
                navController.navigate(Loc_BasicInfo.ORIGINAL_BITIK.titleKey)
            }, subtitle = null, leading = Icons.Default.Favorite, tint = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()

        Text(Loc_BasicInfo.BITIK_RULES.localizedTitle(LocalContext.current))
        BeautifulNavigationItem(
            title = Loc_BasicInfo.RULE1.localizedTitle(context), onClick = {
                navController.navigate(Loc_BasicInfo.RULE1.titleKey)
            }, subtitle = null, leading = Icons.Default.Star, tint = MaterialTheme.colorScheme.primary
        )

        BeautifulNavigationItem(
            title = Loc_BasicInfo.RULE2.localizedTitle(context), onClick = {
                navController.navigate(Loc_BasicInfo.RULE2.titleKey)
            }, subtitle = null, leading = Icons.Default.Star, tint = MaterialTheme.colorScheme.primary
        )

        BeautifulNavigationItem(
            title = Loc_BasicInfo.RULE3.localizedTitle(context), onClick = {
                navController.navigate(Loc_BasicInfo.RULE3.titleKey)
            }, subtitle = null, leading = Icons.Default.Star, tint = MaterialTheme.colorScheme.primary
        )

        BeautifulNavigationItem(
            title = Loc_BasicInfo.RULE4.localizedTitle(context), onClick = {
                navController.navigate(Loc_BasicInfo.RULE4.titleKey)
            }, subtitle = null, leading = Icons.Default.Star, tint = MaterialTheme.colorScheme.primary
        )

        HorizontalDivider()

        Text(Loc_BasicInfo.MEMORIZE_TAMGAS.localizedTitle(LocalContext.current))
        BeautifulNavigationItem(
            title = Loc_BasicInfo.MEMORIZE_TAMGAS.localizedTitle(context), onClick = {
                navController.navigate(Loc_BasicInfo.MEMORIZE_TAMGAS.titleKey)
            }, subtitle = null, leading = Icons.Default.LocalLibrary, tint = MaterialTheme.colorScheme.primary
        )

        ExternalLink_Button(
            label = "Bitish 🚀", description = "Битик үйрөнүү тиркемеси! 🥳", leadingPainter = painterResource(id = R.drawable.bitish_icon), onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.shoktuk.learnbitik"))
                context.startActivity(intent)
            })

        ExternalLink_Button(
            label = "Türk tili    𐱅𐰈𐰼𐰰 : 𐱅𐰃𐰠𐰃", description = "https://turk-tili.com", leadingPainter = painterResource(id = R.drawable.turktili_icon), onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://turk-tili.com"))
                context.startActivity(intent)
            })
    }
}

@Composable
fun BeautifulNavigationItem(
    title: String, onClick: () -> Unit, subtitle: String? = null, leading: ImageVector? = null, tint: Color = MaterialTheme.colorScheme.primary, modifier: Modifier = Modifier
) {
    val bg = MaterialTheme.colorScheme.surface
    val glass = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    val stroke = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val contentColor = MaterialTheme.colorScheme.onSurface

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "press-scale")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(glass, bg.copy(alpha = 0.65f))
                )
            )
            .border(1.dp, stroke, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null, // looks cleaner; rely on scale
                onClick = onClick, onClickLabel = "Open"
            )
            .padding(horizontal = 5.dp, vertical = 5.dp)
            .then(
                Modifier.pointerInput(Unit) {
                    // simple press feedback
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            pressed = event.buttons.isPrimaryPressed
                        }
                    }
                }), verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(tint.copy(alpha = 0.35f), tint.copy(alpha = 0.12f))
                        )
                    )
                    .border(1.dp, tint.copy(alpha = 0.25f), CircleShape), contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = leading, contentDescription = null, colorFilter = ColorFilter.tint(tint)
                )
            }
            Spacer(Modifier.width(12.dp))
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = title, color = contentColor, fontSize = 18.sp
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // subtle chevron chip
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tint.copy(alpha = 0.08f))
                .border(1.dp, tint.copy(alpha = 0.22f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, colorFilter = ColorFilter.tint(tint)
            )
        }
    }
}