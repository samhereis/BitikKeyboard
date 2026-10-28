package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.content.SharedPreferences
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.shoktuk.shoktukkeyboard.project.screens.settings.LocalInlinePickers
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.project.screens.settings.AppearanceSettingsScreen
import com.shoktuk.shoktukkeyboard.project.screens.settings.MainSettingsScreen
import com.shoktuk.shoktukkeyboard.project.screens.settings.OtherAlphabetsSettingsScreen
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_Settings
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

private const val SETTINGS_PREFS = "settings_prefs"

@Composable
fun KeyboardSettingsPanel(onClose: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE) }
    val snapshot = remember { prefs.all.toMap() }
    var version by remember { mutableIntStateOf(0) }
    var page by remember { mutableStateOf<Int?>(null) }
    var autoBitik by remember { mutableStateOf(MyKeyboardService.isAutoWriteBitikMode) }

    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> version++ }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val hasChanges = remember(version) { prefs.all != snapshot }

    fun revert() {
        prefs.edit().apply {
            clear()
            snapshot.forEach { (key, value) ->
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is String -> putString(key, value)
                    is Set<*> -> putStringSet(key, value.filterIsInstance<String>().toSet())
                }
            }
        }.apply()
    }

    ShoktukKeyboardTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (page != null) page = null else onClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (MyKeyboardService.current_writingSystem != WritingSystem.Bitik) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(Loc_Settings.autoBitik.localizedTitle(context), style = MaterialTheme.typography.titleSmall)
                                Switch(checked = autoBitik, onCheckedChange = {
                                    autoBitik = it
                                    MyKeyboardService.isAutoWriteBitikMode = it
                                    onBitikModeChanged.invoke(it)
                                })
                            }
                        }
                    }
                    IconButton(onClick = ::revert, enabled = hasChanges) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null)
                    }
                }

                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (page) {
                        0 -> MainSettingsScreen()
                        1 -> AppearanceSettingsScreen()
                        2 -> OtherAlphabetsSettingsScreen()
                        else -> Column(
                            Modifier.verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(Loc_Settings.mainSettings, Loc_Settings.appearance, Loc_Settings.otherAlphabets).forEachIndexed { index, title ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable { page = index }, shape = MaterialTheme.shapes.large, tonalElevation = 2.dp
                                ) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(title.localizedTitle(context), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

object SettingsPanelBuilder {
    fun create(service: MyKeyboardService, height: Int, onClose: () -> Unit): ComposeView {
        service.lifecycleOwner.attachToDecorView(service.window?.window?.decorView)
        return ComposeView(service).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            setContent { CompositionLocalProvider(LocalInlinePickers provides true) { KeyboardSettingsPanel(onClose) } }
        }
    }
}
