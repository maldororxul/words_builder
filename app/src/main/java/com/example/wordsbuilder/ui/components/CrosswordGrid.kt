package com.example.wordsbuilder.ui.components

import PlacedWord
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.wordsbuilder.R

@Composable
fun CrosswordGrid(
    placedWords: List<PlacedWord>,
    solvedWords: Set<String>,
    wordsMap: Map<String, String>,
    onWordLongPressed: (String) -> Unit,
    selectedWord: String?,
    modifier: Modifier = Modifier
) {
    if (placedWords.isEmpty()) {
        Text("Failed to generate crossword!", color = Color.White)
        return
    }

    val context = LocalContext.current

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

    val scaleState = remember { androidx.compose.animation.core.Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    var showZoomHint by remember { mutableStateOf(false) }

    LaunchedEffect(placedWords) {
        kotlinx.coroutines.delay(200L)
        showZoomHint = true
        scaleState.animateTo(1.3f, androidx.compose.animation.core.tween(500))
        kotlinx.coroutines.delay(400L)
        showZoomHint = false
        scaleState.animateTo(1.0f, androidx.compose.animation.core.tween(400))
    }

    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    // Внешний контейнер экрана
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            // ПОТОК 1: Отдельный изолированный инпут для отслеживания ДВОЙНОГО ТАПА
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
            // ПОТОК 2: Отдельный изолированный инпут для аппаратного ЩИПКА (Pinch-to-Zoom)
            // Теперь они работают параллельно и не блокируют друг друга!
            .pointerInput(Unit) {
                detectTransformGestures(panZoomLock = false) { _, _, zoomChange, _ ->
                    if (zoomChange != 1.0f) {
                        val sensitivity = 1.8f // Наш повышенный коэффициент отзывчивости
                        val adjustedZoom = 1f + (zoomChange - 1f) * sensitivity
                        val targetScale = (scaleState.value * adjustedZoom).coerceIn(1.0f, 2.5f)
                        coroutineScope.launch {
                            scaleState.snapTo(targetScale)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val baseCellSize = min(maxWidth / cols.coerceAtLeast(1), maxHeight / rows.coerceAtLeast(1)) * 0.90f
        val gridWidth = baseCellSize * cols
        val gridHeight = baseCellSize * rows

        // Окно просмотра кроссворда
        Box(
            modifier = Modifier
                .size(gridWidth, gridHeight)
                .horizontalScroll(horizontalScrollState)
                .verticalScroll(verticalScrollState),
            contentAlignment = Alignment.Center
        ) {
            val cellSize = baseCellSize * scaleState.value

            val cellsMap = mutableMapOf<Pair<Int, Int>, Char>()
            placedWords.forEach { pw ->
                pw.word.forEachIndexed { i, char ->
                    val x = if (pw.isHorizontal) pw.x + i else pw.x
                    val y = if (pw.isHorizontal) pw.y else pw.y + i
                    cellsMap[Pair(x, y)] = char
                }
            }

            Box(modifier = Modifier.size(cellSize * cols, cellSize * rows)) {
                cellsMap.forEach { (coords, char) ->
                    val (cx, cy) = coords
                    val gridX = cx - minX
                    val gridY = cy - minY

                    val isPartofSelectedWord = selectedWord != null && placedWords.any { pw ->
                        pw.word.lowercase() == selectedWord.lowercase() && pw.word.indices.any { i ->
                            val px = if (pw.isHorizontal) pw.x + i else pw.x
                            val py = if (pw.isHorizontal) pw.y else pw.y + i
                            px == cx && py == cy
                        }
                    }

                    val isVisible = placedWords.any { pw ->
                        solvedWords.map { it.lowercase() }.contains(pw.word.lowercase()) &&
                                pw.word.indices.any { i ->
                                    val px = if (pw.isHorizontal) pw.x + i else pw.x
                                    val py = if (pw.isHorizontal) pw.y else pw.y + i
                                    px == cx && py == cy
                                }
                    }

                    val CartoonFontFamily = remember { androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(R.font.cartoon)) }
                    val tileShape = androidx.compose.foundation.shape.RoundedCornerShape((cellSize.value * 0.15f).dp)
                    val shadowHeight = (cellSize.value * 0.08f).dp

                    Box(
                        modifier = Modifier
                            .offset(x = cellSize * gridX, y = cellSize * gridY)
                            .size(cellSize)
                            .padding(2.dp)
                            .pointerInput(placedWords, coords) {
                                detectTapGestures(
                                    onLongPress = {
                                        val clickedWord = placedWords.find { pw ->
                                            pw.word.indices.any { i ->
                                                val px = if (pw.isHorizontal) pw.x + i else pw.x
                                                val py = if (pw.isHorizontal) pw.y else pw.y + i
                                                px == cx && py == cy
                                            }
                                        }
                                        clickedWord?.let {
                                            SoundManager.playSound(context, R.raw.click)
                                            onWordLongPressed(it.word)
                                        }
                                    }
                                )
                            }
                    ) {
                        val shadowColor = if (isPartofSelectedWord) Color(0xFFE65100) else if (isVisible) Color(0xFF1B5E20) else Color(0xFF424242)
                        Box(modifier = Modifier.fillMaxSize().background(color = shadowColor, shape = tileShape))

                        val containerColor = if (isPartofSelectedWord) Color(0xFFFF9800) else if (isVisible) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
                        val borderColor = if (isPartofSelectedWord) Color(0xFFFFB74D) else if (isVisible) Color(0xFF81C784) else Color.White

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = shadowHeight)
                                .background(color = containerColor, shape = tileShape)
                                .border(width = (cellSize.value * 0.04f).dp, color = borderColor, shape = tileShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isVisible) {
                                Text(
                                    text = char.toString().uppercase(),
                                    fontFamily = CartoonFontFamily,
                                    fontSize = with(LocalDensity.current) { (cellSize * 0.55f).toSp() },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.offset(y = (cellSize.value * 0.02f).dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Обучающая надпись "Zoom me!"
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
                Text(text = stringResource(R.string.zoom_me), color = Color(0xFFFF9800), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        // Статичная магическая рамка
        Canvas(modifier = Modifier.size(gridWidth, gridHeight)) {
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