package com.shoktuk.shoktukkeyboard.keyboard

import com.shoktuk.shoktukkeyboard.keyboard.LayoutSwaps.SwapSettings
import com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardLetters
import com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardLetters_Arab
import com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardLetters_Kiril
import com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardLetters_Latin
import com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardLetters_Latin_Full
import com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardLetters_Old
import com.shoktuk.shoktukkeyboard.project.data.*
import org.junit.Test
import java.io.File

/** Dumps every key of every swap combination, in the same format as the iOS parity harness, for a line by line diff. */
class LayoutSwapsParityTest {

    private fun dump(out: StringBuilder, combo: String, layoutName: String, layout: KeyboardLayout) {
        layout.rows.forEachIndexed { r, row ->
            // the iOS side has no Shift / Del entries
            row.filter { it.name != "Shift" && it.name != "Del" && it.name != "kg" }.forEachIndexed { i, k ->
                out.append(listOf(
                    combo, layoutName, "row${r + 1}_$i", k.lowercase, k.uppercase ?: "", k.lowerCaseRomanization ?: "", k.upperCaseRomanization ?: "",
                    k.lowerCaseRomanization_Alt ?: "", k.upperCaseRomanization_Alt ?: "", "${k.backgroundColorIndex_lowercase ?: 1}", "${k.backgroundColorIndex_uppercase ?: 1}",
                    k.lowerCaseAlternates.orEmpty().joinToString(","), k.upperCaseAlternates.orEmpty().joinToString(","),
                    k.lowerCaseAlternateHints.orEmpty().joinToString(","), k.upperCaseAlternateHints.orEmpty().joinToString(",")
                ).joinToString("\t")).append('\n')
            }
        }
    }

    @Test
    fun dumpAll() {
        val out = StringBuilder()
        val variantNames = mapOf(BitikVariant.CLASSIC to "classic", BitikVariant.Modern to "modern", BitikVariant.SAMAGAN to "samagan")
        val dialectNames = mapOf(BitikDialect.Altay to "Altay", BitikDialect.Orkon to "Orhon")
        for (v in BitikVariant.values()) for (d in BitikDialect.values()) for (aj in AJ_Letter_Variant.values()) for (eb in EB_Letter_Variant.values())
            for (en in EN_Letter_Variant.values()) for (a in AS_Letter_Variant.values()) for (ek in EK_Letter_Variant.values())
                for (esh in ESH_Letter_Variant.values()) for (ang in ANG_Letter_Variant.values()) {
                    val s = SwapSettings(v, d, aj, eb, en, a, ek, esh, ang, Latin_Variant.minimal, Latin_ZH.j, Latin_CH.cedilla, Latin_NG.n, Latin_O.oUmlaut)
                    // iOS enum case names (see KeyboardLetters_*.fill): the full / cut / el letters are the "default" ones on Android
                    val combo = listOf(
                        "V:${variantNames[v]}", "D:${dialectNames[d]}",
                        "aj:${if (aj == AJ_Letter_Variant.Ach) "aj_ach" else "aj_ay"}",
                        "eb:${if (eb == EB_Letter_Variant.Default) "eb_Full" else "eb_Cut"}",
                        "en:${if (en == EN_Letter_Variant.Default) "en_Full" else "en_Cut"}",
                        "as:${if (a == AS_Letter_Variant.Default) "as_tamge" else "suu_tamge"}",
                        "ek:${if (ek == EK_Letter_Variant.Default) "ek_Cut" else "ek_Full"}",
                        "esh:${if (esh == ESH_Letter_Variant.Default) "esh_el" else "esh_eshik"}",
                        "ang:${if (ang == ANG_Letter_Variant.On) "ang_on" else "ang_off"}",
                    ).joinToString(" ")
                    val base = if (v == BitikVariant.CLASSIC) KeyboardLetters_Old.layout() else KeyboardLetters.layout()
                    dump(out, combo, "bitik", LayoutSwaps.apply(base, com.shoktuk.shoktukkeyboard.project.data.WritingSystem.Bitik, s))
                }
        for (v in Latin_Variant.values()) for (zh in Latin_ZH.values()) for (ch in Latin_CH.values()) for (ng in Latin_NG.values()) for (o in Latin_O.values()) {
            val s = SwapSettings(BitikVariant.Modern, BitikDialect.Altay, AJ_Letter_Variant.Jaa, EB_Letter_Variant.Default, EN_Letter_Variant.Default, AS_Letter_Variant.Default,
                EK_Letter_Variant.Default, ESH_Letter_Variant.Default, ANG_Letter_Variant.Off, v, zh, ch, ng, o)
            val base = if (v == Latin_Variant.minimal) KeyboardLetters_Latin.layout() else KeyboardLetters_Latin_Full.layout()
            val combo = "L:${v.name} zh:${zh.name} ch:${ch.name} ng:${ng.name} o:${o.name}"
            dump(out, combo, "latin", LayoutSwaps.apply(base, com.shoktuk.shoktukkeyboard.project.data.WritingSystem.Latin, s))
        }
        for ((label, ws) in listOf("Bitik" to com.shoktuk.shoktukkeyboard.project.data.WritingSystem.Bitik, "Latin" to com.shoktuk.shoktukkeyboard.project.data.WritingSystem.Latin,
            "Kiril" to com.shoktuk.shoktukkeyboard.project.data.WritingSystem.Kiril, "Arab" to com.shoktuk.shoktukkeyboard.project.data.WritingSystem.Arab)) {
            dump(out, "S:$label", "symbols", LayoutSwaps.applySymbols(com.shoktuk.shoktukkeyboard.keyboard.layouts.KeyboardSymbols.layout(), ws))
        }
        dump(out, "static", "latin_min", KeyboardLetters_Latin.layout())
        dump(out, "static", "latin_full", KeyboardLetters_Latin_Full.layout())
        dump(out, "static", "kiril", KeyboardLetters_Kiril.layout())
        dump(out, "static", "arab", KeyboardLetters_Arab.layout())
        File("/tmp/refac/kotlin_dump.tsv").writeText(out.toString())
    }
}
