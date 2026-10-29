package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import com.shoktuk.shoktukkeyboard.project.data.AJ_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ANG_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.AS_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.EB_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EK_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EN_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ESH_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.Latin_CH
import com.shoktuk.shoktukkeyboard.project.data.Latin_NG
import com.shoktuk.shoktukkeyboard.project.data.Latin_O
import com.shoktuk.shoktukkeyboard.project.data.Latin_Variant
import com.shoktuk.shoktukkeyboard.project.data.Latin_ZH
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ajVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.angVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.asVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ebVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ekVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.enVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.eshVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinCH
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinNG
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinO
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinZH
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem

/**
 * The letter swaps the settings ask for, applied once when a layout is loaded (the Android twin of iOS's `fill()` /
 * `updateRowsFromSettings()`). Layout JSON is the iOS data as is; keys are addressed by their `rowN_i` names.
 */
object LayoutSwaps {

    /** Everything the swaps read from the settings, as plain values (so the swaps can be tested without Android). */
    data class SwapSettings(
        val keyboardVariant: BitikVariant, val bitikDialect: BitikDialect,
        val aj: AJ_Letter_Variant, val eb: EB_Letter_Variant, val en: EN_Letter_Variant, val asv: AS_Letter_Variant,
        val ek: EK_Letter_Variant, val esh: ESH_Letter_Variant, val ang: ANG_Letter_Variant,
        val latinVariant: Latin_Variant, val zh: Latin_ZH, val ch: Latin_CH, val ng: Latin_NG, val o: Latin_O,
    )

    fun settingsFrom(context: Context) = SwapSettings(
        context.keyboardVariant, context.bitikDialect, context.ajVariant, context.ebVariant, context.enVariant, context.asVariant,
        context.ekVariant, context.eshVariant, context.angVariant, context.latinVariant, context.latinZH, context.latinCH, context.latinNG, context.latinO,
    )

    fun apply(layout: KeyboardLayout, system: WritingSystem, context: Context): KeyboardLayout = apply(layout, system, settingsFrom(context))

    fun apply(layout: KeyboardLayout, system: WritingSystem, settings: SwapSettings): KeyboardLayout {
        val swap: (KeyEntry) -> KeyEntry = when (system) {
            WritingSystem.Bitik -> bitik(settings)
            WritingSystem.Latin -> latin(settings)
            else -> return layout
        }
        // Some layout files carry no "name" (Gson leaves it null), so build the copy instead of calling copy().
        @Suppress("USELESS_ELVIS")
        val name = (layout.name as String?) ?: ""
        return KeyboardLayout(name, layout.languageCode, layout.languageColor, layout.directionality, layout.spaceKey, layout.rows.map { row -> row.map(swap) })
    }

    /**
     * The symbols screen (iOS `updateQMark()`): Bitik and Arabic write the question mark as `⸮` with `?` as its hint and
     * alternate (that is how the data is stored); every other script gets a plain `?`.
     */
    fun applySymbols(layout: KeyboardLayout, system: WritingSystem): KeyboardLayout {
        if (system == WritingSystem.Bitik || system == WritingSystem.Arab) return layout
        @Suppress("USELESS_ELVIS")
        val name = (layout.name as String?) ?: ""
        return KeyboardLayout(name, layout.languageCode, layout.languageColor, layout.directionality, layout.spaceKey, layout.rows.map { row ->
            row.map { key ->
                if (key.name == "row3_6") key.copy(
                    lowercase = "?", uppercase = "?", lowerCaseRomanization = null, upperCaseRomanization = null,
                    lowerCaseAlternates = null, upperCaseAlternates = null, lowerCaseAlternateHints = null, upperCaseAlternateHints = null
                ) else key
            }
        })
    }

    // --- Latin ---------------------------------------------------------------------------------------------------

