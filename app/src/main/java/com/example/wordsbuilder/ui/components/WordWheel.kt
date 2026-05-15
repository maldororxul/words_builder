package com.example.wordsbuilder.ui.components

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R
import kotlin.math.*
import kotlin.random.Random

@Composable
fun WordWheel(
    letters: List<Char>,
    targetWords: List<String>,
    modifier: Modifier = Modifier,
    onWordComposed: (String) -> Unit
) {
    var selectedIndices by remember { mutableStateOf(emptyList<Int>()) }
    var touchPoint by remember { mutableStateOf<Offset?>(null) }
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val context = LocalContext.current

    val cartoonFontFamily = FontFamily(Font(R.font.cartoon))

    // Временной триггер для постоянного обновления искривлений "магических" линий на каждом кадре экрана
    var animationTicks by remember { mutableLongStateOf(0L) }
    if (selectedIndices.isNotEmpty()) {
        LaunchedEffect(Unit) {
            while (true) {
                withInfiniteAnimationFrameMillis { frameTime ->
                    animationTicks = frameTime
                }
            }
        }
    }

    Canvas(modifier = modifier
        .size(300.dp)
        .pointerInput(letters) {
            detectDragGestures(
                onDragStart = { offset ->
                    selectedIndices = emptyList()
                    touchPoint = offset
                },
                onDrag = { change, _ ->
                    touchPoint = change.position
                    val canvasWidth = size.width.toFloat()
                    val canvasHeight = size.height.toFloat()
                    val radius = min(canvasWidth, canvasHeight) / 2
                    val center = Offset(canvasWidth / 2, canvasHeight / 2)

                    letters.forEachIndexed { index, _ ->
                        val angle = 2 * PI * index / letters.size - PI / 2
                        val letterPos = Offset(
                            center.x + cos(angle).toFloat() * (radius * 0.8f),
                            center.y + sin(angle).toFloat() * (radius * 0.8f)
                        )
                        val distance = sqrt((change.position.x - letterPos.x).pow(2) + (change.position.y - letterPos.y).pow(2))

                        if (distance < 90f && !selectedIndices.contains(index)) {
                            selectedIndices = selectedIndices + index
                            SoundManager.playSound(context, R.raw.click)
                            val currentWord = selectedIndices.map { letters[it] }.joinToString("")
                            onWordComposed(currentWord)
                        }
                    }
                },
                onDragEnd = {
                    val word = selectedIndices.map { letters[it] }.joinToString("")
                    if (word.length > 1 && !targetWords.contains(word)) {
                        SoundManager.playSound(context, R.raw.error)
                    }
                    val finalWord = selectedIndices.map { letters[it] }.joinToString("")
                    onWordComposed("CHECK:$finalWord")
                    selectedIndices = emptyList()
                    touchPoint = null
                }
            )
        }
    ) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        // 1. Отрисовываем большую круглую полупрозрачную темную подложку самого колеса
        drawCircle(
            color = Color.Black.copy(alpha = 0.25f),
            radius = radius * 0.95f,
            center = center
        )

        // Белая круглая полупрозрачная подложка, на которой лежат буквы
        // Рисуем её строго по внутреннему кольцу расположения букв (radius * 0.8f)
        drawCircle(
            color = Color.White.copy(alpha = 0.15f),
            radius = radius * 0.8f,
            center = center,
            style = Stroke(width = 130f) // Ширина кольца-подложки идеально перекрывает размеры кружков букв
        )

        // Отрисовка "магических" искривленных разрядов (Path) вместо прямых линий
        val orangeColor = Color(0xFFFF8000 )

        fun drawLightningPath(start: Offset, end: Offset) {
            val path = Path().apply { moveTo(start.x, start.y) }
            val distance = sqrt((end.x - start.x).pow(2) + (end.y - start.y).pow(2))
            val segments = (distance / 15f).toInt().coerceAtLeast(3)
            val rand = Random(animationTicks + start.x.toInt())

            for (i in 1 until segments) {
                val fraction = i.toFloat() / segments
                val baseX = start.x + (end.x - start.x) * fraction
                val baseY = start.y + (end.y - start.y) * fraction

                val dx = end.x - start.x
                val dy = end.y - start.y
                val length = sqrt(dx*dx + dy*dy)
                val nx = -dy / length
                val ny = dx / length

                val offsetAmount = (rand.nextFloat() - 0.5f) * 14f
                path.lineTo(baseX + nx * offsetAmount, baseY + ny * offsetAmount)
            }
            path.lineTo(end.x, end.y)

            drawPath(path = path, color = orangeColor, style = Stroke(width = 10f))
            drawPath(path = path, color = Color.White, style = Stroke(width = 3f))
        }

        if (selectedIndices.isNotEmpty()) {
            val points = selectedIndices.map { index ->
                val angle = 2 * PI * index / letters.size - PI / 2
                Offset(center.x + cos(angle).toFloat() * (radius * 0.8f), center.y + sin(angle).toFloat() * (radius * 0.8f))
            }

            for (i in 0 until points.size - 1) {
                drawLightningPath(points[i], points[i+1])
            }

            touchPoint?.let { drawLightningPath(points.last(), it) }
        }

        // Рисуем подложки-круги и мультяшные буквы
        letters.forEachIndexed { index, char ->
            val angle = 2 * PI * index / letters.size - PI / 2
            val letterPos = Offset(
                center.x + cos(angle).toFloat() * (radius * 0.8f),
                center.y + sin(angle).toFloat() * (radius * 0.8f)
            )
            val isSelected = selectedIndices.contains(index)

            // ИСПРАВЛЕНИЕ ПРЕДМЕТА 1: Вызываем встроенный расчет масштаба прямо внутри цикла отрисовки
            // Это гарантирует, что Compose мгновенно вернет размер буквы к 1.0f в ту же миллисекунду, как selectedIndices очистится
            val currentScale = if (isSelected) 1.25f else 1.0f
            val animatedRadius = 65f * currentScale

            drawCircle(
                color = if (isSelected) orangeColor else Color(0xFFE0E0E0),
                radius = animatedRadius,
                center = letterPos
            )

            val animatedFontSize = (38 * currentScale).sp

            val textLayoutResult = textMeasurer.measure(
                text = char.toString().uppercase(),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = animatedFontSize,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    fontFamily = cartoonFontFamily,
                    color = if (isSelected) Color.White else Color(0xFF333333)
                )
            )
            val textSize = textLayoutResult.size
            val verticalCorrection = 8f * currentScale

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    letterPos.x - textSize.width / 2,
                    letterPos.y - textSize.height / 2 + verticalCorrection
                )
            )
        }
    }
}