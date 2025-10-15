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
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ajVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.angVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.asVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ebVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ekVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.enVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.eshVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant

class TranscriptionProccessor {
    fun processTranscription_bitik(text: String, context: Context): String {
        if (context.keyboardVariant == BitikVariant.CLASSIC) {
            return text
        }

        var result = text

        result = result.replace("𐰳", "𐰖")
        result = result.replace("𐰖", "𐰳")

        return result
    }

    fun processTranscription_alphabet(text: String, context: Context): String {
        var result = text

        if (context.keyboardVariant != BitikVariant.CLASSIC && context.ajVariant == AJ_Letter_Variant.Ay) {
            result = result.replace("𐰖", "𐰗")
            result = result.replace("𐰳", "𐰖")
        }

        if (context.ebVariant == EB_Letter_Variant.Second) {
            result = result.replace("𐰌", "𐰋")
        }

        if (context.enVariant == EN_Letter_Variant.Second) {
            result = result.replace("𐰤", "𐰥")
        }

        if (context.bitikDialect == BitikDialect.Altay) {
            if (context.ekVariant == EK_Letter_Variant.Second) {
                result = result.replace("𐰚", "𐰛")
            }

            if (context.asVariant == AS_Letter_Variant.Second) {
                result = result.replace("𐰽", "𐱂")
            }

            if (context.eshVariant == ESH_Letter_Variant.Second) {
                result = result.replace("𐱁", "𐰿")
            }
        }

        if (context.bitikDialect == BitikDialect.Orkon) {
            if (context.angVariant == ANG_Letter_Variant.Off) {
                result = result.replace("𐰬", "𐰭")
            }

            result = result.replace("𐱀", "𐱁")
            result = result.replace("𐰿", "𐱁")
            result = result.replace("𐱄", "𐱃")
        }

        return result
    }
}