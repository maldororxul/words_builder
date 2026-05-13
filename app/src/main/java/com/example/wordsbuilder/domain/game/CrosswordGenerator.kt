import android.util.Log

fun generateCrossword(words: List<String>): List<PlacedWord> {
    Log.d("CROSSWORD_GEN", "Начало генерации. Входящие слова: ${words.joinToString(", ")}")

    if (words.isEmpty()) {
        Log.e("CROSSWORD_GEN", "Список слов пуст!")
        return emptyList()
    }

    var bestResult = emptyList<PlacedWord>()

    for (attempt in 0 until 300) {
        val currentPlaced = mutableListOf<PlacedWord>()
        val gridMap = mutableMapOf<Pair<Int, Int>, Char>()

        // Всегда начинаем с самого длинного слова для стабильности
        val firstWordStr = words[0]
        val first = PlacedWord(firstWordStr, 0, 0, true)
        currentPlaced.add(first)
        placeWord(first, gridMap)

        // Пытаемся добавить остальные
        for (i in 1 until words.size) {
            val word = words[i]
            val placed = findPositionForWord(word, currentPlaced, gridMap)
            if (placed != null) {
                currentPlaced.add(placed)
                placeWord(placed, gridMap)
            }
        }

        if (currentPlaced.size > bestResult.size) {
            bestResult = currentPlaced.toList()
            Log.d("CROSSWORD_GEN", "Попытка $attempt: удалось разместить ${bestResult.size} слов")
        }

        if (bestResult.size == words.size) {
            Log.d("CROSSWORD_GEN", "Успех! Все слова размещены на попытке $attempt")
            break
        }
    }

    if (bestResult.size < 2 && words.size > 1) {
        Log.w("CROSSWORD_GEN", "ВНИМАНИЕ: Не удалось создать пересечения. Размещено только ${bestResult.size} слов.")
    }

    return bestResult
}

fun placeWord(pw: PlacedWord, grid: MutableMap<Pair<Int, Int>, Char>) {
    pw.word.forEachIndexed { i, c ->
        val x = if (pw.isHorizontal) pw.x + i else pw.x
        val y = if (pw.isHorizontal) pw.y else pw.y + i
        grid[Pair(x, y)] = c
    }
}

fun findPositionForWord(
    word: String,
    placedWords: List<PlacedWord>,
    gridMap: Map<Pair<Int, Int>, Char>
): PlacedWord? {
    val candidates = mutableListOf<PlacedWord>()

    for (existing in placedWords) {
        for (i in word.indices) {             // Индекс буквы в НОВОМ слове
            for (j in existing.word.indices) { // Индекс буквы в СУЩЕСТВУЮЩЕМ
                if (word[i] != existing.word[j]) continue

                val isNewHorizontal = !existing.isHorizontal

                // Исправленная математика:
                val newX = if (existing.isHorizontal)
                    existing.x + j else existing.x - i
                val newY = if (existing.isHorizontal)
                    existing.y - i else existing.y + j

                if (canPlaceWord(word, newX, newY, isNewHorizontal, gridMap)) {
                    candidates.add(PlacedWord(word, newX, newY, isNewHorizontal))
                }
            }
        }
    }
    return candidates.randomOrNull()
}

private fun canPlaceWord(
    word: String,
    startX: Int,
    startY: Int,
    horizontal: Boolean,
    gridMap: Map<Pair<Int, Int>, Char>
): Boolean {
    // 1. Проверяем торцы слова (клетки прямо перед и прямо после слова должны быть пустыми)
    val beforeX = if (horizontal) startX - 1 else startX
    val beforeY = if (horizontal) startY else startY - 1
    val afterX = if (horizontal) startX + word.length else startX
    val afterY = if (horizontal) startY else startY + word.length

    if (gridMap.containsKey(Pair(beforeX, beforeY)) || gridMap.containsKey(Pair(afterX, afterY))) return false

    // 2. Проверяем каждую букву
    for (i in word.indices) {
        val cx = if (horizontal) startX + i else startX
        val cy = if (horizontal) startY else startY + i
        val pos = Pair(cx, cy)

        // Конфликт букв в точке пересечения
        if (gridMap.containsKey(pos) && gridMap[pos] != word[i]) return false

        // Проверяем боковых соседей (они должны быть пустыми, если это не точка пересечения)
        if (!gridMap.containsKey(pos)) {
            val sideNeighbors = if (horizontal) {
                listOf(Pair(cx, cy - 1), Pair(cx, cy + 1))
            } else {
                listOf(Pair(cx - 1, cy), Pair(cx + 1, cy))
            }
            if (sideNeighbors.any { gridMap.containsKey(it) }) return false
        }
    }
    return true
}