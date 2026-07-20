package com.shoktuk.shoktukkeyboard.project.screens.settings

import android.content.Context
import androidx.core.content.edit
import com.shoktuk.shoktukkeyboard.project.data.SavedStringItem
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import java.util.UUID

private const val PREFS_NAME = "settings_prefs"
private const val LEGACY_KEY = "savedStringsJSON"

private val DEFAULT_STRINGS: Map<WritingSystem, List<String>> = mapOf(
    WritingSystem.Bitik to listOf("𐰽𐰞𐰢𐱄𐰽𐰕𐰉𐰃", "𐰌𐰄𐱅𐰛"),
    WritingSystem.Latin to listOf("Salamatsızbı", "Bitik"),
    WritingSystem.Kiril to listOf("Саламатсызбы", "Битик"),
    WritingSystem.Arab to listOf("‎سالاماتسىزبى", "‎بئتئك"),
)

private fun keyFor(script: WritingSystem) = "savedStringsJSON_${script.name}"

private fun defaultItems(script: WritingSystem): List<SavedStringItem> {
    val now = System.currentTimeMillis()
    return (DEFAULT_STRINGS[script] ?: emptyList()).mapIndexed { index, text ->
        SavedStringItem(id = UUID.randomUUID().toString(), text = text, sortOrder = index, modifiedAt = now)
    }
}

fun loadSavedStringItems(ctx: Context, script: WritingSystem): List<SavedStringItem> {
    val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val key = keyFor(script)

    prefs.getString(key, null)?.let { raw ->
        val items = SavedStringItem.decodeLocal(raw)
        return if (items.isNotEmpty()) items else defaultItems(script)
    }

    if (script == WritingSystem.Bitik) {
        val legacyItems = prefs.getString(LEGACY_KEY, null)?.let { SavedStringItem.decodeLocal(it) } ?: emptyList()
        if (legacyItems.isNotEmpty()) {
            persistSavedStringItems(ctx, script, legacyItems)
            return legacyItems
        }
    }

    return defaultItems(script)
}

fun persistSavedStringItems(ctx: Context, script: WritingSystem, items: List<SavedStringItem>) {
    ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(keyFor(script), SavedStringItem.encodeLocal(items))
    }
}

fun loadSavedStrings(ctx: Context, script: WritingSystem): MutableList<String> =
    loadSavedStringItems(ctx, script).map { it.text }.toMutableList()
