package com.example.wordsbuilder.domain.game

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import kotlin.math.max

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
    fun getRandomWordsMap(context: Context, langCode: String, targetCount: Int): Map<String, String> {
        val dictionary = loadFullDictionary(context, langCode)
        if (dictionary.isEmpty()) return emptyMap()

        val validWords = dictionary.toList().filter { it.first.length in 3..10 }
        if (validWords.isEmpty()) return emptyMap()

        val longWordsPool = validWords.filter { it.first.length >= 5 }.shuffled()
        val finalSelection = mutableListOf<Pair<String, String>>()

        var attempts = 0
        val maxAttempts = 500

        while (attempts < maxAttempts && finalSelection.size < targetCount) {
            attempts++
            finalSelection.clear()

            // Карта максимальных потребностей в буквах для ВСЕГО набора слов
            val currentWheelRequirements = mutableMapOf<Char, Int>()

            // 1. Берем случайное стартовое слово
            val anchorWord = longWordsPool.randomOrNull() ?: validWords.random()
            finalSelection.add(anchorWord)

            // Считаем буквы первого слова
            anchorWord.first.lowercase().forEach { char ->
                currentWheelRequirements[char] = currentWheelRequirements.getOrDefault(char, 0) + 1
            }

            val candidateWords = validWords.filter { it.first != anchorWord.first }.shuffled()

            for (pair in candidateWords) {
                if (finalSelection.size >= targetCount) break

                val wordLower = pair.first.lowercase()

                // Считаем частоту букв в слове-кандидате
                val candidateFreq = mutableMapOf<Char, Int>()
                wordLower.forEach { char ->
                    candidateFreq[char] = candidateFreq.getOrDefault(char, 0) + 1
                }

                // Вычисляем, какими станут требования к колесу (мультисет максимумов)
                val tempRequirements = HashMap(currentWheelRequirements)
                candidateFreq.forEach { (char, count) ->
                    val currentMax = tempRequirements.getOrDefault(char, 0)
                    tempRequirements[char] = max(currentMax, count) // Берём МАКСИМУМ, а не сумму!
                }

                // Считаем общее физическое количество букв на колесе
                val totalLettersOnWheel = tempRequirements.values.sum()

                // ЖЕСТКИЙ КОНТРОЛЬ ТЗ: Общее число букв (с учетом дубликатов) НЕ ДОЛЖНО превысить 15
                if (totalLettersOnWheel <= 15) {
                    finalSelection.add(pair)
                    // Обновляем глобальные требования к колесу
                    candidateFreq.forEach { (char, count) ->
                        currentWheelRequirements[char] = max(currentWheelRequirements.getOrDefault(char, 0), count)
                    }
                }
            }

            if (finalSelection.size == targetCount) {
                return finalSelection.toMap()
            }
        }

        val fallback = validWords.shuffled().take(targetCount)
        return fallback.toMap()
    }
}
