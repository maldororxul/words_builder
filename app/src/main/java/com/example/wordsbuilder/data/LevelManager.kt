package com.example.wordsbuilder.data

import CampaignLevel
import android.content.Context
import android.util.Log
import org.json.JSONArray

object LevelManager {
    // Загрузка всех уровней кампании для выбранного языка
    fun loadCampaignLevels(context: Context, langCode: String): List<CampaignLevel> {
        val fileName = when (langCode) {
            "ru" -> "levels_ru.json"
            "es" -> "levels_es.json" // добавьте файл, если нужен испанский
            else -> "levels_en.json"
        }
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                val wordsArray = obj.getJSONArray("words")
                val wordsList = List(wordsArray.length()) { wordsArray.getString(it).uppercase() }
                CampaignLevel(
                    id = obj.getInt("id"),
                    reward = obj.optInt("reward", 50), // 50 по умолчанию, если поля нет
                    hintCost = obj.optInt("hint_cost", 20),
                    words = wordsList
                )
            }
        } catch (e: Exception) {
            Log.e("LEVEL_MANAGER", "Ошибка загрузки кампании: ${e.message}")
            emptyList()
        }
    }

    fun getCampaignLevelsCount(context: Context, langCode: String): Int {
        return loadCampaignLevels(context, langCode).size
    }
}