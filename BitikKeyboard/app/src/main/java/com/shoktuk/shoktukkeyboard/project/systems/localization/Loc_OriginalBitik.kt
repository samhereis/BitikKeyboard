package com.shoktuk.shoktukkeyboard.project.systems.localization

import android.content.Context
import localized

enum class Loc_OriginalBitik (
    val titleKey: String,
    val systemImage: String
){
    ot_consonants("ot_consonants", "keyboard"),

    ot_hardTamgas("ot_hardTamgas", "shift"),
    ot_softTamgas("ot_softTamgas", "shift"),
    ot_vowels("ot_vowels", "shift"),
    ot_characters("ot_characters", "shift"),
    noPair("noPair", "shift");

    val fileName: String
    get() = "originalTamgas"

    fun localizedTitle(context: Context): String {
        return titleKey.localized(fileName, context)
    }
}