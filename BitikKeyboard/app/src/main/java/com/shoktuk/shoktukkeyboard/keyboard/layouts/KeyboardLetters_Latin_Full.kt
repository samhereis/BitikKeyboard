package com.shoktuk.shoktukkeyboard.keyboard.layouts

import com.shoktuk.shoktukkeyboard.keyboard.KeyEntry
import com.shoktuk.shoktukkeyboard.keyboard.KeyboardLayout

/**
 * Latin (full) layout, mirrors iOS KeyboardLetters_Latin_Full. Built from the minimal Latin layout: [overrides] replace
 * keys of the same name, [extras] are appended to the end of their row.
 */
object KeyboardLetters_Latin_Full {
    private val overrides: Map<String, KeyEntry> = listOf(
            KeyEntry(
                name = "row1_1",
                lowercase = "w",
                uppercase = "W"
            ),
            KeyEntry(
                name = "row2_2",
                lowercase = "d",
                lowerCaseAlternates = listOf("ð"),
                uppercase = "D",
                upperCaseAlternates = listOf("Ð")
            ),
            KeyEntry(
                name = "row2_3",
                lowercase = "f",
                uppercase = "F"
            ),
            KeyEntry(
                name = "row2_5",
                lowercase = "h",
                uppercase = "H"
            ),
            KeyEntry(
                name = "row2_9",
                lowercase = "ş",
                lowerCaseAlternates = listOf("š"),
                uppercase = "Ş",
                upperCaseAlternates = listOf("Š")
            ),
            KeyEntry(
                name = "row3_1",
                lowercase = "x",
                uppercase = "X"
            ),
            KeyEntry(
                name = "row3_2",
                lowercase = "c",
                lowerCaseAlternates = listOf("ć", "č", "ĉ", "ċ"),
                uppercase = "C",
                upperCaseAlternates = listOf("Ć", "Č", "Ĉ", "Ċ")
            ),
            KeyEntry(
                name = "row3_3",
                lowercase = "v",
                uppercase = "V",
                backgroundColorIndex_uppercase = 4
            )
    ).associateBy { it.name }

    private fun row(base: List<KeyEntry>, extras: List<KeyEntry>): List<KeyEntry> =
        base.map { overrides[it.name] ?: it } + extras

    val row1: List<KeyEntry> = row(KeyboardLetters_Latin.row1, listOf(
            KeyEntry(
                name = "row1_10",
                lowercase = "ğ",
                lowerCaseAlternates = listOf("ƣ", "ɣ"),
                uppercase = "Ğ",
                upperCaseAlternates = listOf("Ƣ", "Ɣ")
            ),
            KeyEntry(
                name = "row1_11",
                lowercase = "ü",
                backgroundColorIndex_lowercase = 4,
                uppercase = "Ü",
                backgroundColorIndex_uppercase = 4
            )
    ))

    val row2: List<KeyEntry> = row(KeyboardLetters_Latin.row2, listOf(
            KeyEntry(
                name = "row2_10",
                lowercase = "ı",
                backgroundColorIndex_lowercase = 4,
                uppercase = "I",
                backgroundColorIndex_uppercase = 4
            ),
            KeyEntry(
                name = "row2_11",
                lowercase = "ñ",
                lowerCaseAlternates = listOf("ŋ"),
                uppercase = "Ñ",
                upperCaseAlternates = listOf("Ŋ")
            )
    ))

    val row3: List<KeyEntry> = row(KeyboardLetters_Latin.row3, listOf(
            KeyEntry(
                name = "row3_7",
                lowercase = "ö",
                backgroundColorIndex_lowercase = 4,
                lowerCaseAlternates = listOf("ө"),
                uppercase = "Ö",
                backgroundColorIndex_uppercase = 4,
                upperCaseAlternates = listOf("Ө")
            ),
            KeyEntry(
                name = "row3_8",
                lowercase = "ç",
                uppercase = "Ç"
            )
    ))

    fun layout(): KeyboardLayout = KeyboardLayout(
        "latin_full", "𐱄", "#800d1a", 1, ":",
        listOf(row1, row2, listOf(KeyEntry(name = "Shift", lowercase = "")) + row3 + listOf(KeyEntry(name = "Del", lowercase = "")))
    )
}
