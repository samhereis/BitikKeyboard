package com.shoktuk.shoktukkeyboard.project.data

import androidx.activity.ComponentActivity
import com.google.gson.Gson

object SavedStringsCloudStore {
    private val gson = Gson()

    private fun filePrefix(script: WritingSystem) = "saved_string_${script.name}_"

    suspend fun fetchAll(activity: ComponentActivity, script: WritingSystem): List<SavedStringItem>? {
        val names = DriveClient.listFiles(activity, filePrefix(script)) ?: return null
        return names.mapNotNull { name ->
            val json = DriveClient.readFile(activity, name) ?: return@mapNotNull null
            runCatching { gson.fromJson(json, SavedStringItem::class.java) }.getOrNull()
        }
    }

    suspend fun save(activity: ComponentActivity, script: WritingSystem, items: List<SavedStringItem>) {
        for (item in items) {
            DriveClient.writeFile(activity, fileName(script, item.id), gson.toJson(item))
        }
    }

    suspend fun delete(activity: ComponentActivity, script: WritingSystem, ids: List<String>) {
        for (id in ids) {
            DriveClient.deleteFile(activity, fileName(script, id))
        }
    }

    fun merge(local: List<SavedStringItem>, cloud: List<SavedStringItem>): List<SavedStringItem> {
        val byId = local.associateByTo(LinkedHashMap()) { it.id }
        for (item in cloud) {
            val existing = byId[item.id]
            if (existing == null || item.modifiedAt > existing.modifiedAt) {
                byId[item.id] = item
            }
        }
        return byId.values.sortedBy { it.sortOrder }
    }

    private fun fileName(script: WritingSystem, id: String) = "${filePrefix(script)}$id.json"
}
