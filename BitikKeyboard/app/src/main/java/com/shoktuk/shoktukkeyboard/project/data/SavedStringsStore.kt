package com.shoktuk.shoktukkeyboard.project.screens.settings

import android.content.Context
import org.json.JSONArray

private const val PREFS_NAME = "settings_prefs"
private const val PREF_KEY_SAVED_STRINGS = "savedStringsJSON"

fun loadSavedStrings(ctx: Context): MutableList<String> {
    val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(PREF_KEY_SAVED_STRINGS, null) ?: return mutableListOf()
    return runCatching {
        val arr = JSONArray(raw)
        MutableList(arr.length()) { i -> arr.optString(i) }
    }.getOrElse { mutableListOf() }
}

fun persistSavedStrings(ctx: Context, list: List<String>) {
    val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val arr = JSONArray().apply { list.forEach { put(it) } }
    prefs.edit().putString(PREF_KEY_SAVED_STRINGS, arr.toString()).apply()
}