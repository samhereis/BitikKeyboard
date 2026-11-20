package com.shoktuk.shoktukkeyboard.store

import LocalizationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Shows one Play Store banner full screen (no system bars), for `_Editor/Store/make_store_screenshots.sh` to take a picture of:
 * `am start -n <pkg>/.store.StoreBannerActivity --es banner AndMore --es lang en`. Debug builds only.
 */
class StoreBannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StorePrefsGuard.begin(applicationContext)
        LocalizationManager.init(applicationContext)
        com.whl.quickjs.android.QuickJSLoader.init()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        val banner = intent.getStringExtra("banner") ?: "AndMore"
        val language = intent.getStringExtra("lang") ?: "en"
        setContent {
            CompositionLocalProvider(LocalStoreLanguage provides language) { StoreBanner(banner) }
        }
    }

    override fun onStop() {
        super.onStop()
        StorePrefsGuard.restore(applicationContext)
    }
}
