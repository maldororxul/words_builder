package com.example.wordsbuilder.data

import CampaignLevel
import android.content.Context
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.InputStreamReader
import androidx.core.content.edit

object LevelManager {

    // Загрузка всего списка уровней из JSON
    fun loadCampaignLevels(context: Context, langCode: String): List<CampaignLevel> {
        val fileName = "levels_$langCode.json"
        return try {
            context.assets.open(fileName).use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val type = object : TypeToken<List<CampaignLevel>>() {}.type
                Gson().fromJson(reader, type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getAllAvailableMediaResources(context: Context): List<Pair<String, String>> {
        return try {
            val jsonString = context.assets.open("levels_en.json").bufferedReader().use { it.readText() }
            val listType = object : com.google.gson.reflect.TypeToken<List<CampaignLevel>>() {}.type
            val levels: List<CampaignLevel> = com.google.gson.Gson().fromJson(jsonString, listType)
            levels.map { it.bgRes to it.musicRes }
        } catch (e: Exception) {
            listOf("bg_forest" to "music_cozy_forest") // Фоллбэк
        }
    }

    // Получение количества уровней в кампании
    fun getCampaignLevelsCount(context: Context, langCode: String): Int {
        return loadCampaignLevels(context, langCode).size
    }

    // Получение конкретного уровня по его ID
    fun getCampaignLevel(context: Context, langCode: String, levelId: Int): CampaignLevel? {
        val levels = loadCampaignLevels(context, langCode)
        return levels.find { it.id == levelId } ?: levels.firstOrNull()
    }

    // --- Оригинальная логика сохранений в SharedPreferences без изменений ---
    fun saveCampaignSolvedWordsJson(context: Context, langCode: String, json: String) {
        val prefs = context.getSharedPreferences("words_builder_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("campaign_solved_words_$langCode", json) }
    }

    fun getCampaignSolvedWords(context: Context, langCode: String): Set<String> {
        val prefs = context.getSharedPreferences("words_builder_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("campaign_solved_words_$langCode", null) ?: return emptySet()
        return try {
            Gson().fromJson(json, object : TypeToken<Set<String>>() {}.type)
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun saveRandomSolvedWordsJson(context: Context, json: String) {
        val prefs = context.getSharedPreferences("words_builder_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("random_solved_words", json) }
    }

    fun getRandomSolvedWords(context: Context): Set<String> {
        val prefs = context.getSharedPreferences("words_builder_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("random_solved_words", null) ?: return emptySet()
        return try {
            Gson().fromJson(json, object : TypeToken<Set<String>>() {}.type)
        } catch (e: Exception) {
            emptySet()
        }
    }
}