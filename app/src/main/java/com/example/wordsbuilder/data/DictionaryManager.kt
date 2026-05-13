import android.content.Context
import org.json.JSONArray

object DictionaryManager {
    fun loadDictionary(context: Context, langCode: String): List<String> {
        // Определяем, какой файл открыть
        val fileName = when (langCode) {
            "ru" -> "words_ru.json"
            "es" -> "words_es.json"
            else -> "words_en.json" // English по умолчанию
        }

        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            List(jsonArray.length()) { jsonArray.getString(it).uppercase() }
        } catch (e: Exception) {
            android.util.Log.e("DICT", "Ошибка загрузки словаря $langCode: ${e.message}")
            emptyList()
        }
    }
}