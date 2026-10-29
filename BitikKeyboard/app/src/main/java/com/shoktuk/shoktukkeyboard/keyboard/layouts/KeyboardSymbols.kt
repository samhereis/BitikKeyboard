package com.shoktuk.shoktukkeyboard.keyboard.layouts

import com.shoktuk.shoktukkeyboard.keyboard.KeyEntry
import com.shoktuk.shoktukkeyboard.keyboard.KeyboardLayout

/** Symbols screen, mirrors iOS KeyboardSymbols (the free key is added in front of row 3). */
object KeyboardSymbols {
    val row1: List<KeyEntry> = listOf(
        KeyEntry(
            name = "row1_0",
            lowercase = "1",
            lowerCaseRomanization = "|",
            backgroundColorIndex_lowercase = 5,
            lowerCaseAlternates = listOf("|"),
            uppercase = "1",
            upperCaseRomanization = "|",
            backgroundColorIndex_uppercase = 5,
            upperCaseAlternates = listOf("|")
        ),
        KeyEntry(
            name = "row1_1",
            lowercase = "2",
            lowerCaseRomanization = "#",
            backgroundColorIndex_lowercase = 5,
            lowerCaseAlternates = listOf("#"),
            uppercase = "2",
            upperCaseRomanization = "#",
            backgroundColorIndex_uppercase = 5,
            upperCaseAlternates = listOf("#")
        ),
        KeyEntry(
            name = "row1_2",
            lowercase = "3",
            lowerCaseRomanization = "@",
            backgroundColorIndex_lowercase = 5,
            lowerCaseAlternates = listOf("@"),
            uppercase = "3",
            upperCaseRomanization = "\$",
            backgroundColorIndex_uppercase = 5,
            upperCaseAlternates = listOf("\$")
        ),
        KeyEntry(
            name = "row1_3",
            lowercase = "4",
            lowerCaseRomanization = "%",
            backgroundColorIndex_lowercase = 5,
            lowerCaseAlternates = listOf("%"),
            uppercase = "4",
            upperCaseRomanization = "%",
            backgroundColorIndex_uppercase = 5,
            upperCaseAlternates = listOf("%")
        ),
        KeyEntry(
            name = "row1_4",
            lowercase = "5",
            lowerCaseRomanization = "&",
            backgroundColorIndex_lowercase = 5,
            lowerCaseAlternates = listOf("&"),
            uppercase = "5",
            upperCaseRomanization = "&",
            backgroundColorIndex_uppercase = 5,
            upperCaseAlternates = listOf("&")
        ),
        KeyEntry(
            name = "row1_5",
            lowercase = "6",
            lowerCaseRomanization = "*",
            backgroundColorIndex_lowercase = 5,
            lowerCaseAlternates = listOf("*"),
            uppercase = "6",
            upperCaseRomanization = "*",
            backgroundColorIndex_uppercase = 5,
            upperCaseAlternates = listOf("*")
        ),
        KeyEntry(
            name = "row1_6",
            lowercase = "7",
            lowerCaseRomanization = "{",
            backgroundColorIndex_lowercase = 5,
            lowerCaseAlternates = listOf("{"),
            uppercase = "7",
            upperCaseRomanization = "{",
            backgroundColorIndex_uppercase = 5,
            upperCaseAlternates = listOf("{")
        ),
        KeyEntry(
            name = "row1_7",
            lowercase = "8",
            lowerCaseRomanization = "}",
            backgroundColorIndex_lowercase = 5,
            lowerCaseAlternates = listOf("}"),
            uppercase = "8",
            upperCaseRomanization = "}",
            backgroundColorIndex_uppercase = 5,
            upperCaseAlternates = listOf("}")
        ),
        KeyEntry(
            name = "row1_8",
            lowercase = "9",
            lowerCaseRomanization = "[",
            backgroundColorIndex_lowercase = 5,
            lowerCaseAlternates = listOf("["),
            uppercase = "9",
            upperCaseRomanization = "[",
            backgroundColorIndex_uppercase = 5,
            upperCaseAlternates = listOf("[")
        ),
        KeyEntry(
            name = "row1_9",
            lowercase = "0",
            lowerCaseRomanization = "]",
            backgroundColorIndex_lowercase = 5,
            lowerCaseAlternates = listOf("]"),
            uppercase = "0",
            upperCaseRomanization = "]",
            backgroundColorIndex_uppercase = 5,
            upperCaseAlternates = listOf("]")
        )
    )

    val row2: List<KeyEntry> = listOf(
        KeyEntry(
            name = "row2_0",
            lowercase = "_",
            uppercase = "_"
        ),
        KeyEntry(
            name = "row2_1",
            lowercase = ":",
            uppercase = ":"
        ),
        KeyEntry(
            name = "row2_2",
            lowercase = ";",
            uppercase = ";"
        ),
        KeyEntry(
            name = "row2_3",
            lowercase = "'",
            uppercase = "'"
        ),
        KeyEntry(
            name = "row2_4",
            lowercase = "\"",
            uppercase = "\""
        ),
        KeyEntry(
            name = "row2_5",
            lowercase = "(",
            uppercase = "("
        ),
        KeyEntry(
            name = "row2_6",
            lowercase = ")",
            uppercase = ")"
        ),
        KeyEntry(
            name = "row2_7",
            lowercase = "|",
            uppercase = "|"
        ),
        KeyEntry(
            name = "row2_8",
            lowercase = "/",
            uppercase = "/"
        ),
        KeyEntry(
            name = "row2_9",
            lowercase = "\\",
            uppercase = "\\"
        )
    )

    val row3: List<KeyEntry> = listOf(
        KeyEntry(
            name = "row3_0",
            lowercase = "<",
            uppercase = "<"
        ),
        KeyEntry(
            name = "row3_1",
            lowercase = ">",
            uppercase = ">"
        ),
        KeyEntry(
            name = "row3_2",
            lowercase = "-",
            uppercase = "-"
        ),
        KeyEntry(
            name = "row3_3",
            lowercase = "+",
            uppercase = "+"
        ),
        KeyEntry(
            name = "row3_4",
            lowercase = "=",
            uppercase = "="
        ),
        KeyEntry(
            name = "row3_5",
            lowercase = "!",
            uppercase = "!"
        ),
        KeyEntry(
            name = "row3_6",
            lowercase = "⸮",
            lowerCaseRomanization = "?",
            lowerCaseAlternates = listOf("?"),
            uppercase = "⸮",
            upperCaseRomanization = "?",
            upperCaseAlternates = listOf("?")
        )
    )

    fun layout(): KeyboardLayout = KeyboardLayout(
        "symbols", "", "", 0, " ",
        listOf(row1, row2, listOf(KeyEntry(name = "kg", lowercase = "🇰🇬", uppercase = "🇰🇬")) + row3 + listOf(KeyEntry(name = "Del", lowercase = "")))
    )
}
