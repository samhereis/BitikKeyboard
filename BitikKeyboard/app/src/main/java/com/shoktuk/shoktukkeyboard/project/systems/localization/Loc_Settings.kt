package com.shoktuk.shoktukkeyboard.project.systems.localization

import android.content.Context
import localized

enum class Loc_Settings(
    val systemImage: String
) {

    setting_no("nosign"),
    keyboardVariant("keyboard"),
    keyboardVariant_Classic("circle"),
    keyboardVariant_Classic_Note("info.circle"),
    keyboardVariant_Standart("square"),
    keyboardVariant_Standart_Note("info.circle"),
    keyboardVariant_Modern("sparkles"),
    keyboardVariant_Modern_Note("info.circle"),

    keyboardDialect("globe"),
    keyboardDialect_Altay("a.circle"),
    keyboardDialect_Orhon("o.circle"),

    howItLooks("eye"),
    whatIsIt("questionmark.circle"),

    bitikLayout("globe"),

    textTranscription("text.alignleft"),
    letterTranscription("character"),
    colon("colon"),

    experience("hand.tap"),
    coloring("paintpalette"),
    vibration("paintpalette"),
    bitikFont("textformat"),

    letters("e.circle"),
    ajLetter("e.circle"),
    ebLetter("e.circle"),
    enLetter("n.circle"),

    forAltayDialekt("a.square"),
    eshLetter("a.circle"),
    asLetter("a.circle"),
    ekLetter("k.circle"),

    forOrhonDialekt("a.square"),
    angLetter("k.circle"),

    accessability("figure.wave"),

    freeLetter("square.dashed"),
    freeLetter_onHold("hand.draw"),
    freeLetter_onPress("hand.tap"),

    savables("tray.full"),

    otherAlphabets("hand.tap"),
    suggestions("text.cursor"),
    suggestionTranscription("textformat.abc"),
    transcription("textformat.abc"),
    transcriptionScript("textformat.abc"),
    scriptLatin("textformat.abc"),
    scriptCyrillic("textformat.abc"),
    suggestionsBitikNote("textformat.abc"),
    suggestionsCyrillicNote("textformat.abc"),
    suggestionsArabNote("textformat.abc"),
    autoCapitalize("textformat.abc"),
    autoCapitalizeNote("textformat.abc"),
    transcriptionToBitik("textformat.abc"),
    personalizeT9("person"),
    personalizeT9Note("textformat.abc"),
    personalizedT9("person"),
    cloudSettingsPage("icloud"),
    cloudDeleteAll("icloud"),
    cloudDeleteAllNote("icloud"),
    cloudDeleteAllDone("icloud"),
    cloudDeleteAllConfirm("icloud"),
    cannotUndo("icloud"),
    remove("icloud"),
    delete("icloud"),
    cancel("icloud"),
    cloudEmpty("icloud"),
    t9Empty("icloud"),
    t9WordsCount("icloud"),
    t9ClearConfirm("icloud"),
    t9ClearNote("icloud"),
    latinAlphabet("hand.tap"),
    latinAlphabet_Kyrgyz("tray.full"),
    latinAlphabet_Full("tray.full"),
    arabAlphabet("hand.tap"),
    kirilAlphabet("hand.tap"),

    changeOrder("tray.full"),
    add("tray.full"),
    ready("tray.full"),
    autoBitik("sparkles"),
    holdShift("tray.full"),
    andChooseSavables("tray.full"),
    mainSettings("keyboard"),
    appearance("paintbrush"),
    other("ellipsis.circle"),
    buttonHeight("tray.full"),
    bottomOffset("tray.full"),

    cloud("icloud"),
    cloudAccount("person.icloud"),
    cloudChecking("hourglass"),
    cloudSignedIn("checkmark.icloud"),
    cloudNoAccount("xmark.icloud"),
    cloudRestricted("exclamationmark.icloud"),
    cloudUnavailable("icloud.slash"),
    cloudSettingsSynced("gearshape"),
    cloudSavedStrings("tray.full"),
    cloudLastChecked("clock"),
    cloudRefresh("arrow.clockwise"),
    cloudSignInHint("info.circle"),
    cloudError("exclamationmark.triangle");

    /** The resource key is the case name. */
    val titleKey: String
        get() = name

    val fileName: String
        get() = "settings"

    fun localizedTitle(context: Context): String {
        return titleKey.localized(fileName, context)
    }
}