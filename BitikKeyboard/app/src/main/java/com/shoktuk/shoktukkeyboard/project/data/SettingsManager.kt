package com.shoktuk.shoktukkeyboard.project.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SettingsManager {
    private const val PREFS_NAME = "settings_prefs"

    private fun Context.prefs(): SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private inline fun <reified E : Enum<E>> SharedPreferences.getEnum(
        key: String, default: E
    ): E = runCatching { enumValueOf<E>(getString(key, default.name) ?: default.name) }.getOrElse { default }

    private fun SharedPreferences.setEnum(key: String, value: Enum<*>) = edit { putString(key, value.name) }

    private fun SharedPreferences.getStringOrDefault(key: String, default: String): String = getString(key, default) ?: default

    private fun SharedPreferences.setString(key: String, value: String) = edit { putString(key, value) }

    // --- Properties ---
    var Context.keyboardVariant: BitikVariant
        get() = prefs().getEnum(BitikVariant.KEY, BitikVariant.Modern)
        set(v) = prefs().setEnum(BitikVariant.KEY, v)

    var Context.bitikDialect: BitikDialect
        get() = prefs().getEnum(BitikDialect.KEY, BitikDialect.Altay)
        set(v) = prefs().setEnum(BitikDialect.KEY, v)

    var Context.textTranscription: TextTranscription
        get() = prefs().getEnum(TextTranscription.KEY, TextTranscription.On)
        set(v) = prefs().setEnum(TextTranscription.KEY, v)

    var Context.letterTranscription: LetterTranscription
        get() = prefs().getEnum(LetterTranscription.KEY, LetterTranscription.On)
        set(v) = prefs().setEnum(LetterTranscription.KEY, v)

    var Context.wordSeparator: WordSeparator
        get() = prefs().getEnum(WordSeparator.KEY, WordSeparator.NoSpace)
        set(v) = prefs().setEnum(WordSeparator.KEY, v)

    var Context.ajVariant: AJ_Letter_Variant
        get() = prefs().getEnum(AJ_Letter_Variant.KEY, AJ_Letter_Variant.Default)
        set(v) = prefs().setEnum(AJ_Letter_Variant.KEY, v)

    var Context.ebVariant: EB_Letter_Variant
        get() = prefs().getEnum(EB_Letter_Variant.KEY, EB_Letter_Variant.Default)
        set(v) = prefs().setEnum(EB_Letter_Variant.KEY, v)

    var Context.enVariant: EN_Letter_Variant
        get() = prefs().getEnum(EN_Letter_Variant.KEY, EN_Letter_Variant.Default)
        set(v) = prefs().setEnum(EN_Letter_Variant.KEY, v)

    var Context.asVariant: AS_Letter_Variant
        get() = prefs().getEnum(AS_Letter_Variant.KEY, AS_Letter_Variant.Default)
        set(v) = prefs().setEnum(AS_Letter_Variant.KEY, v)

    var Context.eshVariant: ESH_Letter_Variant
        get() = prefs().getEnum(ESH_Letter_Variant.KEY, ESH_Letter_Variant.Default)
        set(v) = prefs().setEnum(ESH_Letter_Variant.KEY, v)

    var Context.latinStatus: Latin_Status
        get() = prefs().getEnum(Latin_Status.KEY, Latin_Status.On)
        set(v) = prefs().setEnum(Latin_Status.KEY, v)

    var Context.arabicStatus: Arabic_Status
        get() = prefs().getEnum(Arabic_Status.KEY, Arabic_Status.Off)
        set(v) = prefs().setEnum(Arabic_Status.KEY, v)

    var Context.kirilisaStatus: Kirilisa_Status
        get() = prefs().getEnum(Kirilisa_Status.KEY, Kirilisa_Status.Off)
        set(v) = prefs().setEnum(Kirilisa_Status.KEY, v)

    var Context.coloring: Coloring
        get() = prefs().getEnum(Coloring.KEY, Coloring.On)
        set(v) = prefs().setEnum(Coloring.KEY, v)

    var Context.vibrations: Vibrations
        get() = prefs().getEnum(Vibrations.KEY, Vibrations.On)
        set(v) = prefs().setEnum(Vibrations.KEY, v)

    var Context.sounds: Sounds
        get() = prefs().getEnum(Sounds.KEY, Sounds.Off)
        set(v) = prefs().setEnum(Sounds.KEY, v)

    var Context.freeTamga_Click: String
        get() = prefs().getStringOrDefault("freeTamga_Click", "🇰🇬")
        set(v) = prefs().setString("freeTamga_Click", v)

    var Context.freeTamga_Hold: String
        get() = prefs().getStringOrDefault("freeTamga_Hold", "☀️")
        set(v) = prefs().setString("freeTamga_Hold", v)

    var Context.writingSystem: WritingSystem
        get() = prefs().getEnum(WritingSystem.KEY, WritingSystem.Bitik)
        set(v) = prefs().setEnum(WritingSystem.KEY, v)

    var Context.navBarPaddingSolution: NavBarPaddingSolution
        get() = prefs().getEnum(NavBarPaddingSolution.KEY, NavBarPaddingSolution.Solution_AllEnabled)
        set(v) = prefs().setEnum(NavBarPaddingSolution.KEY, v)
}