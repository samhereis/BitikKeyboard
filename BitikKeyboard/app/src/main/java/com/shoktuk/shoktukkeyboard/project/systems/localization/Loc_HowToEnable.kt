package com.shoktuk.shoktukkeyboard.project.systems.localization

import android.content.Context
import localized

enum class Loc_HowToEnable (
    val titleKey: String,
    val systemImage: String
){
    howToEnable("howToEnable", "keyboard"),

    openSettingsAndEnableKeyboard("openSettingsAndEnableKeyboard", "shift"),
    openSettings("openSettings", "shift"),
    changeLanguage("changeLanguage", "shift"),
    freeSpaceTitle("freeSpaceTitle", "shift"),
    freeSpaceSetting("freeSpaceSetting", "shift");

    val fileName: String
    get() = "hte_android"

    fun localizedTitle(context: Context): String {
        return titleKey.localized(fileName, context)
    }
}