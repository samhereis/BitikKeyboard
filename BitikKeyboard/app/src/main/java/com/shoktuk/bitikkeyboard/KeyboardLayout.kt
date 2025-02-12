package com.shoktuk.bitikkeyboard

data class KeyboardLayout(
        val spaceKey: String, var directionality: Int, val rows: List<List<KeyEntry>>
)
