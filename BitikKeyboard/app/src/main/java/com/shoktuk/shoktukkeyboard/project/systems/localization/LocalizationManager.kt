import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

enum class Language(val code: String, val displayName: String) {
    KY_L("ky_latin", "🇰🇬 Qırğız"),
    KY_K("ky_kiril", "🇰🇬 Кыргыз"),
    TR("tr", "🇹🇷 Türkçe"),
    KZ("kz", "🇰🇿 Қазақша"),
    AZ("az", "🇦🇿 Azərbaycan"),
    EN("en", "🇺🇸 English"),
    RU("ru", "🇷🇺 Русский")
}

fun String.localized(csvFileName: String = "Localizations", context: Context): String {
    return LocalizationManager.localizedString(key = this, csvFileName = csvFileName, context = context)
}

object LocalizationManager {
    var currentLanguage by mutableStateOf(Language.KY_K)
    private val csvCache = mutableMapOf<String, Map<String, Map<Language, String>>>()

    private const val PREFS_NAME = "LocalizationPrefs"
    private const val KEY_LANGUAGE = "current_language"

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLangCode = prefs.getString(KEY_LANGUAGE, Language.KY_K.code)
        savedLangCode?.let { langCode ->
            Language.entries.find { it.code == langCode }?.let {
                currentLanguage = it
            }
        }
    }

    fun setLanguage(context: Context, newLanguage: Language) {
        currentLanguage = newLanguage
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_LANGUAGE, newLanguage.code) }
    }

    fun localizedString(key: String, csvFileName: String, context: Context): String {
        if (!csvCache.containsKey(csvFileName)) {
            loadLocalizations(csvFileName, context)
        }
        val translations = csvCache[csvFileName]
        val translationForKey = translations?.get(key)
        return translationForKey?.get(currentLanguage) ?: key
    }

    private fun loadLocalizations(csvFileName: String, context: Context) {
        try {
            context.assets.open("localization/$csvFileName.csv").use { inputStream ->
                val rows: List<Map<String, String>> = csvReader().readAllWithHeader(inputStream)
                val translations = mutableMapOf<String, Map<Language, String>>()
                for (row in rows) {
                    val key = row["key"] ?: continue
                    val rowMap = mutableMapOf<Language, String>()
                    for (lang in Language.entries) {
                        row[lang.code]?.let { translation ->
                            rowMap[lang] = translation
                        }
                    }
                    translations[key] = rowMap
                }
                csvCache[csvFileName] = translations
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
