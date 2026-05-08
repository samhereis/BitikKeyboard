package com.shoktuk.shoktukkeyboard.project.screens.settings

import android.content.Context
import org.json.JSONArray

private const val PREFS_NAME = "settings_prefs"
private const val PREF_KEY_SAVED_STRINGS = "savedStringsJSON"

private val DEFAULT_STRINGS = listOf(
    "𐰽𐰞𐰢𐱄𐰽𐰕𐰉𐰃",
    "𐰀𐰺𐰃𐰉𐰬𐰕",
    "Salamatsızbı",
    "Arıbañız",
)

fun loadSavedStrings(ctx: Context): MutableList<String> {
    val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(PREF_KEY_SAVED_STRINGS, null)
    val list = runCatching {
        if (raw.isNullOrBlank()) mutableListOf()
        else {
            val arr = JSONArray(raw)
            MutableList(arr.length()) { i -> arr.optString(i) }
        }
    }.getOrElse { mutableListOf() }

    return if (list.isEmpty()) DEFAULT_STRINGS.toMutableList() else list
}

fun persistSavedStrings(ctx: Context, list: List<String>) {
    val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val arr = JSONArray().apply { list.forEach { put(it) } }
    prefs.edit().putString(PREF_KEY_SAVED_STRINGS, arr.toString()).apply()
}