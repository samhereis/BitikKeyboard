package com.shoktuk.shoktukkeyboard.keyboard

data class KeyboardLayout(
    val name: String, val languageCode: String, val languageColor: String, val directionality: Int, val spaceKey: String, val rows: List<List<KeyEntry>>
) {}

data class KeyEntry(
    val name: String,
    val lowercase: String,
    val lowerCaseRomanization: String? = null,
    val lowerCaseRomanization_Alt: String? = null,
    val lowerCaseHold: String? = null,
    val lowerCaseHoldHint: String? = null,
    val backgroundColorIndex_lowercase: Int? = 0,
    val backgroundColorIndex_lowercase_Hold: Int? = 0,

    val uppercase: String? = null,
    val upperCaseRomanization: String? = null,
    val upperCaseRomanization_Alt: String? = null,
    val upperCaseHold: String? = null,
    val upperCaseHoldHint: String? = null,
    val backgroundColorIndex_uppercase: Int? = 0,
    val backgroundColorIndex_uppercase_Hold: Int? = 0
)
