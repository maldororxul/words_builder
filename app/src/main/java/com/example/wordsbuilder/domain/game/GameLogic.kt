import android.content.Context
import androidx.core.content.edit
import com.example.wordsbuilder.R
import com.example.wordsbuilder.data.StatsManager

fun saveScore(context: Context, score: Int) {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    prefs.edit { putInt("total_score", score) }
}

fun getSavedScore(context: Context): Int {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    return prefs.getInt("total_score", 0)
}

// Сохранение и получение номера уровня
fun saveCampaignLevelIndex(context: Context, langCode: String, levelId: Int) {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    prefs.edit { putInt("campaign_level_$langCode", levelId) }
}

fun getSavedCampaignLevelIndex(context: Context, langCode: String): Int {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    return prefs.getInt("campaign_level_$langCode", 1) // по умолчанию 1 уровень
}

// Сохранение и получение разгаданных слов раунда
fun saveCurrentLevelProgress(context: Context, solvedWords: Set<String>) {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    val joinedWords = solvedWords.joinToString(",")
    prefs.edit { putString("current_level_progress", joinedWords) }
}

fun getSavedLevelProgress(context: Context): Set<String> {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    val savedString = prefs.getString("current_level_progress", "") ?: ""
    return if (savedString.isEmpty()) emptySet() else savedString.split(",").toSet()
}

fun saveCoins(context: Context, amount: Int) {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    prefs.edit { putInt("game_coins", amount) }
}

fun getSavedCoins(context: Context): Int {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    return prefs.getInt("game_coins", 100) // 100 стартовых монет игроку
}

fun handleWordInput(
    input: String,
    targetWords: List<String>,
    solvedWords: Set<String>,
    gameMode: String,
    context: Context,
    onSolvedUpdate: (Set<String>) -> Unit,
    onScoreUpdate: (Int) -> Unit,           // сюда передаём delta
    onCurrentWordChange: (String) -> Unit
): Boolean {
    if (input.startsWith("CHECK:")) {
        val word = input.removePrefix("CHECK:")

        if (targetWords.contains(word) && !solvedWords.contains(word)) {
            val pointsToAdd = word.length

            // Начисляем очки
            onScoreUpdate(pointsToAdd)

            val newSolved = solvedWords + word
            onSolvedUpdate(newSolved)

            // Сохраняем прогресс для кампании
            if (gameMode == "campaign") {
                saveCurrentLevelProgress(context, newSolved)
            }

            SoundManager.playSound(context, R.raw.success)
            val statsManager = StatsManager(context)
            // 1. Плюс одно разгаданное слово в общий счетчик
            statsManager.totalWordsSolved += 1
            // 2. Добавляем слово в список уникальных (метод сам отсечет дубликаты)
            statsManager.addUniqueWord(word)
            // 3. Рассчитываем очки: длина слова * 10. Если это кампания — даем бонус x2 (длина * 20)
            val scoreEarned = if (gameMode == "campaign") (word.length * 20) else (word.length * 10)

            statsManager.totalScore += scoreEarned

            if (newSolved.size == targetWords.size) {
                SoundManager.playSound(context, R.raw.victory)
            }
            return true
        }
        else if (word.length > 1) {
            SoundManager.playSound(context, R.raw.error)
        }

        onCurrentWordChange("")
    } else {
        onCurrentWordChange(input)
    }
    return false
}

// Сохранение количества слов для случайного режима (по умолчанию 5)
fun saveRandomWordsCount(context: Context, count: Int) {
    val prefs = context.getSharedPreferences("words_builder_prefs", Context.MODE_PRIVATE)
    prefs.edit { putInt("random_words_count", count) }
}

// Чтение количества слов для случайного режима
fun getRandomWordsCount(context: Context): Int {
    val prefs = context.getSharedPreferences("words_builder_prefs", Context.MODE_PRIVATE)
    return prefs.getInt("random_words_count", 5) // 5 — дефолтное значение
}

fun getResourceIdentifier(context: Context, resName: String, defType: String): Int {
    val id = context.resources.getIdentifier(resName, defType, context.packageName)
    return if (id != 0) id else {
        // Дефолтные значения, если ресурс не найден
        when (defType) {
            "drawable" -> android.R.drawable.ic_dialog_alert
            "raw" -> 0 // логика тишины или стандартного трека
            else -> 0
        }
    }
}