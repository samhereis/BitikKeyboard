import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoktuk.shoktukkeyboard.R
import com.shoktuk.shoktukkeyboard.project.data.NavBarPaddingSolution
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.navBarPaddingSolution
import com.shoktuk.shoktukkeyboard.project.screens.settings.CenteredDropdownPopup
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_HowToEnable
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@Composable
fun HowToEnable_Screen() {
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step 1: Activate Keyboard
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = Loc_HowToEnable.openSettingsAndEnableKeyboard.localizedTitle(context),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                    context.startActivity(intent)
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Loc_HowToEnable.openSettings.localizedTitle(context), modifier = Modifier.fillMaxWidth(), fontSize = 20.sp, textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = Loc_HowToEnable.changeLanguage.localizedTitle(context),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Image(
                painter = painterResource(id = R.drawable.hte_change_keyboard), contentDescription = "Screenshot of the keyboard", modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(25.dp))
            )
        }

        NavBarPaddingSolutionView()
    }
}

@Composable
fun NavBarPaddingSolutionView() {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(context.navBarPaddingSolution) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = Loc_HowToEnable.freeSpaceTitle.localizedTitle(context),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.secondary
            )

            CenteredDropdownPopup(
                label = Loc_HowToEnable.freeSpaceSetting.localizedTitle(context),
                options = NavBarPaddingSolution.entries,
                selected = selected,
                onSelect = { alpha ->
                    selected = alpha
                    context.navBarPaddingSolution = selected
                },
                optionLabel = { it.id },
                modifier = Modifier.fillMaxWidth()
            )

            Image(
                painter = painterResource(id = R.drawable.navbar_freespace), contentDescription = "Screenshot of the keyboard", modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(25.dp))
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HowToEnable_ScreenPreview() {
    ShoktukKeyboardTheme {
        HowToEnable_Screen()
    }
}
