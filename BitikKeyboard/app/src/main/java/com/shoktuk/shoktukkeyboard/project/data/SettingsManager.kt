package com.shoktuk.shoktukkeyboard.project.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SettingsManager {
    private const val PREFS_NAME = "settings_prefs"

    /**
     * Only the debug store-screenshot tool ever sets this (always null in the app): the settings are then read from and
     * written to this in-memory copy, so the pictures never touch the person's saved settings.
     */
    @Volatile
    var prefsOverride: SharedPreferences? = null

    private fun Context.prefs(): SharedPreferences = prefsOverride ?: getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private inline fun <reified E : Enum<E>> SharedPreferences.getEnum(
        key: String, default: E
    ): E = runCatching { enumValueOf<E>(getString(key, default.name) ?: default.name) }.getOrElse { default }

    private fun SharedPreferences.setEnum(key: String, value: Enum<*>) = edit { putString(key, value.name) }

    private fun SharedPreferences.getStringOrDefault(key: String, default: String): String = getString(key, default) ?: default
    private fun SharedPreferences.setString(key: String, value: String) = edit { putString(key, value) }

    private fun SharedPreferences.getIntOrDefault(key: String, default: Int): Int {
        try {
            return getInt(key, default) ?: default
        } catch (e: Exception) {
            print(e)
        }

        return 170
    }

    private fun SharedPreferences.setInt(key: String, value: Int) = edit { putInt(key, value) }

    private const val SUGGESTIONS_OFF_KEY = "suggestionsOffScripts"

    /** Whether the suggestion strip is on for [script]; each of the four scripts has its own switch (on by default). */
    fun Context.suggestionsEnabled(script: WritingSystem): Boolean =
        !prefs().getStringOrDefault(SUGGESTIONS_OFF_KEY, "").split(",").contains(script.id)

    fun Context.setSuggestionsEnabled(script: WritingSystem, on: Boolean) {
        val off = prefs().getStringOrDefault(SUGGESTIONS_OFF_KEY, "").split(",").filter { it.isNotEmpty() && it != script.id }.toMutableList()
        if (!on) off.add(script.id)
        prefs().setString(SUGGESTIONS_OFF_KEY, off.sorted().joinToString(","))
    }

    private const val SUGGESTION_TRANSCRIPTION_KEY = "suggestionTranscriptionScripts"

    /**
     * Whether suggestions of [script] show a small transcription under each word: Latin shows the Cyrillic spelling,
     * Arabic the Latin one, Bitik the Latin or Cyrillic one (its own setting). On for all three until turned off.
     */
    fun Context.suggestionTranscription(script: WritingSystem): Boolean =
        prefs().getStringOrDefault(SUGGESTION_TRANSCRIPTION_KEY, "${WritingSystem.Bitik.id},${WritingSystem.Latin.id},${WritingSystem.Arab.id}").split(",").contains(script.id)

    private const val AUTO_CAPITALIZE_KEY = "autoCapitalize"

    /** Capital letter at the start of a text and after a sentence end (Latin and Cyrillic keyboards). On until turned off. */
    var Context.autoCapitalize: Boolean
        get() = prefs().getStringOrDefault(AUTO_CAPITALIZE_KEY, "on") != "off"
        set(value) { prefs().setString(AUTO_CAPITALIZE_KEY, if (value) "on" else "off") }

    private const val PERSONALIZE_KEY = "personalizeT9"

    /** "Personalize T9": remember the words you write to rank suggestions. Turning it off forgets them all. */
    var Context.personalizeT9: Boolean
        get() = prefs().getStringOrDefault(PERSONALIZE_KEY, "on") != "off"
        set(value) {
            prefs().setString(PERSONALIZE_KEY, if (value) "on" else "off")
            if (!value) com.shoktuk.shoktukkeyboard.suggestions.LearnedWords.clear(this)
        }

    private const val SUGGESTION_SCRIPT_KEY = "suggestionTranscriptionScript"

    /**
     * The spelling shown under Bitik suggestions: "kiril" or "latin". With nothing stored yet it is picked from the
     * system language (Russian and Kyrgyz read Cyrillic) and stored.
     */
    var Context.suggestionTranscriptionScript: String
        get() {
            val stored = prefs().getStringOrDefault(SUGGESTION_SCRIPT_KEY, "")
            if (stored.isNotEmpty()) return stored
            val language = java.util.Locale.getDefault().language
            val chosen = if (language == "ru" || language == "ky") "kiril" else "latin"
            prefs().setString(SUGGESTION_SCRIPT_KEY, chosen)
            return chosen
        }
        set(v) = prefs().setString(SUGGESTION_SCRIPT_KEY, v)

    fun Context.setSuggestionTranscription(script: WritingSystem, on: Boolean) {
        val scripts = prefs().getStringOrDefault(SUGGESTION_TRANSCRIPTION_KEY, "${WritingSystem.Bitik.id},${WritingSystem.Latin.id},${WritingSystem.Arab.id}").split(",").filter { it.isNotEmpty() && it != script.id }.toMutableList()
        if (on) scripts.add(script.id)
        prefs().setString(SUGGESTION_TRANSCRIPTION_KEY, scripts.sorted().joinToString(","))
    }

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

    var Context.bitikTranscriptionAlphabets: BitikTranscriptionAlphabets
        get() = prefs().getEnum(BitikTranscriptionAlphabets.KEY, BitikTranscriptionAlphabets.On)
        set(v) = prefs().setEnum(BitikTranscriptionAlphabets.KEY, v)

    var Context.wordSeparator: WordSeparator
        get() = prefs().getEnum(WordSeparator.KEY, WordSeparator.NoSpace)
        set(v) = prefs().setEnum(WordSeparator.KEY, v)

    var Context.ajVariant: AJ_Letter_Variant
        get() = prefs().getEnum(AJ_Letter_Variant.KEY, AJ_Letter_Variant.Jaa)
        set(v) = prefs().setEnum(AJ_Letter_Variant.KEY, v)

    var Context.angVariant: ANG_Letter_Variant
        get() = prefs().getEnum(ANG_Letter_Variant.KEY, ANG_Letter_Variant.Off)
        set(v) = prefs().setEnum(ANG_Letter_Variant.KEY, v)

    var Context.ebVariant: EB_Letter_Variant
        get() = prefs().getEnum(EB_Letter_Variant.KEY, EB_Letter_Variant.Default)
        set(v) = prefs().setEnum(EB_Letter_Variant.KEY, v)

    var Context.enVariant: EN_Letter_Variant
        get() = prefs().getEnum(EN_Letter_Variant.KEY, EN_Letter_Variant.Default)
        set(v) = prefs().setEnum(EN_Letter_Variant.KEY, v)

    var Context.asVariant: AS_Letter_Variant
        get() = prefs().getEnum(AS_Letter_Variant.KEY, AS_Letter_Variant.Second)
        set(v) = prefs().setEnum(AS_Letter_Variant.KEY, v)

    var Context.ekVariant: EK_Letter_Variant
        get() = prefs().getEnum(EK_Letter_Variant.KEY, EK_Letter_Variant.Default)
        set(v) = prefs().setEnum(EK_Letter_Variant.KEY, v)

    var Context.eshVariant: ESH_Letter_Variant
        get() = prefs().getEnum(ESH_Letter_Variant.KEY, ESH_Letter_Variant.Default)
        set(v) = prefs().setEnum(ESH_Letter_Variant.KEY, v)

    var Context.latinStatus: Latin_Status
        get() = prefs().getEnum(Latin_Status.KEY, Latin_Status.On)
        set(v) = prefs().setEnum(Latin_Status.KEY, v)

    var Context.latinVariant: Latin_Variant
        get() = prefs().getEnum(Latin_Variant.KEY, Latin_Variant.minimal)
        set(v) = prefs().setEnum(Latin_Variant.KEY, v)

    var Context.latinZH: Latin_ZH
        get() = prefs().getEnum(Latin_ZH.KEY, Latin_ZH.j)
        set(v) = prefs().setEnum(Latin_ZH.KEY, v)

    var Context.latinCH: Latin_CH
        get() = prefs().getEnum(Latin_CH.KEY, Latin_CH.cedilla)
        set(v) = prefs().setEnum(Latin_CH.KEY, v)

    var Context.latinNG: Latin_NG
        get() = prefs().getEnum(Latin_NG.KEY, Latin_NG.n)
        set(v) = prefs().setEnum(Latin_NG.KEY, v)

    var Context.latinO: Latin_O
        get() = prefs().getEnum(Latin_O.KEY, Latin_O.oUmlaut)
        set(v) = prefs().setEnum(Latin_O.KEY, v)

    var Context.arabicStatus: Arabic_Status
        get() = prefs().getEnum(Arabic_Status.KEY, Arabic_Status.Off)
        set(v) = prefs().setEnum(Arabic_Status.KEY, v)

    var Context.kirilisaStatus: Kirilisa_Status
        get() = prefs().getEnum(Kirilisa_Status.KEY, Kirilisa_Status.On)
        set(v) = prefs().setEnum(Kirilisa_Status.KEY, v)

    var Context.coloring: Coloring
        get() = prefs().getEnum(Coloring.KEY, Coloring.On)
        set(v) = prefs().setEnum(Coloring.KEY, v)

    var Context.bitikFont: BitikFont
        get() = prefs().getEnum(BitikFont.KEY, BitikFont.Zamanchak)
        set(v) = prefs().setEnum(BitikFont.KEY, v)

    var Context.vibrations: Vibrations
        get() = prefs().getEnum(Vibrations.KEY, Vibrations.On)
        set(v) = prefs().setEnum(Vibrations.KEY, v)

    var Context.sounds: Sounds
        get() = prefs().getEnum(Sounds.KEY, Sounds.On)
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
        get() = prefs().getEnum(NavBarPaddingSolution.KEY, NavBarPaddingSolution.Solution_Auto)
        set(v) = prefs().setEnum(NavBarPaddingSolution.KEY, v)

    var Context.keyboardHeight: Int
        get() = prefs().getIntOrDefault("buttonHeight", 170)
        set(v) = prefs().setInt("buttonHeight", v)

    var Context.savedStringsTextSize: Int
        get() = prefs().getIntOrDefault("savedStringsTextSize", 15)
        set(v) = prefs().setInt("savedStringsTextSize", v)

    var Context.bottomOffset: Int
        get() = prefs().getIntOrDefault("bottomOffset", 25)
        set(v) = prefs().setInt("bottomOffset", v)
}