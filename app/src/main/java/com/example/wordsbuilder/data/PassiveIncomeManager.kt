package com.example.wordsbuilder.data
import android.content.Context
import org.json.JSONObject
import androidx.core.content.edit

class PassiveIncomeManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    private val KEY_LAST_TIMESTAMP = "passive_last_timestamp"

    var coinsPerSecond: Double = 0.01
    var maxCoinsLimit: Int = 50

    init {
        loadConfigFromAssets()
        // Если игра запускается САМЫЙ первый раз, фиксируем стартовый таймстемп
        if (prefs.getLong(KEY_LAST_TIMESTAMP, 0L) == 0L) {
            resetTimestamp()
        }
    }

    private fun loadConfigFromAssets() {
        try {
            val jsonString = context.assets.open("passive_income.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            coinsPerSecond = jsonObject.optDouble("coins_per_second", 0.01)
            maxCoinsLimit = jsonObject.optInt("max_coins_limit", 50)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Возвращает точное (дробное) количество накопившихся монет
    fun calculateAccumulatedCoins(): Double {
        val lastTime = prefs.getLong(KEY_LAST_TIMESTAMP, System.currentTimeMillis())
        val currentTime = System.currentTimeMillis()

        // Разница в секундах
        val secondsPassed = (currentTime - lastTime) / 1000.0
        val earned = secondsPassed * coinsPerSecond

        return earned.coerceAtMost(maxCoinsLimit.toDouble())
    }

    // Сброс таймстемпа на текущее системное время
    fun resetTimestamp() {
        prefs.edit { putLong(KEY_LAST_TIMESTAMP, System.currentTimeMillis()) }
    }
}
