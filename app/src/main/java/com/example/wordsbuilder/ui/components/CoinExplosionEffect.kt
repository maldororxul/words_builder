package com.example.wordsbuilder.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CoinExplosionEffect(
    triggerCount: Int,
    modifier: Modifier = Modifier
) {
    // Не запускаем анимацию при холодном старте экрана
    if (triggerCount == 0) return

    // Каждая монетка анимирует свой прогресс полета от 0.0 (старт) до 1.0 (финиш) независимо
    val animators = remember(triggerCount) {
        List(6) { Animatable(0f) }
    }

    // Векторы разлета монеток (смещение X и Y в пикселях на пике анимации)
    // Разные углы: влево-вверх, строго вверх, вправо-вверх
    val trajectories = remember {
        listOf(
            Pair(-120f, -220f), // Монетка 1 (влево и сильно вверх)
            Pair(-60f, -280f),  // Монетка 2
            Pair(0f, -320f),    // Монетка 3 (строго вверх)
            Pair(60f, -280f),   // Монетка 4
            Pair(120f, -220f),  // Монетка 5 (вправо и сильно вверх)
            Pair(180f, -160f)   // Монетка 6
        )
    }

    // Запуск аппаратной анимации в корутине
    LaunchedEffect(triggerCount) {
        animators.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = 1f,
                    // Идеальная длительность игрового взрыва: 450 миллисекунд
                    animationSpec = tween(durationMillis = 450 + (index * 30))
                )
            }
        }
    }

    // Отрисовываем легкие текстовые элементы вместо Canvas
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Монетки стартуют точно из центра плашки
    ) {
        animators.forEachIndexed { index, animatable ->
            val progress = animatable.value

            // Плавное затухание (растворение) ближе к концу полета
            val coinAlpha = (1f - progress).coerceIn(0f, 1f)

            if (coinAlpha > 0f) {
                val (targetX, targetY) = trajectories[index]

                // Вычисляем текущие координаты на основе прогресса
                val currentX = (targetX * progress).roundToInt()
                val currentY = (targetY * progress).roundToInt()

                Text(
                    text = "🪙",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .offset { IntOffset(currentX, currentY) } // Аппаратное смещение
                        .alpha(coinAlpha) // Аппаратная прозрачность
                )
            }
        }
    }
}
