package com.shoktuk.shoktukkeyboard.keyboard.layouts

import com.shoktuk.shoktukkeyboard.keyboard.KeyEntry
import com.shoktuk.shoktukkeyboard.keyboard.KeyboardLayout

/** Latin (minimal) layout, mirrors iOS KeyboardLetters_Latin. */
object KeyboardLetters_Latin {
    val row1: List<KeyEntry> = listOf(
        KeyEntry(
            name = "row1_0",
            lowercase = "q",
            uppercase = "Q"
        ),
        KeyEntry(
            name = "row1_1",
            lowercase = "ş",
            lowerCaseRomanization = "w",
            lowerCaseAlternates = listOf("w", "š"),
            uppercase = "Ş",
            upperCaseRomanization = "W",
            upperCaseAlternates = listOf("W", "Š")
        ),
        KeyEntry(
            name = "row1_2",
            lowercase = "e",
            lowerCaseRomanization = "ə",
            backgroundColorIndex_lowercase = 4,
            lowerCaseAlternates = listOf("ə", "é", "è", "ê", "ë", "ē", "ė", "ę"),
            uppercase = "E",
            upperCaseRomanization = "Ə",
            backgroundColorIndex_uppercase = 4,
            upperCaseAlternates = listOf("Ə", "É", "È", "Ê", "Ë", "Ē", "Ė", "Ę")
        ),
        KeyEntry(
            name = "row1_3",
            lowercase = "r",
            lowerCaseAlternates = listOf("ř"),
            uppercase = "R",
            upperCaseAlternates = listOf("Ř")
        ),
        KeyEntry(
            name = "row1_4",
            lowercase = "t",
            lowerCaseAlternates = listOf("ť", "þ"),
            uppercase = "T",
            upperCaseAlternates = listOf("Ť", "Þ")
        ),
        KeyEntry(
            name = "row1_5",
            lowercase = "y",
            lowerCaseAlternates = listOf("ý", "ÿ"),
            uppercase = "Y",
            upperCaseAlternates = listOf("Ý", "Ÿ")
        ),
        KeyEntry(
            name = "row1_6",
            lowercase = "u",
            backgroundColorIndex_lowercase = 4,
            lowerCaseAlternates = listOf("ū", "ú", "ù", "û", "ů"),
            uppercase = "U",
            backgroundColorIndex_uppercase = 4,
            upperCaseAlternates = listOf("Ū", "Ú", "Ù", "Û", "Ů")
        ),
        KeyEntry(
            name = "row1_7",
            lowercase = "i",
            backgroundColorIndex_lowercase = 4,
            lowerCaseAlternates = listOf("í", "ì", "î", "ï", "ī", "į"),
            uppercase = "İ",
            backgroundColorIndex_uppercase = 4,
            upperCaseAlternates = listOf("Í", "Ì", "Î", "Ï", "Ī", "Į")
        ),
        KeyEntry(
            name = "row1_8",
            lowercase = "o",
            backgroundColorIndex_lowercase = 4,
            lowerCaseAlternates = listOf("ó", "ò", "ô", "õ", "ø", "ō", "œ"),
            uppercase = "O",
            backgroundColorIndex_uppercase = 4,
            upperCaseAlternates = listOf("Ó", "Ò", "Ô", "Õ", "Ø", "Ō", "Œ")
        ),
        KeyEntry(
            name = "row1_9",
            lowercase = "p",
            uppercase = "P"
        )
    )