    private fun latin(settings: SwapSettings): (KeyEntry) -> KeyEntry {
        val minimal = settings.latinVariant == Latin_Variant.minimal
        val zhC = settings.zh == Latin_ZH.c
        val chC = settings.ch == Latin_CH.c
        val ng = settings.ng == Latin_NG.ng
        val barredO = settings.o == Latin_O.oBarred

        return { key ->
            when {
                minimal && zhC && key.name == "row2_6" ->
                    key.copy(lowercase = "c", lowerCaseRomanization = "j", uppercase = "C", upperCaseRomanization = "J").withAlternates(listOf("j", "ć", "č", "ĉ", "ċ"))
                minimal && zhC && key.name == "row3_2" ->
                    key.copy(
                        lowerCaseRomanization = null, lowerCaseRomanization_Alt = null, upperCaseRomanization = null, upperCaseRomanization_Alt = null,
                        lowerCaseAlternates = null, upperCaseAlternates = null, lowerCaseAlternateHints = null, upperCaseAlternateHints = null
                    )
                minimal && !zhC && chC && key.name == "row3_2" ->
                    key.copy(lowercase = "c", lowerCaseRomanization = "ç", uppercase = "C", upperCaseRomanization = "Ç").withAlternates(listOf("ç", "ć", "č", "ĉ", "ċ"))
                ng && minimal && key.name == "row3_1" -> key.copy(lowercase = "ŋ", uppercase = "Ŋ").withAlternates(listOf("x", "ñ"))
                ng && !minimal && key.name == "row2_11" -> key.copy(lowercase = "ŋ", uppercase = "Ŋ").withAlternates(listOf("ŋ", "ñ"))
                barredO && minimal && key.name == "row2_5" -> key.copy(lowercase = "ө", uppercase = "Ө").withAlternates(listOf("h", "ö"))
                barredO && !minimal && key.name == "row3_7" -> key.copy(lowercase = "ө", uppercase = "Ө").withAlternates(listOf("ө", "ö"))
                else -> key
            }
        }
    }

    // --- Bitik (Altay / Orhon x classic / default / modern) -------------------------------------------------------

