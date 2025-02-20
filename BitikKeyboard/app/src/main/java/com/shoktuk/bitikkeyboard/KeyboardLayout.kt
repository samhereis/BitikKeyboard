package com.shoktuk.bitikkeyboard

data class KeyboardLayout(
        val name: String,          // "letters" or "symbols"
        val directionality: Int,   // 0 = LTR, 1 = RTL
        val spaceKey: String,
        val rows: List<List<KeyEntry>>
)

data class KeyEntry(
        val name: String,
        val lowercase: String,
        val uppercase: String? = null,
        val hold: String? = null,
        // Optional romanization hints for subtext
        val lowerCaseHint: String? = null,
        val upperCaseHint: String? = null
)
