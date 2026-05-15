package com.example.wordsbuilder.ui.components

import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GameButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFFF8000),
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }

    // Вычисляем цвета для создания 3D объема (темная подложка-тень)
    val shadowColor = remember(containerColor) {
        // Затемняем основной цвет кнопки для создания эффекта глубины
        Color(
            red = (containerColor.red * 0.6f).coerceIn(0f, 1f),
            green = (containerColor.green * 0.6f).coerceIn(0f, 1f),
            blue = (containerColor.blue * 0.6f).coerceIn(0f, 1f),
            alpha = containerColor.alpha
        )
    }

    // Анимация смещения кнопки вниз при нажатии.
    // Когда кнопка нажата, она сдвигается на 6dp вниз, перекрывая свою тень.
    val targetOffset = if (isPressed && enabled) 6f else 0f
    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = tween(durationMillis = 30),
        label = "ButtonPress"
    )

    // Если кнопка заблокирована, она становится серой
    val finalContainerColor = if (enabled) containerColor else Color(0xFF444444)
    val finalShadowColor = if (enabled) shadowColor else Color(0xFF222222)
    val finalTextColor = if (enabled) Color.White else Color.Gray

    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .height(56.dp) // Чуть увеличиваем высоту для компенсации 3D эффекта
            .fillMaxWidth()
            .background(finalShadowColor, shape = shape) // Задний неподвижный слой (тень/объем)
            .pointerInteropFilter { event ->
                if (!enabled) return@pointerInteropFilter false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isPressed) {
                            SoundManager.playSound(context, R.raw.click)
                            onClick()
                        }
                        isPressed = false
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        true
                    }
                    else -> false
                }
            }
    ) {
        // Передний движущийся слой кнопки
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isPressed && enabled) 0.dp else 6.dp) // Пропадает зазор при нажатии
                .offset { IntOffset(0, animatedOffset.dp.roundToPx()) }
                .background(finalContainerColor, shape = shape)
                .clip(shape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = finalTextColor
            )
        }
    }
}