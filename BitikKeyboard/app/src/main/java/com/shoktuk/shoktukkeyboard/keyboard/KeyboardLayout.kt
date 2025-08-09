package com.shoktuk.shoktukkeyboard.keyboard

data class KeyboardLayout(
    val name: String, val languageCode: String, val languageColor: String, val directionality: Int, val spaceKey: String, val rows: List<List<KeyEntry>>
) {}

data class KeyEntry(
    val name: String,
    val lowercase: String,
    val lowerCaseRomanization: String? = "",
    val lowerCaseRomanization_Alt: String? = "",
    val lowerCaseHold: String? = "",
    val lowerCaseHoldHint: String? = "",
    val backgroundColorIndex_lowercase: Int? = 0,
    val backgroundColorIndex_lowercase_Hold: Int? = 0,

    val uppercase: String? = "",
    val upperCaseRomanization: String? = "",
    val upperCaseRomanization_Alt: String? = "",
    val upperCaseHold: String? = "",
    val upperCaseHoldHint: String? = "",
    val backgroundColorIndex_uppercase: Int? = 0,
    val backgroundColorIndex_uppercase_Hold: Int? = 0
)
