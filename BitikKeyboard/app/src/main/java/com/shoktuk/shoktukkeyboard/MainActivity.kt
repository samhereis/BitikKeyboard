package com.shoktuk.shoktukkeyboard

import LocalizationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.shoktuk.shoktukkeyboard.project.data.RateUsManager
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import com.whl.quickjs.android.QuickJSLoader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        LocalizationManager.init(applicationContext)

        setContent {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            ShoktukKeyboardTheme {
                SideMenuView()
            }
        }

        QuickJSLoader.init()

        // Count only real launches, not configuration-change recreations.
        if (savedInstanceState == null) {
            RateUsManager.onAppOpened(this)
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
