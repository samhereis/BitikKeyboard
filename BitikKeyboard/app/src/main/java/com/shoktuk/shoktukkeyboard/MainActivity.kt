package com.shoktuk.shoktukkeyboard

import LocalizationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        LocalizationManager.init(applicationContext)

        setContent {
            ShoktukKeyboardTheme {
                SideMenuView()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    ShoktukKeyboardTheme {
        SideMenuView()
    }
}
