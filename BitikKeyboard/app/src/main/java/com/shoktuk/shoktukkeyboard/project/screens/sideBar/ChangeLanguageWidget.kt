import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.R

data class FlagItem(val imageName: Painter, val language: Language)

@Composable
fun FlagButton(
    flag: FlagItem, isSelected: Boolean, onClick: () -> Unit
) {
    // Animate the scale if selected (visual "bigger" effect)
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 1f)

    Box(modifier = Modifier
        .size(width = 80.dp, height = 54.dp)
        .clickable { onClick() }
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .then(
            if (isSelected) Modifier.border(BorderStroke(2.dp, Color.Blue), shape = RoundedCornerShape(4.dp))
            else Modifier
        ), contentAlignment = Alignment.Center) {
        Image(
            painter = flag.imageName, contentDescription = flag.language.name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ChangeLanguageWidget() {
    val context = LocalContext.current
    val currentLanguage = LocalizationManager.currentLanguage
    val flags = listOf(
        FlagItem(painterResource(id = R.drawable.flag_kg), Language.KY),
        FlagItem(painterResource(id = R.drawable.flag_kz), Language.KZ),
        FlagItem(painterResource(id = R.drawable.flag_tr), Language.TR),
        FlagItem(painterResource(id = R.drawable.flag_usa), Language.EN),
        FlagItem(painterResource(id = R.drawable.flag_ru), Language.RU)
    )

    Surface(
        modifier = Modifier.padding(16.dp), shape = RoundedCornerShape(12.dp), color = Color.Transparent, border = BorderStroke(2.dp, Color.Gray)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)) {
                flags.take(3).forEach { flag ->
                    FlagButton(
                        flag = flag, isSelected = flag.language == currentLanguage
                    ) {
                        LocalizationManager.setLanguage(context, flag.language)
                    }
                }
            }
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)) {
                flags.drop(3).forEach { flag ->
                    FlagButton(
                        flag = flag, isSelected = flag.language == currentLanguage
                    ) {
                        LocalizationManager.setLanguage(context, flag.language)
                    }
                }
            }
        }
    }
}
