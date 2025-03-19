import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.shoktuk.shoktukkeyboard.project.data.BasicInfoScreens
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NavigationItem(title = "bi_originalBitik".localized(
            "loc_basicRules",
            context = LocalContext.current
        )) {
            navController.navigate(BasicInfoScreens.ORIGINAL_TAMGAS.title)
        }
        NavigationItem(title = "Modern Bitik") {
            navController.navigate(BasicInfoScreens.MODERNIZED_TAMGAS.title)
        }
        NavigationItem(title = "Rules of Writing") {
            navController.navigate(BasicInfoScreens.RULES_OF_WRITING.title)
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun NavigationItem(title: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(10.dp))
            .background(Color.DarkGray)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 20.sp, color = Color.White)
            Image(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}