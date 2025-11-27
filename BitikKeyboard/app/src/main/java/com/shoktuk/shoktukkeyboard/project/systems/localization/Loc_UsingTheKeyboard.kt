package com.shoktuk.shoktukkeyboard.project.systems.localization

import android.content.Context
import localized

enum class Loc_UsingTheKeyboard(
    val titleKey: String,
    val systemImage: String
) {

    keyboardDesign("keyboardDesign", "keyboard"),
    shiftButton("shiftButton", "shift"),
    standartColorsConsonants("standartColorsConsonants", "textformat"),
    blueColorIsVowels("blueColorIsVowels", "paintpalette"),
    redIsSpecialLetters("redIsSpecialLetters", "paintbrush"),
    disablingTheColoring("disablingTheColoring", "nosign"),
    secondLetterVariantsOnHold("secondLetterVariantsOnHold", "nosign"),
    noNeedToChangeShiftInOneWord("noNeedToChangeShiftInOneWord", "textformat.size.smaller"),
    needToChangeShiftException("needToChangeShiftException", "exclamationmark.triangle"),
    advice("advice", "lightbulb"),
    youCanSeeWordWritingInLatinOrKiril("youCanSeeWordWritingInLatinOrKiril", "character.book.closed");

    val fileName: String
        get() = "usingTheKeyboard"

    fun localizedTitle(context: Context): String {
        return titleKey.localized(fileName, context)
    }
}