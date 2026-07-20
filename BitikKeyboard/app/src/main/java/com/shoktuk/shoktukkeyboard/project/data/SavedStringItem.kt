package com.shoktuk.shoktukkeyboard.project.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

data class SavedStringItem(
    val id: String,
    var text: String,
    var sortOrder: Int,
    var modifiedAt: Long,
) {
    companion object {
        private val gson = Gson()
        private val itemListType = object : TypeToken<List<SavedStringItem>>() {}.type
        private val legacyListType = object : TypeToken<List<String>>() {}.type

        fun decodeLocal(json: String): List<SavedStringItem> {
            val asItems = runCatching { gson.fromJson<List<SavedStringItem>>(json, itemListType) }.getOrNull()
            if (asItems != null) return asItems

            val legacy = runCatching { gson.fromJson<List<String>>(json, legacyListType) }.getOrNull()
                ?: return emptyList()
            val now = System.currentTimeMillis()
            return legacy.mapIndexed { index, text ->
                SavedStringItem(id = UUID.randomUUID().toString(), text = text, sortOrder = index, modifiedAt = now)
            }
        }

        fun encodeLocal(items: List<SavedStringItem>): String = gson.toJson(items)
    }
}
