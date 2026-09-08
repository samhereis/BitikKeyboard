package com.shoktuk.shoktukkeyboard.project.data

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.core.content.edit
import com.google.gson.Gson

private const val PREFS_NAME = "settings_prefs"
private const val META_PREFS_NAME = "cloud_sync_meta"
private const val DRIVE_FILE_NAME = "settings.json"
private const val BOOTSTRAP_KEY = "cloudSettingsBootstrapped_v1"

private val intSettingKeys = setOf("buttonHeight", "bottomOffset")

val cloudSyncedKeys: List<String> = listOf(
    "buttonHeight", "bottomOffset", "freeTamga_Click", "freeTamga_Hold",
    BitikVariant.KEY, BitikDialect.KEY, TextTranscription.KEY, LetterTranscription.KEY, WordSeparator.KEY,
    AJ_Letter_Variant.KEY, ANG_Letter_Variant.KEY, EB_Letter_Variant.KEY, EN_Letter_Variant.KEY,
    AS_Letter_Variant.KEY, EK_Letter_Variant.KEY, ESH_Letter_Variant.KEY,
    Latin_Status.KEY, Latin_Variant.KEY, Latin_ZH.KEY, Latin_NG.KEY, Latin_O.KEY, Arabic_Status.KEY, Kirilisa_Status.KEY,
    Coloring.KEY, HoldabilityColoring.KEY, Vibrations.KEY, Sounds.KEY, WritingSystem.KEY,
    NavBarPaddingSolution.KEY,
)

private data class SettingsPayload(
    val values: Map<String, String> = emptyMap(),
    val modifiedAt: Map<String, Long> = emptyMap(),
)

object SettingsCloudSync {
    private val gson = Gson()

    @Volatile
    private var isApplyingRemoteChange = false

    private fun Context.settingsPrefs() = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private fun Context.metaPrefs() = getSharedPreferences(META_PREFS_NAME, Context.MODE_PRIVATE)

    private fun readLocal(context: Context, key: String): String? {
        val prefs = context.settingsPrefs()
        if (!prefs.contains(key)) return null
        return if (key in intSettingKeys) prefs.getInt(key, 0).toString() else prefs.getString(key, null)
    }

    private fun writeLocal(context: Context, key: String, value: String) {
        context.settingsPrefs().edit {
            if (key in intSettingKeys) value.toIntOrNull()?.let { putInt(key, it) } else putString(key, value)
        }
    }

    private fun localModifiedAt(context: Context, key: String): Long =
        context.metaPrefs().getLong("modifiedAt_$key", 0L)

    private fun setLocalModifiedAt(context: Context, key: String, at: Long) {
        context.metaPrefs().edit { putLong("modifiedAt_$key", at) }
    }

    fun isApplyingRemote(): Boolean = isApplyingRemoteChange

    suspend fun onLocalChange(activity: ComponentActivity, key: String) {
        if (isApplyingRemoteChange || key !in cloudSyncedKeys) return
        setLocalModifiedAt(activity, key, System.currentTimeMillis())
        push(activity)
    }

    suspend fun push(activity: ComponentActivity) {
        val cloud = downloadPayload(activity) ?: SettingsPayload()
        val mergedValues = cloud.values.toMutableMap()
        val mergedModifiedAt = cloud.modifiedAt.toMutableMap()

        for (key in cloudSyncedKeys) {
            val localValue = readLocal(activity, key) ?: continue
            val localAt = localModifiedAt(activity, key)
            val cloudAt = cloud.modifiedAt[key] ?: 0L
            if (localAt >= cloudAt) {
                mergedValues[key] = localValue
                mergedModifiedAt[key] = localAt
            }
        }

        uploadPayload(activity, SettingsPayload(mergedValues, mergedModifiedAt))
    }

    suspend fun pull(activity: ComponentActivity) {
        val cloud = downloadPayload(activity) ?: return
        isApplyingRemoteChange = true
        try {
            for ((key, cloudValue) in cloud.values) {
                if (key !in cloudSyncedKeys) continue
                val cloudAt = cloud.modifiedAt[key] ?: 0L
                if (cloudAt > localModifiedAt(activity, key)) {
                    writeLocal(activity, key, cloudValue)
                    setLocalModifiedAt(activity, key, cloudAt)
                }
            }
        } finally {
            isApplyingRemoteChange = false
        }
    }

    suspend fun bootstrapIfNeeded(activity: ComponentActivity) {
        val meta = activity.metaPrefs()
        if (meta.getBoolean(BOOTSTRAP_KEY, false)) return
        meta.edit { putBoolean(BOOTSTRAP_KEY, true) }

        if (downloadPayload(activity) != null) return
        push(activity)
    }

    suspend fun cloudSettingCount(activity: ComponentActivity): Int? =
        downloadPayload(activity)?.values?.keys?.count { it in cloudSyncedKeys }

    private suspend fun downloadPayload(activity: ComponentActivity): SettingsPayload? {
        val json = DriveClient.readFile(activity, DRIVE_FILE_NAME) ?: return null
        return runCatching { gson.fromJson(json, SettingsPayload::class.java) }.getOrNull()
    }

    private suspend fun uploadPayload(activity: ComponentActivity, payload: SettingsPayload) {
        DriveClient.writeFile(activity, DRIVE_FILE_NAME, gson.toJson(payload))
    }
}
