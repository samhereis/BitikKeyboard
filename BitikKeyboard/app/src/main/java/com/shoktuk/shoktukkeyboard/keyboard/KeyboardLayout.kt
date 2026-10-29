package com.shoktuk.shoktukkeyboard.keyboard

data class KeyboardLayout(
    val name: String, val languageCode: String, val languageColor: String, val directionality: Int, val spaceKey: String, val rows: List<List<KeyEntry>>
) {}

data class KeyEntry(
    val name: String,
    val lowercase: String,
    val lowerCaseRomanization: String? = "",
    val lowerCaseRomanization_Alt: String? = "",
    val backgroundColorIndex_lowercase: Int? = 1,
    val lowerCaseAlternates: List<String>? = null,
    val lowerCaseAlternateHints: List<String>? = null,

    val uppercase: String? = "",
    val upperCaseRomanization: String? = "",
    val upperCaseRomanization_Alt: String? = "",
    val backgroundColorIndex_uppercase: Int? = 1,
    val upperCaseAlternates: List<String>? = null,
    val upperCaseAlternateHints: List<String>? = null,
) {
    /** Hold-and-drag list for both cases (first entry = what a plain hold-and-release types). */
    fun withAlternates(lower: List<String>): KeyEntry {
        // The key's own letter is what a tap types, so it never belongs in its alternates (same as iOS).
        val upper = lower.map { it.uppercase() }.filter { it.length == 1 }
        return copy(
            lowerCaseAlternates = lower.filter { it != lowercase },
            lowerCaseAlternateHints = null,
            upperCaseAlternates = upper.filter { it != uppercase },
            upperCaseAlternateHints = null
        )
    }
}
