package com.shoktuk.shoktukkeyboard.project.systems.localization

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import localized

enum class Loc_BasicInfo(
    val titleKey: String,
    val systemImage: ImageVector
) {
    USING_THE_KEYBOARD("usingTheKeyboard", Icons.Default.Info),
    ORIGINAL_BITIK("originalBitik", Icons.Default.Favorite),
    BITIK_RULES("bitikRules", Icons.Default.Star),
    RULE1("rule1", Icons.Default.Star),
    RULE2("rule2", Icons.Default.Star),
    RULE3("rule3", Icons.Default.Star),
    RULE4("rule4", Icons.Default.Star),
    MEMORIZE_TAMGAS("memorizeTamgas", Icons.Default.Memory),
    CARD_NUMBER("cardNumber", Icons.Default.Star),
    COPIED("copied", Icons.Default.Star);

    val fileName: String
        get() = "basicInfo"

    fun localizedTitle(context: Context): String {
        return titleKey.localized(fileName, context)
    }
}