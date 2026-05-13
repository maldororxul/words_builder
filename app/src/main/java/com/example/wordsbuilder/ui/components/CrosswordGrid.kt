import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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

    // Находим границы
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

    BoxWithConstraints(modifier = modifier) {
        val cellSize = androidx.compose.ui.unit.min(
            maxWidth / cols.coerceAtLeast(1),
            maxHeight / rows.coerceAtLeast(1)
        ) * 0.95f

        // Создаём карту всех клеток
        val cellsMap = mutableMapOf<Pair<Int, Int>, Char>()
        placedWords.forEach { pw ->
            pw.word.forEachIndexed { i, char ->
                val x = if (pw.isHorizontal) pw.x + i else pw.x
                val y = if (pw.isHorizontal) pw.y else pw.y + i
                cellsMap[Pair(x, y)] = char
            }
        }

        Box(
            modifier = Modifier
                .size(cellSize * cols, cellSize * rows)
                .align(Alignment.Center)
        ) {
            cellsMap.forEach { (coords, char) ->
                val (cx, cy) = coords
                val gridX = cx - minX
                val gridY = cy - minY

                // Проверяем видимость
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
                        .offset(x = cellSize * gridX, y = cellSize * gridY)
                        .size(cellSize)
                        .background(Color.White.copy(alpha = 0.95f))
                        .border(1.dp, Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVisible) {
                        Text(
                            text = char.toString(),
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