    val row2: List<KeyEntry> = listOf(
        KeyEntry(
            name = "row2_0",
            lowercase = "a",
            backgroundColorIndex_lowercase = 4,
            lowerCaseAlternates = listOf("ä", "á", "à", "â", "ã", "å", "ā", "æ"),
            uppercase = "A",
            backgroundColorIndex_uppercase = 4,
            upperCaseAlternates = listOf("Ä", "Á", "À", "Â", "Ã", "Å", "Ā", "Æ")
        ),
        KeyEntry(
            name = "row2_1",
            lowercase = "s",
            lowerCaseAlternates = listOf("ś", "ß"),
            uppercase = "S",
            upperCaseAlternates = listOf("Ś", "ẞ")
        ),
        KeyEntry(
            name = "row2_2",
            lowercase = "d",
            lowerCaseAlternates = listOf("ď", "ð"),
            uppercase = "D",
            upperCaseAlternates = listOf("Ď", "Ð")
        ),
        KeyEntry(
            name = "row2_3",
            lowercase = "ü",
            lowerCaseRomanization = "f",
            backgroundColorIndex_lowercase = 4,
            lowerCaseAlternates = listOf("f"),
            uppercase = "Ü",
            upperCaseRomanization = "F",
            backgroundColorIndex_uppercase = 4,
            upperCaseAlternates = listOf("F")
        ),
        KeyEntry(
            name = "row2_4",
            lowercase = "g",
            lowerCaseAlternates = listOf("ĝ", "ġ", "ģ", "ǧ", "ǵ"),
            uppercase = "G",
            upperCaseAlternates = listOf("Ĝ", "Ġ", "Ģ", "Ǧ", "Ǵ")
        ),
        KeyEntry(
            name = "row2_5",
            lowercase = "ö",
            lowerCaseRomanization = "h",
            backgroundColorIndex_lowercase = 4,
            lowerCaseAlternates = listOf("h", "ө"),
            uppercase = "Ö",
            upperCaseRomanization = "H",
            backgroundColorIndex_uppercase = 4,
            upperCaseAlternates = listOf("H", "Ө")
        ),
        KeyEntry(
            name = "row2_6",
            lowercase = "j",
            uppercase = "J"
        ),
        KeyEntry(
            name = "row2_7",
            lowercase = "k",
            uppercase = "K"
        ),
        KeyEntry(
            name = "row2_8",
            lowercase = "l",
            lowerCaseAlternates = listOf("ł", "ľ"),
            uppercase = "L",
            upperCaseAlternates = listOf("Ł", "Ľ")
        ),
        KeyEntry(
            name = "row2_9",
            lowercase = "ğ",
            lowerCaseAlternates = listOf("ƣ", "ɣ"),
            uppercase = "Ğ",
            upperCaseAlternates = listOf("Ƣ", "Ɣ")
        )
    )

    val row3: List<KeyEntry> = listOf(
        KeyEntry(
            name = "row3_0",
            lowercase = "z",
            lowerCaseAlternates = listOf("ž", "ź", "ż"),
            uppercase = "Z",
            upperCaseAlternates = listOf("Ž", "Ź", "Ż")
        ),
        KeyEntry(
            name = "row3_1",
            lowercase = "ñ",
            lowerCaseRomanization = "x",
            lowerCaseAlternates = listOf("x", "ŋ"),
            uppercase = "Ñ",
            upperCaseRomanization = "X",
            upperCaseAlternates = listOf("X", "Ŋ")
        ),
        KeyEntry(
            name = "row3_2",
            lowercase = "ç",
            lowerCaseRomanization = "c",
            lowerCaseAlternates = listOf("c"),
            uppercase = "Ç",
            upperCaseRomanization = "C",
            upperCaseAlternates = listOf("C")
        ),
        KeyEntry(
            name = "row3_3",
            lowercase = "ı",
            lowerCaseRomanization = "v",
            backgroundColorIndex_lowercase = 4,
            lowerCaseAlternates = listOf("v"),
            uppercase = "I",
            upperCaseRomanization = "V",
            backgroundColorIndex_uppercase = 4,
            upperCaseAlternates = listOf("V")
        ),
        KeyEntry(
            name = "row3_4",
            lowercase = "b",
            uppercase = "B"
        ),
        KeyEntry(
            name = "row3_5",
            lowercase = "n",
            lowerCaseAlternates = listOf("ń", "ň"),
            uppercase = "N",
            upperCaseAlternates = listOf("Ń", "Ň")
        ),
        KeyEntry(
            name = "row3_6",
            lowercase = "m",
            uppercase = "M"
        )
    )

    fun layout(): KeyboardLayout = KeyboardLayout(
        "latin", "𐱄", "#800d1a", 1, ":",
        listOf(row1, row2, listOf(KeyEntry(name = "Shift", lowercase = "")) + row3 + listOf(KeyEntry(name = "Del", lowercase = "")))
    )
}
