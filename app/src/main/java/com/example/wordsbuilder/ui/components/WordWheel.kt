import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun WordWheel(
    letters: List<Char>,
    targetWords: List<String>,
    onWordComposed: (String) -> Unit
) {
    var selectedIndices by remember { mutableStateOf(emptyList<Int>()) }
    var touchPoint by remember { mutableStateOf<Offset?>(null) }
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val context = LocalContext.current

    val CartoonFontFamily = FontFamily(Font(R.font.cartoon))

    // Массив аниматоров для каждой буквы на колесе
    val scales = letters.indices.map { index ->
        val isSelected = selectedIndices.contains(index)
        // Если буква выбрана — увеличиваем её до 1.25х, если нет — возвращаем к 1.0х
        animateFloatAsState(
            targetValue = if (isSelected) 1.25f else 1.0f,
            animationSpec = tween(durationMillis = 100),
            label = "ScaleArc_$index"
        )
    }

    Canvas(modifier = Modifier
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

                        // Увеличиваем зону захвата с учётом потенциального масштабирования круга
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

        // Рисуем линии связи между буквами
        if (selectedIndices.isNotEmpty()) {
            val points = selectedIndices.map { index ->
                val angle = 2 * PI * index / letters.size - PI / 2
                Offset(center.x + cos(angle).toFloat() * (radius * 0.8f), center.y + sin(angle).toFloat() * (radius * 0.8f))
            }
            for (i in 0 until points.size - 1) {
                drawLine(Color(0xFF00BCD4), points[i], points[i+1], strokeWidth = 14f)
            }
            touchPoint?.let { drawLine(Color(0xFF00BCD4), points.last(), it, strokeWidth = 14f) }
        }

        // Рисуем подложки-круги и мультяшные буквы
        letters.forEachIndexed { index, char ->
            val angle = 2 * PI * index / letters.size - PI / 2
            val letterPos = Offset(
                center.x + cos(angle).toFloat() * (radius * 0.8f),
                center.y + sin(angle).toFloat() * (radius * 0.8f)
            )
            val isSelected = selectedIndices.contains(index)

            // Получаем текущий плавный масштаб для данной буквы
            val currentScale = scales[index].value

            // Анимированный радиус круга подложки (базовый 65f увеличивается до ~81f при тапе)
            val animatedRadius = 65f * currentScale

            drawCircle(
                color = if (isSelected) Color(0xFF00BCD4) else Color(0xFFE0E0E0),
                radius = animatedRadius,
                center = letterPos
            )

            // Анимируем размер шрифта на основе текущего масштаба элемента
            val animatedFontSize = (38 * currentScale).sp

            val textLayoutResult = textMeasurer.measure(
                text = char.toString().uppercase(),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = animatedFontSize,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    fontFamily = CartoonFontFamily,
                    color = if (isSelected) Color.White else Color(0xFF333333)
                )
            )
            val textSize = textLayoutResult.size

            // ИСПРАВЛЕНИЕ СМЕЩЕНИЯ ВВЕРХ:
            // Большинство кастомных шрифтов имеют завышенный внутренний отступ (Ascent).
            // Добавление небольшой константы (напр. + 4 пикселя) идеально центрирует текст по вертикали.
            val verticalCorrection = 6f * currentScale

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