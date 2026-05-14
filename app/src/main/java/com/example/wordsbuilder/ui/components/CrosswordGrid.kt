package com.example.wordsbuilder.ui.components

import PlacedWord
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlin.math.sqrt
import kotlin.random.Random

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

    // Состояние масштаба кроссворда
    var scale by remember { mutableFloatStateOf(1f) }

    // Стейты прокрутки кроссворда
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    // Слушатель масштаба
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(1.0f, 2.5f)
    }

    // КРИТИЧЕСКОЕ ИЗМЕНЕНИЕ 1: transformable вешается на самый верхний контейнер,
    // чтобы скроллы не перехватывали жест щипка двумя пальцами.
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .transformable(state = transformState),
        contentAlignment = Alignment.Center
    ) {
        // Базовые размеры сетки под область экрана
        val baseCellSize = min(
            maxWidth / cols.coerceAtLeast(1),
            maxHeight / rows.coerceAtLeast(1)
        ) * 0.90f

        val gridWidth = baseCellSize * cols
        val gridHeight = baseCellSize * rows

        // Создаём карту всех клеток (Оригинальная логика)
        val cellsMap = mutableMapOf<Pair<Int, Int>, Char>()
        placedWords.forEach { pw ->
            pw.word.forEachIndexed { i, char ->
                val x = if (pw.isHorizontal) pw.x + i else pw.x
                val y = if (pw.isHorizontal) pw.y else pw.y + i
                cellsMap[Pair(x, y)] = char
            }
        }

        // КРИТИЧЕСКОЕ ИЗМЕНЕНИЕ 2: Окно просмотра жестко ограничено размерами рамки (gridWidth, gridHeight).
        // Скроллинг работает только внутри него.
        Box(
            modifier = Modifier
                .size(gridWidth, gridHeight)
                .horizontalScroll(horizontalScrollState)
                .verticalScroll(verticalScrollState),
            contentAlignment = Alignment.Center
        ) {
            // Динамический размер ячейки на основе текущего зума
            val cellSize = baseCellSize * scale

            // Внутренний контейнер кроссворда, который физически растет при зуме
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

        // Статичная магическая рамка поверх всего экрана
        Canvas(
            modifier = Modifier.size(gridWidth, gridHeight)
        ) {
            val orangeColor = Color(0xFFFF9800)

            fun drawStaticMagicEdge(start: Offset, end: Offset, edgeId: Int) {
                val path = Path().apply { moveTo(start.x, start.y) }
                val distance = sqrt((end.x - start.x) * (end.x - start.x) + (end.y - start.y) * (end.y - start.y))
                val segments = (distance / 20f).toInt().coerceAtLeast(5)
                val rand = Random(edgeId + distance.toInt())

                for (i in 1 until segments) {
                    val fraction = i.toFloat() / segments
                    val baseX = start.x + (end.x - start.x) * fraction
                    val baseY = start.y + (end.y - start.y) * fraction

                    val dx = end.x - start.x
                    val dy = end.y - start.y
                    val length = sqrt(dx * dx + dy * dy)
                    val nx = -dy / length
                    val ny = dx / length

                    val offsetAmount = (rand.nextFloat() - 0.5f) * 12f
                    path.lineTo(baseX + nx * offsetAmount, baseY + ny * offsetAmount)
                }
                path.lineTo(end.x, end.y)

                drawPath(path = path, color = orangeColor, style = Stroke(width = 8f))
                drawPath(path = path, color = Color.White, style = Stroke(width = 2.5f))
            }

            val offsetDistance = 16f
            val topLeft = Offset(-offsetDistance, -offsetDistance)
            val topRight = Offset(size.width + offsetDistance, -offsetDistance)
            val bottomLeft = Offset(-offsetDistance, size.height + offsetDistance)
            val bottomRight = Offset(size.width + offsetDistance, size.height + offsetDistance)

            drawStaticMagicEdge(topLeft, topRight, edgeId = 100)
            drawStaticMagicEdge(topRight, bottomRight, edgeId = 200)
            drawStaticMagicEdge(bottomRight, bottomLeft, edgeId = 300)
            drawStaticMagicEdge(bottomLeft, topLeft, edgeId = 400)
        }
    }
}
