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
    SavedStrings("SavedStrings");
}

data class SideMenuItem(
    val path: Loc_SideMenu, var icon: ImageVector
)
