package com.shoktuk.shoktukkeyboard.store

import LocalizationManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/*
 * The Android counterpart of the iOS `ScreenshotExporter`: every Play Store banner in one place, scroll sideways through
 * them, pick the language, and "Export all" saves each one as `Android_<lang>/Android_<lang>_<Banner>.png` (the same folders and
 * names as the iPhone / iPad banners; they all go to ~/Desktop/ShoktukKeyboardScreenshots/<Platform>/).
 *
 *  - On a device or emulator: run ScreenshotExporterActivity (debug builds), or `_Editor/Store/pull_store_screenshots.sh`
 *    after exporting to bring the files to the Desktop. The real keyboard is drawn by the app itself.
 *  - In Android Studio: the preview shows the sideways list and the controls. Previews cannot run the keyboard (no input
 *    method service exists there), so its cards are placeholders; the banners' layout, texts and tilts are real.
 */

/** The languages of the store listing: the code the banner uses, the name on the chip and the code in the file names. */
data class StoreLanguage(val id: String, val name: String, val fileCode: String)

private val storeLanguages = listOf(
    StoreLanguage("en", "English", "en"),
    StoreLanguage("ky_kiril", "Кыргызча", "ky"),
    StoreLanguage("tr", "Türkçe", "tr"),
)

/** `capture` is the view whose pixels are saved (the activity's window); null in previews, where nothing can be saved. */
@Composable
fun ScreenshotExporter(
    capture: View? = null,
    outputDir: File? = null,
    setFullScreen: (Boolean) -> Unit = {},
    /** Language ids to export on start (from a script); null to just show the exporter. */
    autoExport: List<String>? = null,
) {
    var language by remember { mutableStateOf(storeLanguages[0]) }
    var allLanguages by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf<Pair<StoreLanguage, String>?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    /** Shows each banner full size, waits until it has settled, saves the window. Suspends until all are saved. */
    suspend fun exportLanguages(chosen: List<StoreLanguage>) {
        val view = capture ?: return
        val dir = outputDir ?: return
        var saved = 0
        setFullScreen(true)
        delay(400)
        for (item in chosen) {
            for (name in storeBannerNames) {
                exporting = item to name
                delay(1500)
                // The keyboard cards draw one after the other: wait until the last picture is made.
                var waited = 0
                while (StoreKeyboardSnapshotter.busy && waited < 600) { delay(100); waited++ }
                delay(700)
                val file = File(dir, "Android_${item.fileCode}/Android_${item.fileCode}_$name.png")
                runCatching {
                    file.parentFile?.mkdirs()
                    file.outputStream().use { view.drawToBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
                    saved++
                }.onFailure { status = "Could not save $name: ${it.message}" }
            }
        }
        exporting = null
        setFullScreen(false)
        status = "Saved $saved banners to ${dir.absolutePath}\nBring them to ~/Desktop/ShoktukKeyboardScreenshots/Android: _Editor/Store/pull_store_screenshots.sh"
    }

    fun exportAll() {
        val chosen = language
        scope.launch { exportLanguages(if (allLanguages) storeLanguages else listOf(chosen)) }
    }

    // Started from a script (am start --es autoexport "en ky_kiril tr"): export without touching the screen, then say so.
    LaunchedEffect(autoExport) {
        val codes = autoExport ?: return@LaunchedEffect
        val dir = outputDir ?: return@LaunchedEffect
        File(dir, "done").delete()
        exportLanguages(storeLanguages.filter { it.id in codes })
        capture?.context?.let { StorePrefsGuard.restore(it) }  // the person's own settings are back before the script is told
        File(dir, "done").writeText("done")
    }

    val current = exporting
    if (current != null) {
        // The banner at its real size, the way the window shows it: that is what gets saved.
        key(current) {
            CompositionLocalProvider(LocalStoreLanguage provides current.first.id) { StoreBanner(current.second) }
        }
        return
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF2F2F7)).systemBarsPadding().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            storeLanguages.forEach { item ->
                FilterChip(selected = language == item, onClick = { language = item }, label = { Text(item.name) })
            }
        }

        val screen = LocalConfiguration.current
        val naturalW = screen.screenWidthDp.takeIf { it > 0 } ?: 411
        val naturalH = screen.screenHeightDp.takeIf { it > 0 } ?: 914
        val shownH = 560f
        val scale = shownH / naturalH
        // Lazy: only the banners on screen are built (each one makes its real keyboard pictures, which is slow on a slow device).
        LazyRow(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(storeBannerNames, key = { it }) { name ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        Modifier.size((naturalW * scale).dp, shownH.dp).clip(RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0x22000000), RoundedCornerShape(10.dp))
                    ) {
                        // Laid out at the real screen size, then shrunk, so it looks exactly like the exported picture.
                        key(language.id) {
                            Box(
                                Modifier.wrapContentSize(Alignment.TopStart, unbounded = true).size(naturalW.dp, naturalH.dp)
                                    .graphicsLayer { scaleX = scale; scaleY = scale; transformOrigin = TransformOrigin(0f, 0f) }
                            ) {
                                CompositionLocalProvider(LocalStoreLanguage provides language.id) { StoreBanner(name) }
                            }
                        }
                    }
                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Switch(checked = allLanguages, onCheckedChange = { allLanguages = it })
            Text("All languages", Modifier.weight(1f))
            Button(onClick = { exportAll() }, enabled = capture != null) { Text("Export all") }
        }
        Text(status ?: if (capture == null) "Run ScreenshotExporterActivity on a device to export." else "", fontSize = 11.sp, color = Color.Gray)
    }
}

/** Debug builds only: the exporter on a device or emulator, with the real keyboard. */
class ScreenshotExporterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StorePrefsGuard.begin(applicationContext)
        LocalizationManager.init(applicationContext)
        com.whl.quickjs.android.QuickJSLoader.init()
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        val auto = intent.getStringExtra("autoexport")?.split(' ', ',')?.filter { it.isNotBlank() }
        setContent {
            ScreenshotExporter(
                capture = window.decorView,
                outputDir = File(getExternalFilesDir(null), "store"),
                autoExport = auto,
                setFullScreen = { full ->
                    WindowCompat.setDecorFitsSystemWindows(window, !full)
                    if (full) controller.hide(WindowInsetsCompat.Type.systemBars()) else controller.show(WindowInsetsCompat.Type.systemBars())
                },
            )
        }
    }

    override fun onStop() {
        super.onStop()
        StorePrefsGuard.restore(applicationContext)
    }
}

@Preview(widthDp = 411, heightDp = 914, name = "Screenshot exporter")
@Composable
private fun ScreenshotExporterPreview() = ScreenshotExporter()
