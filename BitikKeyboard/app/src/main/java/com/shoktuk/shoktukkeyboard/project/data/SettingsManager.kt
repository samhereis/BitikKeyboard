package com.shoktuk.shoktukkeyboard.project.data

import android.content.Context
import androidx.core.content.edit

enum class KeyboardVariant(val id: String) {
    CLASSIC("settings_keyboardVariant_Classic"), SAMAGAN("settings_keyboardVariant_Modern")
}

enum class E_Letter_Variannt(val id: String) {
    E_A("𐰁"), E_E("𐰅")
}

enum class TextTranscription(val id: String) {
    On("Жанык"), Off("Өчүк")
}

enum class TamgaTranscription(val id: String) {
    On("Жанык"), Off("Өчүк")
}

enum class AS_Letter_Variant(val id: String) {
    AS_As("𐱂"), AS_SU("𐰽")
}

enum class BitikDialect(val id: String) {
    Altay("Алтай"), Orkon("Оркон")
}


object SettingsManager {
    private const val PREFS_NAME = "settings_prefs"
    private const val KEY_VARIANT = "keyboard_variant"
    private const val E_VARIANT = "e_variant"
    private const val TEXT_TRANSCRIPTION = "text_transcription"
    private const val LETTER_TRANSCRIPTION = "letter_transcription"
    private const val AS_VARIANT = "AS_variant"
    private const val BITIKDIALECT = "BitikDialect"

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

    // E Letter
    fun getEVariant(context: Context): E_Letter_Variannt {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return E_Letter_Variannt.valueOf(
            prefs.getString(E_VARIANT, E_Letter_Variannt.E_E.name) ?: E_Letter_Variannt.E_E.name
        )
    }

    fun setEVariant(context: Context, variant: E_Letter_Variannt) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(E_VARIANT, variant.name) }
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

    // aS letter
    fun getASVariant(context: Context): AS_Letter_Variant {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AS_Letter_Variant.valueOf(
            prefs.getString(AS_VARIANT, AS_Letter_Variant.AS_SU.name) ?: AS_Letter_Variant.AS_SU.name
        )
    }

    fun setASVariant(context: Context, variant: AS_Letter_Variant) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(AS_VARIANT, variant.name) }
    }

    // aS letter
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
}
