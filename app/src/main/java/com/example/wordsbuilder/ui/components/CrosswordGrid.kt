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
import androidx.compose.ui.res.stringResource
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

    // ЕДИНАЯ ТОЧКА ПРАВДЫ ДЛЯ МАСШТАБА
    val scaleState = remember { androidx.compose.animation.core.Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    // Состояние видимости обучающей надписи "Zoom me!"
    var showZoomHint by remember { mutableStateOf(false) }

    // Запускаем анимацию подсказки строго ОДИН раз при открытии уровня
    LaunchedEffect(placedWords) {
        kotlinx.coroutines.delay(200L)
        showZoomHint = true
        scaleState.animateTo(
            targetValue = 1.3f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 500)
        )
        kotlinx.coroutines.delay(400L)
        showZoomHint = false
        scaleState.animateTo(
            targetValue = 1.0f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 400)
        )
    }

    // Стейты прокрутки кроссворда
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    // Слушатель масштаба (использует snapTo для мгновенного обновления без анимации)
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        val targetScale = (scaleState.value * zoomChange).coerceIn(1.0f, 2.5f)
        coroutineScope.launch {
            scaleState.snapTo(targetScale)
        }
    }

    // Внешний контейнер занимает ВСЁ доступное пространство экрана
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { touchOffset ->
                        coroutineScope.launch {
                            if (scaleState.value > 1.0f) {
                                // СБРОС: Плавно возвращаем масштаб к 1.0 и центрируем скролл
                                launch { scaleState.animateTo(1.0f, androidx.compose.animation.core.tween(300)) }
                                launch { horizontalScrollState.animateScrollTo(0) }
                                launch { verticalScrollState.animateScrollTo(0) }
                            } else {
                                // УВЕЛИЧЕНИЕ В ТОЧКУ КЛИКА
                                val targetScale = 2.5f
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f

                                val deltaX = touchOffset.x - centerX
                                val deltaY = touchOffset.y - centerY

                                val scrollTargetX = (deltaX * targetScale).toInt()
                                val scrollTargetY = (deltaY * targetScale).toInt()

                                launch { scaleState.animateTo(targetScale, androidx.compose.animation.core.tween(300)) }
                                launch { horizontalScrollState.animateScrollTo(scrollTargetX.coerceAtLeast(0)) }
                                launch { verticalScrollState.animateScrollTo(scrollTargetY.coerceAtLeast(0)) }
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

        // Окно просмотра (скролла) занимает fillMaxSize()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .verticalScroll(verticalScrollState),
            contentAlignment = Alignment.Center
        ) {
            // Динамический размер ячейки зависит от анимируемого scaleState.value
            val cellSize = baseCellSize * scaleState.value

            val cellsMap = mutableMapOf<Pair<Int, Int>, Char>()
            placedWords.forEach { pw ->
                pw.word.forEachIndexed { i, char ->
                    val x = if (pw.isHorizontal) pw.x + i else pw.x
                    val y = if (pw.isHorizontal) pw.y else pw.y + i
                    cellsMap[Pair(x, y)] = char
                }
            }

            // Внутренний контейнер кроссворда
            Box(
                modifier = Modifier.size(cellSize * cols, cellSize * rows)
            ) {
                // Находим цикл клеток cellsMap.forEach внутри Box(modifier = Modifier.size(...)) и заменяем его:
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

                    // Регистрируем ваш мультяшный шрифт для ячеек
                    val CartoonFontFamily = remember { androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.example.wordsbuilder.R.font.cartoon)) }

                    // Скругление углов плитки адаптируется под её размер (примерно 15% от размера ячейки)
                    val tileShape = androidx.compose.foundation.shape.RoundedCornerShape((cellSize.value * 0.15f).dp)
                    val shadowHeight = (cellSize.value * 0.08f).dp // Толщина 3D-подложки

                    Box(
                        modifier = Modifier
                            .offset(x = cellSize * gridX, y = cellSize * gridY)
                            .size(cellSize)
                            .padding(2.dp) // Небольшой зазор между плитками для объема
                    ) {
                        // 1. ЗАДНИЙ СЛОЙ (3D-ТЕНЬ ПЛИТКИ)
                        // Для угаданных — глубокий темно-зеленый, для пустых — темно-серый
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = if (isVisible) Color(0xFF1B5E20) else Color(0xFF424242),
                                    shape = tileShape
                                )
                        )

                        // 2. ПЕРЕДНИЙ СЛОЙ (КРЫШКА ПЛИТКИ)
                        // Смещен чуть выше, создавая честный эффект объема плитки
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = shadowHeight) // Освобождаем место под нижнюю грань-тень
                                .background(
                                    color = if (isVisible) Color(0xFF4CAF50) else Color(0xFFE0E0E0), // Сочный зеленый / Приятный светло-серый
                                    shape = tileShape
                                )
                                // Аккуратный внутренний бортик, подчеркивающий мультяшность плитки
                                .border(
                                    width = (cellSize.value * 0.04f).dp,
                                    color = if (isVisible) Color(0xFF81C784) else Color.White,
                                    shape = tileShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isVisible) {
                                Text(
                                    text = char.toString().uppercase(),
                                    // Используем ваш мультяшный шрифт
                                    fontFamily = CartoonFontFamily,
                                    // Размер шрифта динамически подстраивается под сжатие ячейки
                                    fontSize = with(LocalDensity.current) { (cellSize * 0.55f).toSp() },
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White, // Белые сочные буквы на зеленой плитке
                                    modifier = Modifier
                                        // Небольшая вертикальная коррекция, чтобы компенсировать отступы шрифта
                                        .offset(y = (cellSize.value * 0.02f).dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Мультяшная надпись по центру экрана
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
                    text = stringResource(com.example.wordsbuilder.R.string.zoom_me),
                    color = Color(0xFFFF9800),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Статичная магическая рамка поверх всего экрана
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