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

        }
    }
}
