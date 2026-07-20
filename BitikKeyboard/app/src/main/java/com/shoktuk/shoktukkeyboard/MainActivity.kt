package com.shoktuk.shoktukkeyboard

import LocalizationManager
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.shoktuk.shoktukkeyboard.project.data.CLOUD_SYNC_ENABLED
import com.shoktuk.shoktukkeyboard.project.data.DriveClient
import com.shoktuk.shoktukkeyboard.project.data.RateUsManager
import com.shoktuk.shoktukkeyboard.project.data.SettingsCloudSync
import com.shoktuk.shoktukkeyboard.project.data.cloudSyncedKeys
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import com.whl.quickjs.android.QuickJSLoader
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var settingsChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (CLOUD_SYNC_ENABLED) {
            DriveClient.registerLauncher(this)
            registerSettingsChangeListener()
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        LocalizationManager.init(applicationContext)

        setContent {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            ShoktukKeyboardTheme {
                SideMenuView()
            }
        }

        QuickJSLoader.init()

        if (savedInstanceState == null) {
            RateUsManager.onAppOpened(this)
        }

        if (CLOUD_SYNC_ENABLED) {
            lifecycleScope.launch {
                SettingsCloudSync.bootstrapIfNeeded(this@MainActivity)
                SettingsCloudSync.pull(this@MainActivity)
            }
        }
    }

    private fun registerSettingsChangeListener() {
        val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == null || key !in cloudSyncedKeys) return@OnSharedPreferenceChangeListener
            lifecycleScope.launch {
                SettingsCloudSync.onLocalChange(this@MainActivity, key)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        settingsChangeListener = listener
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    ShoktukKeyboardTheme {
        SideMenuView()
    }
}
