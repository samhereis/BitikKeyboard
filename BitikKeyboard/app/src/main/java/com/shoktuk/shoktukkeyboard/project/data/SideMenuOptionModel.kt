package com.shoktuk.shoktukkeyboard.project.data

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_SideMenu
import localized

enum class SettingScreens(val id: String) {
    SavedStrings("savables"),
    // ids double as localization keys (see the top-bar title lookup), so they must
    // match the CSV keys in settings.csv.
    MainSettings("mainSettings"),
    Appearance("appearance"),
    OtherAlphabets("otherAlphabets"),
    Other("other");
}

data class SideMenuItem(
    val path: Loc_SideMenu, var icon: ImageVector
)
