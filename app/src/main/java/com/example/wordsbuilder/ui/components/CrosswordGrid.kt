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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

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
    val scaleState = remember { androidx.compose.animation.core.Animatable(1f) }

    // Состояние видимости обучающей надписи "Zoom me!"
    var showZoomHint by remember { mutableStateOf(false) }

    // Запускаем анимацию подсказки строго ОДИН раз при открытии уровня
    LaunchedEffect(placedWords) {
        // Небольшая задержка перед стартом, чтобы игрок успел увидеть кроссворд
        kotlinx.coroutines.delay(200L)

        // 1. Показываем надпись и плавно приближаем кроссворд
        showZoomHint = true
        scaleState.animateTo(
            targetValue = 1.3f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 500)
        )

        // Задерживаем кроссворд в приближенном состоянии на мгновение
        kotlinx.coroutines.delay(400L)

        // 2. Скрываем надпись и плавно возвращаем масштаб к оригиналу (1.0f)
        showZoomHint = false
        scaleState.animateTo(
            targetValue = 1.0f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 400)
        )
    }

    // Стейты прокрутки кроссворда
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    // Слушатель масштаба
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(1.0f, 2.5f)
    }

    val coroutineScope = rememberCoroutineScope()

    // Внешний контейнер занимает ВСЁ доступное пространство экрана
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            // 1. Добавляем перехватчик ДВОЙНОГО ТАПА
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { touchOffset -> // Получаем точные пиксельные координаты тапа на экране
                        if (scale > 1.0f) {
                            // СБРОС: Возвращаем масштаб к 1.0 и плавно центрируем кроссворд
                            scale = 1.0f
                            coroutineScope.launch {
                                horizontalScrollState.animateScrollTo(0)
                                verticalScrollState.animateScrollTo(0)
                            }
                        } else {
                            // УВЕЛИЧЕНИЕ В ТОЧКУ КЛИКА:
                            val targetScale = 2.5f

                            // Получаем геометрический центр области просмотра
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f

                            // Вычисляем расстояние от центра экрана до пальца пользователя
                            val deltaX = touchOffset.x - centerX
                            val deltaY = touchOffset.y - centerY

                            // Рассчитываем, на сколько пикселей нужно сместить скролл-бары,
                            // чтобы точка тапа оказалась ровно по центру экрана после зума
                            val scrollTargetX = (deltaX * targetScale).toInt()
                            val scrollTargetY = (deltaY * targetScale).toInt()

                            // Применяем новый масштаб
                            scale = targetScale

                            // Плавно скроллим холст в расчетную точку (coerced автоматически ограничит края)
                            coroutineScope.launch {
                                horizontalScrollState.animateScrollTo(scrollTargetX.coerceAtLeast(0))
                                verticalScrollState.animateScrollTo(scrollTargetY.coerceAtLeast(0))
                            }
                        }
                    }
                )
            }
            .transformable(state = transformState),
        contentAlignment = Alignment.Center
    ) {
        // Базовые размеры ячейки (Оригинальная логика)
        val baseCellSize = min(
            maxWidth / cols.coerceAtLeast(1),
            maxHeight / rows.coerceAtLeast(1)
        ) * 0.90f

        // КРИТИЧЕСКОЕ ИЗМЕНЕНИЕ 1: Окно просмотра (скролла) теперь занимает fillMaxSize().
        // Оно ограничено краями компонента, а не размерами сгенерированного кроссворда.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .verticalScroll(verticalScrollState),
            contentAlignment = Alignment.Center
        ) {
            val cellSize = baseCellSize * scale

            // ТОЧЕЧНОЕ ИЗМЕНЕНИЕ: Создаём карту клеток прямо ЗДЕСЬ (внутри Box скролла)
            val cellsMap = mutableMapOf<Pair<Int, Int>, Char>()
            placedWords.forEach { pw ->
                pw.word.forEachIndexed { i, char ->
                    val x = if (pw.isHorizontal) pw.x + i else pw.x
                    val y = if (pw.isHorizontal) pw.y else pw.y + i
                    cellsMap[Pair(x, y)] = char
                }
            }

            // Теперь этот внутренний Box отлично видит cellsMap
            Box(
                modifier = Modifier.size(cellSize * cols, cellSize * rows)
            ) {
                cellsMap.forEach { (coords, char) ->
                    val (cx, cy) = coords
                    val gridX = cx - minX
                    val gridY = cy - minY

                    // Проверяем видимость (Оригинальная логика)
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

        // ТОЧЕЧНОЕ ИЗМЕНЕНИЕ: Вставляем красивую мультяшную надпись по центру экрана
        androidx.compose.animation.AnimatedVisibility(
            visible = showZoomHint,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.75f), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Zoom me! 🔍",
                    color = Color(0xFFFF9800), // Сочный оранжевый цвет в тон вашей магической рамки
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // КРИТИЧЕСКОЕ ИЗМЕНЕНИЕ 2: Холст рамки растягивается на fillMaxSize().
        // Линии молний теперь будут рисоваться строго по внешнему периметру всего игрового экрана.
        Canvas(
            modifier = Modifier.fillMaxSize()
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

            // Отступы внутрь от краев экрана, чтобы рамка выглядела гармонично
            val offsetDistance = 0f
            val topLeft = Offset(offsetDistance, offsetDistance)
            val topRight = Offset(size.width - offsetDistance, offsetDistance)
            val bottomLeft = Offset(offsetDistance, size.height - offsetDistance)
            val bottomRight = Offset(size.width - offsetDistance, size.height - offsetDistance)

            drawStaticMagicEdge(topLeft, topRight, edgeId = 100)
            drawStaticMagicEdge(topRight, bottomRight, edgeId = 200)
            drawStaticMagicEdge(bottomRight, bottomLeft, edgeId = 300)
            drawStaticMagicEdge(bottomLeft, topLeft, edgeId = 400)
        }
    }
}
