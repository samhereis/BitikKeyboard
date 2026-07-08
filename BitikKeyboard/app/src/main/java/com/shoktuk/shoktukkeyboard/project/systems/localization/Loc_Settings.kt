package com.shoktuk.shoktukkeyboard.project.systems.localization

import android.content.Context
import localized

enum class Loc_Settings(
    val titleKey: String,
    val systemImage: String
) {

    setting_no("setting_no", "nosign"),
    keyboardVariant("keyboardVariant", "keyboard"),
    keyboardVariant_Classic("keyboardVariant_Classic", "circle"),
    keyboardVariant_Classic_Note("keyboardVariant_Classic_Note", "info.circle"),
    keyboardVariant_Standart("keyboardVariant_Standart", "square"),
    keyboardVariant_Standart_Note("keyboardVariant_Standart_Note", "info.circle"),
    keyboardVariant_Modern("keyboardVariant_Modern", "sparkles"),
    keyboardVariant_Modern_Note("keyboardVariant_Modern_Note", "info.circle"),

    keyboardDialect("keyboardDialect", "globe"),
    keyboardDialect_Altay("keyboardDialect_Altay", "a.circle"),
    keyboardDialect_Orhon("keyboardDialect_Orhon", "o.circle"),

    bitikLayout("bitikLayout", "globe"),

    textTranscription("textTranscription", "text.alignleft"),
    letterTranscription("letterTranscription", "character"),
    colon("colon", "colon"),

    experience("experience", "hand.tap"),
    coloring("coloring", "paintpalette"),
    vibration("vibration", "paintpalette"),
    holdabilityIndicator("holdabilityIndicator", "hand.point.up.left"),

    letters("letters", "e.circle"),
    ajLetter("ajLetter", "e.circle"),
    ebLetter("ebLetter", "e.circle"),
    enLetter("enLetter", "n.circle"),

    forAltayDialekt("forAltayDialekt", "a.square"),
    eshLetter("eshLetter", "a.circle"),
    asLetter("asLetter", "a.circle"),
    ekLetter("ekLetter", "k.circle"),

    forOrhonDialekt("forOrhonDialekt", "a.square"),
    angLetter("angLetter", "k.circle"),

    accessability("accessability", "figure.wave"),

    freeLetter("freeLetter", "square.dashed"),
    freeLetter_onHold("freeLetter_onHold", "hand.draw"),
    freeLetter_onPress("freeLetter_onPress", "hand.tap"),

    savables("savables", "tray.full"),

    otherAlphabets("otherAlphabets", "hand.tap"),
    latinAlphabet("latinAlphabet", "hand.tap"),
    latinAlphabet_Kyrgyz("latinAlphabet_Kyrgyz", "tray.full"),
    latinAlphabet_Full("latinAlphabet_Full", "tray.full"),
    arabAlphabet("arabAlphabet", "hand.tap"),
    kirilAlphabet("kirilAlphabet", "hand.tap"),

    changeOrder("changeOrder", "tray.full"),
    add("add", "tray.full"),
    ready("ready", "tray.full"),
    holdShift("holdShift", "tray.full"),
    andChooseSavables("andChooseSavables", "tray.full"),
    mainSettings("mainSettings", "keyboard"),
    appearance("appearance", "paintbrush"),
    other("other", "ellipsis.circle"),
    buttonHeight("buttonHeight", "tray.full"),
    bottomOffset("bottomOffset", "tray.full");

    val fileName: String
        get() = "settings"

    fun localizedTitle(context: Context): String {
        return titleKey.localized(fileName, context)
    }
}