    private fun bitik(settings: SwapSettings): (KeyEntry) -> KeyEntry {
        val variant = settings.keyboardVariant
        val classic = variant == BitikVariant.CLASSIC
        val defaultVariant = variant == BitikVariant.Modern
        val samagan = variant == BitikVariant.SAMAGAN
        val altay = settings.bitikDialect == BitikDialect.Altay

        val ach = settings.aj == AJ_Letter_Variant.Ach
        val ebFull = settings.eb == EB_Letter_Variant.Default
        val enFull = settings.en == EN_Letter_Variant.Default
        val asTamge = settings.asv == AS_Letter_Variant.Default
        val ekCut = settings.ek == EK_Letter_Variant.Default
        val eshEl = settings.esh == ESH_Letter_Variant.Default
        val angOn = settings.ang == ANG_Letter_Variant.On

        return { key ->
            var k = key

            // Settings swaps shared by every Bitik layout (iOS `KeyboardLetters.fill()` / `KeyboardLetters_Old.fill()`).
            when (key.name) {
                "row1_5" -> if (!classic) k = k.withLower(if (ach) "𐰖" else "𐰗", if (ach) "𐰗" else null)
                "row2_6" -> if (!classic) k = k.withLower(if (ach) "𐰳" else "𐰖", if (ach) null else "𐰳")
                "row3_4" -> k = k.swapUpper(if (ebFull) "𐰌" else "𐰋")
                "row3_5" -> k = k.swapUpper(if (enFull) "𐰤" else "𐰥")
            }
            if (altay) {
                when (key.name) {
                    "row2_1" -> k = k.swapLower(if (asTamge) "𐱂" else "𐰽")
                    "row2_7" -> k = k.swapUpper(if (ekCut) "𐰚" else "𐰛")
                    "row1_1" -> if (!classic) k = k.swapUpper(if (eshEl) "𐱁" else "𐰿")
                }
            }

            // Variant specific letters (default shows O/U and OT as separate keys, modern folds OT into O).
            if (!classic) {
                when (key.name) {
                    "row1_6" -> k = if (defaultVariant) k.copy(lowercase = "𐰆", lowerCaseRomanization = "O / U", lowerCaseRomanization_Alt = "O / У")
                    else k.copy(lowercase = "𐰩", lowerCaseRomanization = "U", lowerCaseRomanization_Alt = "У")
                    "row1_8" -> k = if (defaultVariant) k.copy(
                        lowercase = "𐱇", lowerCaseRomanization = "OT", lowerCaseRomanization_Alt = "от", backgroundColorIndex_lowercase = 5,
                        lowerCaseAlternates = null, lowerCaseAlternateHints = null
                    ) else k.copy(
                        lowercase = "𐰆", lowerCaseRomanization = "O", lowerCaseRomanization_Alt = "O", backgroundColorIndex_lowercase = 4,
                        lowerCaseAlternateHints = listOf("OT от")
                    ).withLower("𐰆", "𐱇")
                    "row2_0" -> k = k.swapLower(if (samagan) "𐰁" else "𐰀")
                }
            }

            // Orhon: iOS `KeyboardLetters_Orh.fill()` / `KeyboardLetters_Old_Orh.fill()`.
            if (!altay) {
                when (key.name) {
                    "row3_1" -> k = k.copy(
                        lowercase = if (angOn) "𐰬" else "𐰭",
                        lowerCaseRomanization = if (angOn) "aÑ" else "Ñ",
                        upperCaseRomanization = if (angOn) "eÑ" else "Ñ",
                        lowerCaseRomanization_Alt = if (classic) k.lowerCaseRomanization_Alt else if (angOn) "ңъ" else "ң",
                        upperCaseRomanization_Alt = if (classic) k.upperCaseRomanization_Alt else if (angOn) "нь" else "ң"
                    )
                    "row1_1" -> k = if (classic) {
                        // the letter pushed aside (𐱀) joins the alternates next to 𐰿
                        k.swapLower("𐱁").swapUpper("𐱁").copy(lowerCaseRomanization = "Ş", upperCaseRomanization = "Ş")
                    } else {
                        k.copy(
                            lowercase = "𐱁", uppercase = "𐱁", lowerCaseRomanization = "Ş", upperCaseRomanization = "Ş",
                            lowerCaseRomanization_Alt = "ш", upperCaseRomanization_Alt = "ш"
                        ).withLower("𐱁", "𐱀").withUpper("𐱁", "𐰿")
                    }
                    "row1_4" -> k = k.swapLower("𐱃")
                    "row2_0" -> if (classic) k = k.swapUpper("𐰰")
                }
            }
            k
        }
    }

    /** iOS `swapLower`: [newMain] becomes the lowercase letter, the letter it replaces goes into the alternates where [replace] stood (default [newMain]). */
    private fun KeyEntry.swapLower(newMain: String, replace: String? = null): KeyEntry {
        val previous = lowercase
        val target = replace ?: newMain
        return copy(lowercase = newMain, lowerCaseAlternates = lowerCaseAlternates?.map { if (it == target) previous else it }?.filter { it != newMain }?.takeIf { it.isNotEmpty() })
    }

    private fun KeyEntry.swapUpper(newMain: String, replace: String? = null): KeyEntry {
        val previous = uppercase ?: ""
        val target = replace ?: newMain
        return copy(uppercase = newMain, upperCaseAlternates = upperCaseAlternates?.map { if (it == target) previous else it }?.filter { it != newMain }?.takeIf { it.isNotEmpty() })
    }

    /** `setLowerAlternate` of iOS: sets the lowercase letter (when given) and the single alternate. */
    private fun KeyEntry.withLower(letter: String, alternate: String?): KeyEntry =
        copy(lowercase = letter, lowerCaseAlternates = alternate?.takeIf { it != letter }?.let { listOf(it) })

    private fun KeyEntry.withUpper(letter: String, alternate: String?): KeyEntry =
        copy(uppercase = letter, upperCaseAlternates = alternate?.takeIf { it != letter }?.let { listOf(it) })
}
