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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R
import com.example.wordsbuilder.SoundManager
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun WordWheel(
    letters: List<Char>,
    targetWords: List<String>, // Добавляем этот параметр
    onWordComposed: (String) -> Unit
) {
    var selectedIndices by remember { mutableStateOf(emptyList<Int>()) }
    var touchPoint by remember { mutableStateOf<Offset?>(null) }
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val context = LocalContext.current

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

                        if (distance < 60f && !selectedIndices.contains(index)) {
                            selectedIndices = selectedIndices + index
                            SoundManager.playSound(context, R.raw.click)
                            // ОБНОВЛЕНИЕ В РЕАЛЬНОМ ВРЕМЕНИ:
                            val currentWord = selectedIndices.map { letters[it] }.joinToString("")
                            onWordComposed(currentWord)
                        }
                    }
                },
                onDragEnd = {
                    val word = selectedIndices.map { letters[it] }.joinToString("")
                    // Звук ошибки только если слово длиннее 1 буквы и его нет в списке
                    if (word.length > 1 && !targetWords.contains(word)) {
                        SoundManager.playSound(context, R.raw.error)
                    }
                    // Всегда уведомляем GameScreen, что ввод завершен
                    val finalWord = selectedIndices.map { letters[it] }.joinToString("")
                    onWordComposed("CHECK:$finalWord")
                    // При отпускании можно оставить слово или очистить
                    // (обычно в таких играх слово "улетает" в кроссворд)
                    selectedIndices = emptyList()
                    touchPoint = null
                }
            )
        }
    ) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Рисуем линии
        if (selectedIndices.isNotEmpty()) {
            val points = selectedIndices.map { index ->
                val angle = 2 * PI * index / letters.size - PI / 2
                Offset(center.x + cos(angle).toFloat() * (radius * 0.8f), center.y + sin(angle).toFloat() * (radius * 0.8f))
            }
            for (i in 0 until points.size - 1) {
                drawLine(Color(0xFF00BCD4), points[i], points[i+1], strokeWidth = 12f)
            }
            touchPoint?.let { drawLine(Color(0xFF00BCD4), points.last(), it, strokeWidth = 12f) }
        }

        // Рисуем буквы
        letters.forEachIndexed { index, char ->
            val angle = 2 * PI * index / letters.size - PI / 2
            val letterPos = Offset(
                center.x + cos(angle).toFloat() * (radius * 0.8f),
                center.y + sin(angle).toFloat() * (radius * 0.8f)
            )
            val isSelected = selectedIndices.contains(index)
            val textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 32.sp, // Увеличили
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) Color.White else Color.Black
            )
            drawCircle(
                color = if (isSelected) Color(0xFF00BCD4) else Color(0xFFE0E0E0),
                radius = 50f,
                center = letterPos
            )

            // ТОЧНОЕ ЦЕНТРИРОВАНИЕ ТЕКСТА
            val textLayoutResult = textMeasurer.measure(
                text = char.toString(),
                style = androidx.compose.ui.text.TextStyle(fontSize = 28.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            )
            val textSize = textLayoutResult.size
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    letterPos.x - textSize.width / 2,
                    letterPos.y - textSize.height / 2
                )
            )
        }
    }
}