package com.example.wordsbuilder.ui.components

import PlacedWord
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.areAnyPressed
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.wordsbuilder.R

@Composable
fun CrosswordGrid(
    placedWords: List<PlacedWord>,
    solvedWords: Set<String>,
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

    var showTapHint by remember { mutableStateOf(false) }
    var tutorialSelectedWord by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(placedWords) {
        // --- АНИМАЦИЯ 1: ЗУМ ---
        kotlinx.coroutines.delay(200L)
        showZoomHint = true
        scaleState.animateTo(1.3f, androidx.compose.animation.core.tween(500))
        kotlinx.coroutines.delay(400L)
        showZoomHint = false
        scaleState.animateTo(1.0f, androidx.compose.animation.core.tween(400))
        // --- АНИМАЦИЯ 2: ТАП (Новая) ---
        val randomWord = placedWords.randomOrNull()?.word
        if (randomWord != null) {
            kotlinx.coroutines.delay(300L) // Пауза
            tutorialSelectedWord = randomWord
            showTapHint = true
            kotlinx.coroutines.delay(1500L) // Длительность
            showTapHint = false
            tutorialSelectedWord = null
        }
    }

    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(6.dp)
            // КРИТИЧЕСКОЕ ИЗМЕНЕНИЕ: Низкоуровневая обработка жестов через единую шину событий
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    var lastTapTime = 0L

                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes

                        // Обработка Pinch-to-Zoom (два и более пальца)
                        if (changes.size >= 2) {
                            val zoomChange = event.calculateZoom()
                            if (zoomChange != 1.0f) {
                                val sensitivity = 1.8f
                                val adjustedZoom = 1f + (zoomChange - 1f) * sensitivity
                                val targetScale = (scaleState.value * adjustedZoom).coerceIn(1.0f, 2.5f)
                                coroutineScope.launch {
                                    scaleState.snapTo(targetScale)
                                }
                            }
                        }
                        // Обработка Двойного Тапа (один палец)
                        else if (changes.size == 1 && !event.buttons.areAnyPressed) {
                            val change = changes.first()
                            if (change.changedToUp()) {
                                val currentTime = System.currentTimeMillis()
                                // Интервал между тапами менее 300мс считается двойным кликом
                                if (currentTime - lastTapTime < 300L) {
                                    val touchOffset = change.position
                                    coroutineScope.launch {
                                        if (scaleState.value > 1.0f) {
                                            launch { scaleState.animateTo(1.0f, androidx.compose.animation.core.tween(300)) }
                                            launch { horizontalScrollState.animateScrollTo(0) }
                                            launch { verticalScrollState.animateScrollTo(0) }
                                        } else {
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
                                    lastTapTime = 0L // Сбрасываем триггер
                                } else {
                                    lastTapTime = currentTime
                                }
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current   // ← добавь это

        val baseCellSize = min(
            maxWidth / cols.coerceAtLeast(1),
            maxHeight / rows.coerceAtLeast(1)
        ) * 0.90f

        val cellSize = baseCellSize * scaleState.value

        val gridWidth = cellSize * cols
        val gridHeight = cellSize * rows

        val offset = remember { mutableStateOf(Offset.Zero) }
        val maxOffset = remember { mutableStateOf(Offset.Zero) }

        LaunchedEffect(gridWidth, gridHeight, maxWidth, maxHeight, scaleState.value) {
            with(density) {
                val extraWidth = (gridWidth - maxWidth).coerceAtLeast(0.dp).toPx()
                val extraHeight = (gridHeight - maxHeight).coerceAtLeast(0.dp).toPx()

                maxOffset.value = Offset(extraWidth / 2f, extraHeight / 2f)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()                    // ← КЛИППИНГ
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            var newOffset = offset.value + dragAmount

                            val maxX = maxOffset.value.x
                            val maxY = maxOffset.value.y

                            newOffset = Offset(
                                newOffset.x.coerceIn(-maxX, maxX),
                                newOffset.y.coerceIn(-maxY, maxY)
                            )
                            offset.value = newOffset
                        }
                    )
                }
                .graphicsLayer {
                    translationX = offset.value.x
                    translationY = offset.value.y
                },
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

            Box(modifier = Modifier.size(gridWidth, gridHeight)) {
                cellsMap.forEach { (coords, char) ->
                    val (cx, cy) = coords
                    val gridX = cx - minX
                    val gridY = cy - minY
                    val effectiveSelectedWord = selectedWord ?: tutorialSelectedWord
                    val isPartofSelectedWord = effectiveSelectedWord != null && placedWords.any { pw ->
                        pw.word.lowercase() == effectiveSelectedWord.lowercase() && pw.word.indices.any { i ->
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

                    val cartoonFontFamily = remember { androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(R.font.cartoon)) }
                    val tileShape = androidx.compose.foundation.shape.RoundedCornerShape((cellSize.value * 0.15f).dp)
                    val shadowHeight = (cellSize.value * 0.08f).dp

                    Box(
                        modifier = Modifier
                            .offset(
                                x = cellSize * gridX,
                                y = (cellSize * gridY) - (shadowHeight * gridY) // Вычитаем накопительный зазор тени
                            )
                            .size(cellSize)
                            .padding(2.dp)
                            .pointerInput(placedWords, coords) {
                                // Кастомный pointerInput на ячейке изолирован и отвечает только за LongPress
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

                        val containerColor = if (isPartofSelectedWord) Color(0xFFFF8000 ) else if (isVisible) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
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
                                    fontFamily = cartoonFontFamily,
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
        AnimatedVisibility(
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
                Text(text = stringResource(R.string.zoom_me), color = Color(0xFFFF8000 ), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        AnimatedVisibility(
            visible = showTapHint,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.75f), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(text = stringResource(R.string.tap_me), color = Color(0xFFFF8000 ), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        // Статичная магическая рамка
        Canvas(modifier = Modifier.fillMaxSize()) {
            val orangeColor = Color(0xFFFF8000 )
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