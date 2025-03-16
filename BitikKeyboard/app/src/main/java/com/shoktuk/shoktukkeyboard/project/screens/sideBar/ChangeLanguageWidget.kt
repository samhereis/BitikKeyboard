import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.R

// --- Enum for Languages ---
enum class Language(val value: String) {
    kyrgyz("kyrgyz"), kazakh("kazakh"), turkish("turkish"), english("english"), russian("russian")
}

// --- LanguageSelection Class ---
// This class reads/writes the current language to SharedPreferences.
class LanguageSelection(context: Context) {
    private val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    // Use Compose mutableState for reactivity.
    var currentLanguage by mutableStateOf(Language.kyrgyz)
        private set

    init {
        val savedLanguage = prefs.getString("AppLanguage", null)
        currentLanguage = Language.values().find { it.value == savedLanguage } ?: Language.kyrgyz
    }

    fun setLanguage(language: Language) {
        currentLanguage = language
        prefs.edit().putString("AppLanguage", language.value).apply()
        updateLocalization()
    }

    fun updateLocalization() {
        println("Language changed to: ${currentLanguage.value}")
        // Additional actions (e.g., notifications) can be added here.
    }
}

// --- Data class for flag items ---
data class FlagItem(val imageName: Painter, val language: Language)

// --- ChangeLanguageWidget Composable ---
// This composable displays the flags in two rows.
@Composable
fun ChangeLanguageWidget(languageSelection: LanguageSelection) {
    // Define flag items (ensure your drawable resources match these names).
    val flags = listOf(
        FlagItem(painterResource(id = R.drawable.flag_kg), Language.kyrgyz),
        FlagItem(painterResource(id = R.drawable.flag_kz), Language.kazakh),
        FlagItem(painterResource(id = R.drawable.flag_tr), Language.turkish),
        FlagItem(painterResource(id = R.drawable.flag_usa), Language.english),
        FlagItem(painterResource(id = R.drawable.flag_ru), Language.russian)
    )

    Surface(
        modifier = Modifier.padding(16.dp), shape = RoundedCornerShape(12.dp), color = Color.Transparent, border = BorderStroke(2.dp, Color.Gray)
    ) {
        // Column that centers its children horizontally
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top row: first 3 flags, spaced by 16.dp, automatically centered by the Column
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                flags.take(3).forEach { flag ->
                    FlagButton(flag) {
                        languageSelection.updateLocalization()
                    }
                }
            }
            // Bottom row: remaining flags
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                flags.drop(3).forEach { flag ->
                    FlagButton(flag) {
                        languageSelection.updateLocalization()
                    }
                }
            }
        }
    }
}

@Composable
fun FlagButton(
    flag: FlagItem, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 80.dp, height = 54.dp)
            .clickable { onClick() }, contentAlignment = Alignment.Center
    ) {
        Image(
            painter = flag.imageName, contentDescription = flag.language.name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun getImageResId(imageName: String): Int {
    return when (imageName) {
        "flag_kg" -> R.drawable.flag_kg
        "flag_kz" -> R.drawable.flag_kz
        "flag_tr" -> R.drawable.flag_tr
        "flag_usa" -> R.drawable.flag_usa
        "flag_ru" -> R.drawable.flag_ru
        else -> R.drawable.ic_launcher_foreground
    }
}


@Preview(showBackground = true)
@Composable
fun ChangeLanguageWidgetPreview() {
    val context = LocalContext.current
    // Create a LanguageSelection instance using the current context.
    val languageSelection = remember { LanguageSelection(context) }
    ChangeLanguageWidget(languageSelection = languageSelection)
}
