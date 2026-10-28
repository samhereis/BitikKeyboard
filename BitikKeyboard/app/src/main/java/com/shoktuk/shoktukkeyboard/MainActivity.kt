package com.shoktuk.shoktukkeyboard

import LocalizationManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.shoktuk.shoktukkeyboard.project.data.CLOUD_SYNC_ENABLED
import com.shoktuk.shoktukkeyboard.project.data.DriveClient
import com.shoktuk.shoktukkeyboard.project.data.RateUsManager
import com.shoktuk.shoktukkeyboard.project.data.SettingsCloudSync
import com.shoktuk.shoktukkeyboard.project.data.cloudSyncedKeys
import Language
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import com.whl.quickjs.android.QuickJSLoader
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var settingsChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private var requestedDestination by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedDestination = intent?.getStringExtra(EXTRA_DESTINATION)
        applyScreenshotLanguage()

        if (CLOUD_SYNC_ENABLED) {
            DriveClient.registerLauncher(this)
            registerSettingsChangeListener()
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        LocalizationManager.init(applicationContext)

        setContent {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            ShoktukKeyboardTheme {
                SideMenuView(
                    requestedDestination = requestedDestination,
                    onDestinationHandled = { requestedDestination = null }
                )
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedDestination = intent.getStringExtra(EXTRA_DESTINATION)
        applyScreenshotLanguage()
    }

    private fun applyScreenshotLanguage() {
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) return
        val code = intent?.getStringExtra(EXTRA_SCREENSHOT_LANGUAGE) ?: return
        Language.entries.find { it.code == code }?.let { LocalizationManager.setLanguage(this, it) }
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

    companion object {
        const val EXTRA_DESTINATION = "destination"
        const val EXTRA_SCREENSHOT_LANGUAGE = "screenshot_language"
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    ShoktukKeyboardTheme {
        SideMenuView()
    }
}
