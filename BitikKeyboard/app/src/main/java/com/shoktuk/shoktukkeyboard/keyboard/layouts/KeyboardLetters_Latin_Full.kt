package com.shoktuk.shoktukkeyboard.keyboard.layouts

import com.shoktuk.shoktukkeyboard.keyboard.KeyEntry
import com.shoktuk.shoktukkeyboard.keyboard.KeyboardLayout

/** Latin (full) layout, mirrors iOS KeyboardLetters_Latin_Full. */
object KeyboardLetters_Latin_Full {
    val row1: List<KeyEntry> = listOf(
        KeyEntry(
            name = "row1_0",
            lowercase = "q",
            uppercase = "Q"
        ),
        KeyEntry(
            name = "row1_1",
            lowercase = "w",
            uppercase = "W"
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
        ),
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
            name = "row2_4",
            lowercase = "g",
            lowerCaseAlternates = listOf("ĝ", "ġ", "ģ", "ǧ", "ǵ"),
            uppercase = "G",
            upperCaseAlternates = listOf("Ĝ", "Ġ", "Ģ", "Ǧ", "Ǵ")
        ),
        KeyEntry(
            name = "row2_5",
            lowercase = "h",
            uppercase = "H"
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
            lowercase = "ş",
            lowerCaseAlternates = listOf("š"),
            uppercase = "Ş",
            upperCaseAlternates = listOf("Š")
        ),
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
        ),
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
    )

    fun layout(): KeyboardLayout = KeyboardLayout(
        "latin_full", "𐱄", "#800d1a", 1, ":",
        listOf(row1, row2, listOf(KeyEntry(name = "Shift", lowercase = "")) + row3 + listOf(KeyEntry(name = "Del", lowercase = "")))
    )
}
