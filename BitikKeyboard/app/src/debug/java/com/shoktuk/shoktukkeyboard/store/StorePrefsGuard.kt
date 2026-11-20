package com.shoktuk.shoktukkeyboard.store

import android.content.Context
import android.content.SharedPreferences
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * The store banners write the app's own saved settings (so the real keyboard draws with them). This keeps the person's real
 * settings safe: the settings live in memory while the banners show ([InMemoryPrefs] in `SettingsManager.prefsOverride`), and the
 * few other saved files the keyboard writes (emoji recents, language) are copied by [begin] (never over an older, still-unrestored copy); [restore] puts them back and deletes the copy. Debug builds only.
 */
object StorePrefsGuard {
    private val files = listOf("settings_prefs", "emoji_prefs", "LocalizationPrefs")
    private fun backup(context: Context) = File(context.filesDir, "store_prefs_backup.json")

    @Synchronized
    fun begin(context: Context) {
        val app = context.applicationContext ?: context
        // The app's settings are read and written in memory while the banners show: the saved ones are not touched at all.
        SettingsManager.prefsOverride = InMemoryPrefs()
        if (backup(app).exists()) return  // an earlier run did not get to restore: that copy holds the real settings
        val all = JSONObject()
        for (name in files) {
            val entries = JSONArray()
            for ((key, value) in app.getSharedPreferences(name, Context.MODE_PRIVATE).all) {
                val type = when (value) {
                    is String -> "s"; is Int -> "i"; is Long -> "l"; is Float -> "f"; is Boolean -> "b"
                    is Set<*> -> "set"; else -> continue
                }
                val stored: Any = if (value is Set<*>) JSONArray(value.map { it.toString() }) else value!!
                entries.put(JSONObject().put("k", key).put("t", type).put("v", stored))
            }
            all.put(name, entries)
        }
        backup(app).writeText(all.toString())
    }

    @Synchronized
    fun restore(context: Context) {
        val app = context.applicationContext ?: context
        SettingsManager.prefsOverride = null
        val file = backup(app)
        if (!file.exists()) return
        val all = runCatching { JSONObject(file.readText()) }.getOrNull() ?: return
        for (name in files) {
            val entries = all.optJSONArray(name) ?: continue
            val edit = app.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear()
            for (i in 0 until entries.length()) {
                val e = entries.getJSONObject(i)
                val key = e.getString("k")
                when (e.getString("t")) {
                    "s" -> edit.putString(key, e.getString("v"))
                    "i" -> edit.putInt(key, e.getInt("v"))
                    "l" -> edit.putLong(key, e.getLong("v"))
                    "f" -> edit.putFloat(key, e.getDouble("v").toFloat())
                    "b" -> edit.putBoolean(key, e.getBoolean("v"))
                    "set" -> edit.putStringSet(key, e.getJSONArray("v").let { a -> (0 until a.length()).map { a.getString(it) }.toSet() })
                }
            }
            edit.commit()
        }
        file.delete()
    }
}

/** A SharedPreferences that lives only in memory (starts empty, so the keyboard shows its defaults plus what the banner sets). */
class InMemoryPrefs : SharedPreferences {
    private val data = HashMap<String, Any?>()
    private val listeners = HashSet<SharedPreferences.OnSharedPreferenceChangeListener>()

    @Synchronized override fun getAll(): MutableMap<String, *> = HashMap(data)
    @Synchronized override fun getString(key: String, defValue: String?): String? = data[key] as? String ?: defValue
    @Suppress("UNCHECKED_CAST")
    @Synchronized override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? = (data[key] as? Set<String>)?.toMutableSet() ?: defValues
    @Synchronized override fun getInt(key: String, defValue: Int): Int = data[key] as? Int ?: defValue
    @Synchronized override fun getLong(key: String, defValue: Long): Long = data[key] as? Long ?: defValue
    @Synchronized override fun getFloat(key: String, defValue: Float): Float = data[key] as? Float ?: defValue
    @Synchronized override fun getBoolean(key: String, defValue: Boolean): Boolean = data[key] as? Boolean ?: defValue
    @Synchronized override fun contains(key: String): Boolean = data.containsKey(key)
    override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener) { listeners += l }
    override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener) { listeners -= l }

    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private val pending = HashMap<String, Any?>()
        private var clear = false
        private val removed = HashSet<String>()
        override fun putString(key: String, value: String?) = apply { pending[key] = value }
        override fun putStringSet(key: String, values: MutableSet<String>?) = apply { pending[key] = values?.toSet() }
        override fun putInt(key: String, value: Int) = apply { pending[key] = value }
        override fun putLong(key: String, value: Long) = apply { pending[key] = value }
        override fun putFloat(key: String, value: Float) = apply { pending[key] = value }
        override fun putBoolean(key: String, value: Boolean) = apply { pending[key] = value }
        override fun remove(key: String) = apply { removed += key }
        override fun clear() = apply { clear = true }
        override fun commit(): Boolean { apply(); return true }
        override fun apply() {
            val changed = synchronized(this@InMemoryPrefs) {
                if (clear) data.clear()
                removed.forEach { data.remove(it) }
                for ((k, v) in pending) if (v == null) data.remove(k) else data[k] = v
                pending.keys + removed
            }
            changed.forEach { key -> listeners.toList().forEach { it.onSharedPreferenceChanged(this@InMemoryPrefs, key) } }
        }
    }
}
