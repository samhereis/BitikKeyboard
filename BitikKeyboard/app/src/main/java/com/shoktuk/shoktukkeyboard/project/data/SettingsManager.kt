package com.shoktuk.shoktukkeyboard.project.data

import android.content.Context

enum class KeyboardVariant(val id: String) {
    CLASSIC("settings_keyboardVariant_Classic"), SAMAGAN("settings_keyboardVariant_Modern")
}

enum class TranscriptionAlphabet {
    LATIN, CYRILLIC
}

object SettingsManager {
    private const val PREFS_NAME = "settings_prefs"
    private const val KEY_VARIANT = "keyboard_variant"
    private const val KEY_ALPHABET = "transcription_alphabet"

    fun getKeyboardVariant(context: Context): KeyboardVariant {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return KeyboardVariant.valueOf(
            prefs.getString(KEY_VARIANT, KeyboardVariant.CLASSIC.name) ?: KeyboardVariant.CLASSIC.name
        )
    }

    fun setKeyboardVariant(context: Context, variant: KeyboardVariant) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_VARIANT, variant.name).apply()
    }

    fun getTranscriptionAlphabet(context: Context): TranscriptionAlphabet {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return TranscriptionAlphabet.valueOf(
            prefs.getString(KEY_ALPHABET, TranscriptionAlphabet.LATIN.name) ?: TranscriptionAlphabet.LATIN.name
        )
    }

    fun setTranscriptionAlphabet(context: Context, alphabet: TranscriptionAlphabet) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ALPHABET, alphabet.name).apply()
    }
}
