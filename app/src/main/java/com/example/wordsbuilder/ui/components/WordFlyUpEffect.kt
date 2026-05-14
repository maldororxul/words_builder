package com.example.wordsbuilder.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun WordFlyUpEffect(
    word: String,
    triggerCount: Int,
    modifier: Modifier = Modifier
) {
    if (triggerCount == 0 || word.isEmpty()) return

    // Создаем независимые аниматоры для каждой буквы слова
    val animators = remember(triggerCount) {
        List(word.length) { Animatable(0f) }
    }

    // Траектории: каждая буква летит строго вверх с небольшим случайным отклонением по X
    val trajectories = remember(word) {
        List(word.length) { index ->
            val randomX = (-30..30).random().toFloat()
            // Чем длиннее слово, тем выше летят центральные буквы (эффект арки)
            val targetY = -400f - (index * 20f)
            Pair(randomX, targetY)
        }
    }

    LaunchedEffect(triggerCount) {
        animators.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = 1f,
                    // Запуск лесенкой: каждая последующая буква взлетает чуть позже
                    animationSpec = tween(durationMillis = 400, delayMillis = index * 50)
                )
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            word.forEachIndexed { index, char ->
                val progress = animators.getOrNull(index)?.value ?: 0f
                val alpha = (1f - progress).coerceIn(0f, 1f)

                if (alpha > 0f) {
                    val (targetX, targetY) = trajectories[index]
                    val currentX = (targetX * progress).roundToInt()
                    val currentY = (targetY * progress).roundToInt()

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(currentX, currentY) }
                            .alpha(alpha)
                            .size(40.dp)
                            .background(Color(0xFF4CAF50), shape = RoundedCornerShape(8.dp)), // Зеленая победная плашка буквы
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString().uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Оставляем пустой зазор, чтобы структура Row не ломалась во время улета соседей
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        }
    }
}