package com.shoktuk.shoktukkeyboard.project.systems.localization

import android.content.Context
import localized

enum class Loc_BitikRules(
    val titleKey: String,
    val systemImage: String
) {
    R_1_RIGHT_TO_LEFT("r_1_rightToLeft", "keyboard"),

    R_2_HARD_SOFT("r_2_hardSoft", "shift"),
    R_2_HARD_VOWEL("r_2_hardVowel", "shift"),
    R_2_SOFT_VOWEL("r_2_softVowel", "shift"),
    R_2_SOFT_WITH_SOFT_HARD_WITH_HARD("r_2_softWithSoftHardWIthHard", "shift"),

    R_3_SHORT_WRITING("r_3_shortWriting", "shift"),
    R_3_AUTHOR_SUGGESTIONS("r_3_authorSuggestions", "shift"),
    R_3_SPECIAL_LETTERS("r_3_specialLetters", "shift"),
    R_3_SPECIAL_LETTERS_DETAILS("r_3_specialLetters_Details", "shift"),
    R_3_SINGARMONISM("r_3_singarmonism", "shift"),
    R_3_SINGARMONISM_DETAIL("r_3_singarmonism_Detail", "shift"),
    R_3_SINGARMONISM_JIRAQ("r_3_singarmonism_Jiraq", "shift"),

    R_4_DOUBLE_WORDS("r_4_doubleWords", "shift"),
    R_4_DOUBLE_WORDS_2("r_4_doubleWords_2", "shift");

    val fileName: String
        get() = "bitikRules"

    fun localizedTitle(context: Context): String {
        return titleKey.localized(fileName, context)
    }
}