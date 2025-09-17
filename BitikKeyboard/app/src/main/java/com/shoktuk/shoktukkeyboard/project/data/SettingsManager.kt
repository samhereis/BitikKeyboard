package com.shoktuk.shoktukkeyboard.project.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

// ---------- Enums ----------
enum class BitikVariant(val id: String) {
    CLASSIC("settings_keyboardVariant_Classic"), SAMAGAN("settings_keyboardVariant_Modern");

    companion object {
        const val KEY = "keyboard_variant"
    }
}

enum class TextTranscription(val id: String) {
    On("Жанык"), Off("Өчүк");

    companion object {
        const val KEY = "text_transcription"
    }
}

enum class LetterTranscription(val id: String) {
    On("Жанык"), Off("Өчүк");

    companion object {
        const val KEY = "letter_transcription"
    }
}

enum class BitikDialect(val id: String) {
    Altay("Алтай"), Orkon("Оркон");

    companion object {
        const val KEY = "BitikDialect"
    }
}

enum class WritingSystem(val id: String) {
    Bitik("Bitik"), Latin("latin"), Kiril("kiril");

    companion object {
        const val KEY = "WritingSystem"
    }
}

enum class EB_Letter_Variant(val id: String) {
    Default("𐰌"), Second("𐰋");

    companion object {
        const val KEY = "EB_Letter_Variant"
    }
}

enum class EN_Letter_Variant(val id: String) {
    Default("𐰤"), Second("𐰥");

    companion object {
        const val KEY = "EN_Letter_Variant"
    }
}

enum class AS_Letter_Variant(val id: String) {
    Default("𐰽"), Second("𐱂");

    companion object {
        const val KEY = "AS_variant"
    }
}

enum class ESH_Letter_Variant(val id: String) {
    Default("𐱁"), Second("𐰿");

    companion object {
        const val KEY = "ESH_Letter_Variant"
    }
}

// ---------- Settings Manager ----------
object SettingsManager {
    private const val PREFS_NAME = "settings_prefs"

    private fun Context.prefs(): SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private inline fun <reified E : Enum<E>> SharedPreferences.getEnum(
        key: String, default: E
    ): E = runCatching { enumValueOf<E>(getString(key, default.name) ?: default.name) }.getOrElse { default }

    private fun SharedPreferences.setEnum(key: String, value: Enum<*>) = edit { putString(key, value.name) }

    // --- Properties ---
    var Context.keyboardVariant: BitikVariant
        get() = prefs().getEnum(BitikVariant.KEY, BitikVariant.CLASSIC)
        set(v) = prefs().setEnum(BitikVariant.KEY, v)

    var Context.bitikDialect: BitikDialect
        get() = prefs().getEnum(BitikDialect.KEY, BitikDialect.Altay)
        set(v) = prefs().setEnum(BitikDialect.KEY, v)

    var Context.writingSystem: WritingSystem
        get() = prefs().getEnum(WritingSystem.KEY, WritingSystem.Bitik)
        set(v) = prefs().setEnum(WritingSystem.KEY, v)

    var Context.textTranscription: TextTranscription
        get() = prefs().getEnum(TextTranscription.KEY, TextTranscription.On)
        set(v) = prefs().setEnum(TextTranscription.KEY, v)

    var Context.letterTranscription: LetterTranscription
        get() = prefs().getEnum(LetterTranscription.KEY, LetterTranscription.On)
        set(v) = prefs().setEnum(LetterTranscription.KEY, v)

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
}