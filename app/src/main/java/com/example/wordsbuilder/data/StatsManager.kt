package com.example.wordsbuilder.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit


class StatsManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

    private val KEY_TOTAL_SCORE = "stat_total_score"
    private val KEY_WORDS_SOLVED = "stat_words_solved"
    private val KEY_UNIQUE_WORDS = "stat_unique_words"
    private val KEY_HINTS_USED = "stat_hints_used"

    // 0. Очков заработано
    var totalScore: Int
        get() = prefs.getInt(KEY_TOTAL_SCORE, 0)
        set(value) = prefs.edit { putInt(KEY_TOTAL_SCORE, value) }

    // 1. Всего слов разгадано
    var totalWordsSolved: Int
        get() = prefs.getInt(KEY_WORDS_SOLVED, 0)
        set(value) = prefs.edit { putInt(KEY_WORDS_SOLVED, value) }

    // 2. Уникальных слов разгадано (храним в виде JSON-сета строк)
    fun addUniqueWord(word: String) {
        val uniqueWords = getUniqueWords().toMutableSet()
        if (uniqueWords.add(word.lowercase())) {
            val json = Gson().toJson(uniqueWords)
            prefs.edit { putString(KEY_UNIQUE_WORDS, json) }
        }
    }

    fun getUniqueWordsCount(): Int = getUniqueWords().size

    private fun getUniqueWords(): Set<String> {
        val json = prefs.getString(KEY_UNIQUE_WORDS, null) ?: return emptySet()
        return try {
            Gson().fromJson(json, object : TypeToken<Set<String>>() {}.type)
        } catch (e: Exception) {
            emptySet()
        }
    }

    // 3. Подсказок взято
    var hintsUsed: Int
        get() = prefs.getInt(KEY_HINTS_USED, 0)
        set(value) = prefs.edit { putInt(KEY_HINTS_USED, value) }
}
