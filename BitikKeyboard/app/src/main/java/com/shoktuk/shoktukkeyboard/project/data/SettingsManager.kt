package com.shoktuk.shoktukkeyboard.project.data

import android.content.Context
import androidx.core.content.edit

enum class KeyboardVariant(val id: String) {
    CLASSIC("settings_keyboardVariant_Classic"), SAMAGAN("settings_keyboardVariant_Modern")
}

enum class TextTranscription(val id: String) {
    On("Жанык"), Off("Өчүк")
}

enum class TamgaTranscription(val id: String) {
    On("Жанык"), Off("Өчүк")
}

enum class BitikDialect(val id: String) {
    Altay("Алтай"), Orkon("Оркон")
}

enum class KeyboardAlphabet(val id: String) {
    Bitik("bitik"), Latin("latin")
}

enum class A_Letter_Variannt(val id: String) {
    Default("𐰁"), Second("𐰀");

    fun getKey(): String {
        return "A_Letter_Variannt"
    }
}

enum class E_Letter_Variannt(val id: String) {
    Default("𐰅"), Second("𐰁");

    fun getKey(): String {
        return "e_variant"
    }
}

enum class EB_Letter_Variant(val id: String) {
    Default("𐰌"), Second("𐰋");

    fun getKey(): String {
        return "EB_Letter_Variant"
    }
}

enum class EN_Letter_Variant(val id: String) {
    Default("𐰤"), Second("𐰥");

    fun getKey(): String {
        return "EN_Letter_Variant"
    }
}

enum class AS_Letter_Variant(val id: String) {
    Default("𐰽"), Second("𐱂");

    fun getKey(): String {
        return "AS_variant"
    }
}

enum class ESH_Letter_Variant(val id: String) {
    Default("𐱁"), Second("𐰿");

    fun getKey(): String {
        return "ESH_Letter_Variant"
    }
}

object SettingsManager {
    private const val PREFS_NAME = "settings_prefs"
    private const val KEY_VARIANT = "keyboard_variant"
    private const val BITIKDIALECT = "BitikDialect"
    private const val BITIKALPHABET = "BitikAlphabet"
    private const val TEXT_TRANSCRIPTION = "text_transcription"
    private const val LETTER_TRANSCRIPTION = "letter_transcription"

    // Variant
    fun getKeyboardVariant(context: Context): KeyboardVariant {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return KeyboardVariant.valueOf(
            prefs.getString(KEY_VARIANT, KeyboardVariant.CLASSIC.name) ?: KeyboardVariant.CLASSIC.name
        )
    }

    fun setKeyboardVariant(context: Context, variant: KeyboardVariant) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_VARIANT, variant.name) }
    }

    // Bitik Dialect
    fun getBitikDialect(context: Context): BitikDialect {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return BitikDialect.valueOf(
            prefs.getString(BITIKDIALECT, BitikDialect.Altay.name) ?: BitikDialect.Altay.name
        )
    }

    fun setBitikDialect(context: Context, variant: BitikDialect) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(BITIKDIALECT, variant.name) }
    }

    // Keyboard Alphabet
    fun getBitikAlphabet(context: Context): KeyboardAlphabet {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return KeyboardAlphabet.valueOf(
            prefs.getString(BITIKALPHABET, KeyboardAlphabet.Bitik.name) ?: KeyboardAlphabet.Bitik.name
        )
    }

    fun setBitikAlphabet(context: Context, variant: KeyboardAlphabet) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(BITIKALPHABET, variant.name) }
    }

    // Text transcription
    fun getTextTranscription(context: Context): TextTranscription {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return TextTranscription.valueOf(
            prefs.getString(TEXT_TRANSCRIPTION, TextTranscription.On.name) ?: TextTranscription.On.name
        )
    }

    fun setTextTranscription(context: Context, variant: TextTranscription) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(TEXT_TRANSCRIPTION, variant.name) }
    }

    // Leter transcription
    fun getLeterTranscription(context: Context): TamgaTranscription {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return TamgaTranscription.valueOf(
            prefs.getString(LETTER_TRANSCRIPTION, TamgaTranscription.On.name) ?: TamgaTranscription.On.name
        )
    }

    fun setLeterTranscription(context: Context, variant: TamgaTranscription) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(LETTER_TRANSCRIPTION, variant.name) }
    }

    // A Letter
    fun getA_Variant(context: Context): A_Letter_Variannt {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return A_Letter_Variannt.valueOf(
            prefs.getString(A_Letter_Variannt.Default.getKey(), A_Letter_Variannt.Default.name) ?: A_Letter_Variannt.Default.name
        )
    }

    fun setAVariant(context: Context, variant: A_Letter_Variannt) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(A_Letter_Variannt.Default.getKey(), variant.name) }
    }

    // E Letter
    fun getE_Variant(context: Context): E_Letter_Variannt {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return E_Letter_Variannt.valueOf(
            prefs.getString(E_Letter_Variannt.Default.getKey(), E_Letter_Variannt.Default.name) ?: E_Letter_Variannt.Default.name
        )
    }

    fun setEVariant(context: Context, variant: E_Letter_Variannt) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(E_Letter_Variannt.Default.getKey(), variant.name) }
    }

    // EB Letter
    fun getEB_Variant(context: Context): EB_Letter_Variant {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return EB_Letter_Variant.valueOf(
            prefs.getString(EB_Letter_Variant.Default.getKey(), EB_Letter_Variant.Default.name) ?: EB_Letter_Variant.Default.name
        )
    }

    fun setEB_Variant(context: Context, variant: EB_Letter_Variant) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(EB_Letter_Variant.Default.getKey(), variant.name) }
    }

    // EN Letter
    fun getEN_Variant(context: Context): EN_Letter_Variant {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return EN_Letter_Variant.valueOf(
            prefs.getString(EN_Letter_Variant.Default.getKey(), EN_Letter_Variant.Default.name) ?: EN_Letter_Variant.Default.name
        )
    }

    fun setEN_Variant(context: Context, variant: EN_Letter_Variant) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(EN_Letter_Variant.Default.getKey(), variant.name) }
    }

    // aS letter
    fun getAS_Variant(context: Context): AS_Letter_Variant {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AS_Letter_Variant.valueOf(
            prefs.getString(AS_Letter_Variant.Default.getKey(), AS_Letter_Variant.Default.name) ?: AS_Letter_Variant.Default.name
        )
    }

    fun setAS_Variant(context: Context, variant: AS_Letter_Variant) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(AS_Letter_Variant.Default.getKey(), variant.name) }
    }

    // E Letter
    fun getES_Variant(context: Context): ESH_Letter_Variant {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return ESH_Letter_Variant.valueOf(
            prefs.getString(ESH_Letter_Variant.Default.getKey(), ESH_Letter_Variant.Default.name) ?: ESH_Letter_Variant.Default.name
        )
    }

    fun setES_Variant(context: Context, variant: ESH_Letter_Variant) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(ESH_Letter_Variant.Default.getKey(), variant.name) }
    }
}
