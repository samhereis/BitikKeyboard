package com.shoktuk.shoktukkeyboard.keyboard

data class KeyboardLayout(
    val name: String,
    val languageCode: String,
    val languageColor: String,
    val directionality: Int,
    val spaceKey: String,
    val rows: List<List<KeyEntry>>
) {
}

data class KeyEntry(
    val name: String,
    val lowercase: String,
    val uppercase: String? = null,
    val lowercaseHold: String? = null,
    val uppercaseHold: String? = null,
    val lowerCaseHint: String? = null,
    val upperCaseHint: String? = null,
    val lowerCaseColor: Int? = null,
    val upperCaseColor: Int? = null
)
