package com.example.wordsbuilder.domain.game

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

object RandomWordGenerator {

    // Чтение всего словаря слов с определениями из words_XX.json
    fun loadFullDictionary(context: Context, langCode: String): Map<String, String> {
        val fileName = "words_$langCode.json"
        return try {
            context.assets.open(fileName).use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val type = object : TypeToken<Map<String, String>>() {}.type
                Gson().fromJson(reader, type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    // Выборка N случайных слов с их определениями для случайного уровня
    fun getRandomWordsMap(context: Context, langCode: String, count: Int): Map<String, String> {
        val dictionary = loadFullDictionary(context, langCode)
        if (dictionary.isEmpty()) return emptyMap()

        // Перемешиваем ключи и берем нужное количество
        val shuffledKeys = dictionary.keys.shuffled().take(count)

        // Строим отфильтрованный словарь из выбранных случайных ключей
        return dictionary.filterKeys { shuffledKeys.contains(it) }
    }
}
