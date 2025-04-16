import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

enum class Language(val code: String) {
    KY("ky"), KZ("kz"), TR("tr"), EN("en"), RU("ru")
}

fun String.localized(csvFileName: String = "Localizations", context: Context): String {
    return LocalizationManager.localizedString(key = this, csvFileName = csvFileName, context = context)
}

object LocalizationManager {

    var currentLanguage by mutableStateOf(Language.KZ)
    private val csvCache = mutableMapOf<String, Map<String, Map<Language, String>>>()

    private const val PREFS_NAME = "LocalizationPrefs"
    private const val KEY_LANGUAGE = "current_language"

    // Call this once, for example in your Application onCreate
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLangCode = prefs.getString(KEY_LANGUAGE, Language.KY.code)
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
            context.assets.open("$csvFileName.csv").use { inputStream ->
                // Use the InputStream overload of readAllWithHeader
                val rows: List<Map<String, String>> = csvReader().readAllWithHeader(inputStream)
                val translations = mutableMapOf<String, Map<Language, String>>()
                for (row in rows) {
                    // "key" column must exist in your CSV header
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
