import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min

@Composable
fun CrosswordGrid(
    placedWords: List<PlacedWord>,
    solvedWords: Set<String>,
    modifier: Modifier = Modifier
) {
    if (placedWords.isEmpty()) {
        Text("Failed to generate crossword!", color = Color.White)
        return
    }

    // Находим границы (Оригинальная логика)
    val allX = placedWords.flatMap { pw ->
        pw.word.indices.map { i -> if (pw.isHorizontal) pw.x + i else pw.x }
    }
    val allY = placedWords.flatMap { pw ->
        pw.word.indices.map { i -> if (pw.isHorizontal) pw.y else pw.y + i }
    }
    val minX = allX.minOrNull() ?: 0
    val maxX = allX.maxOrNull() ?: 0
    val minY = allY.minOrNull() ?: 0
    val maxY = allY.maxOrNull() ?: 0
    val cols = maxX - minX + 1
    val rows = maxY - minY + 1

    // Состояние масштаба (от 1.0х до 2.5х)
    val scaleState = remember { androidx.compose.runtime.mutableFloatStateOf(1f) }

    // Стейты прокрутки кроссворда
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    // Отслеживаем только щипок (зум), игнорируя перемещения
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        scaleState.floatValue = (scaleState.floatValue * zoomChange).coerceIn(1.0f, 2.5f)
    }

    // Внешний контейнер-рамка, перехватывающий жесты
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = transformState)
    ) {
        // Базовый размер ячейки под текущий экран (Оригинальная логика)
        val baseCellSize = min(
            maxWidth / cols.coerceAtLeast(1),
            maxHeight / rows.coerceAtLeast(1)
        ) * 0.95f

        // КРИТИЧЕСКОЕ ИЗМЕНЕНИЕ 1: Рассчитываем размер ячейки линейно на основе зума.
        // Теперь размер физически увеличивается/уменьшается в реальном времени.
        val cellSize = baseCellSize * scaleState.floatValue

        // Создаём карту всех клеток (Оригинальная логика)
        val cellsMap = mutableMapOf<Pair<Int, Int>, Char>()
        placedWords.forEach { pw ->
            pw.word.forEachIndexed { i, char ->
                val x = if (pw.isHorizontal) pw.x + i else pw.x
                val y = if (pw.isHorizontal) pw.y else pw.y + i
                cellsMap[Pair(x, y)] = char
            }
        }

        // КРИТИЧЕСКОЕ ИЗМЕНЕНИЕ 2: Оборачиваем холст в контейнеры прокрутки.
        // Они активируются, только если размер холста (cellSize * cols) становится больше экрана.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .verticalScroll(verticalScrollState),
            contentAlignment = Alignment.Center // Всегда держит кроссворд строго по центру
        ) {
            // Внутренний контейнер сетки, размер которого честно зависит от нового cellSize
            Box(
                modifier = Modifier.size(cellSize * cols, cellSize * rows)
            ) {
                cellsMap.forEach { (coords, char) ->
                    val (cx, cy) = coords
                    val gridX = cx - minX
                    val gridY = cy - minY

                    val isVisible = placedWords.any { pw ->
                        solvedWords.contains(pw.word) &&
                                pw.word.indices.any { i ->
                                    val px = if (pw.isHorizontal) pw.x + i else pw.x
                                    val py = if (pw.isHorizontal) pw.y else pw.y + i
                                    px == cx && py == cy
                                }
                    }

                    Box(
                        modifier = Modifier
                            // Смещение ячеек автоматически пересчитывается за счет динамического cellSize
                            .offset(x = cellSize * gridX, y = cellSize * gridY)
                            .size(cellSize)
                            .background(Color.White.copy(alpha = 0.95f))
                            .border(1.dp, Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isVisible) {
                            Text(
                                text = char.toString(),
                                // Размер букв тоже плавно растет вместе с ячейкой
                                fontSize = with(LocalDensity.current) { (cellSize * 0.6f).toSp() },
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}