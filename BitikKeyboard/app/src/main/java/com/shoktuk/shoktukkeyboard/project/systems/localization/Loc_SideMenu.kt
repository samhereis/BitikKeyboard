package com.shoktuk.shoktukkeyboard.project.systems.localization

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector
import localized


enum class Loc_SideMenu(val titleKey: String, val systemImage: ImageVector) {
    HOW_TO_ENABLE("sideBar_howToEbable", Icons.Filled.Settings),
    TEST_KEYBOARD("sideBar_testTheKeyboard", Icons.Filled.CheckCircle),
    BASIC_INFO("sideBar_basicInfo", Icons.Filled.Info),
    SETTINGS("sideBar_settings",  Icons.Filled.Settings),
    SUPPORT("sideBar_support", Icons.Filled.ThumbUp);

    val fileName: String
        get() = "sideBar"

    fun localizedTitle(context: Context): String {
        return titleKey.localized(fileName, context)
    }
}