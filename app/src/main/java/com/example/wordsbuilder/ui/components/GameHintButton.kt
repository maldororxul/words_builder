package com.example.wordsbuilder.ui.components

import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
fun GameHintButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    val cartoonFontFamily = remember { androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(R.font.cartoon)) }

    // Константы цветов конкретно под дизайн кнопки подсказки
    val containerColor = Color(0xFFFFC107)
    val shadowColor = remember(containerColor) {
        Color(
            red = (containerColor.red * 0.6f).coerceIn(0f, 1f),
            green = (containerColor.green * 0.6f).coerceIn(0f, 1f),
            blue = (containerColor.blue * 0.6f).coerceIn(0f, 1f),
            alpha = containerColor.alpha
        )
    }

    val targetOffset = if (isPressed && enabled) 6f else 0f
    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = tween(durationMillis = 30),
        label = "HintButtonPress"
    )

    val finalContainerColor = if (enabled) containerColor else Color(0xFF444444)
    val finalShadowColor = if (enabled) shadowColor else Color(0xFF222222)
    val finalTextColor = if (enabled) Color.Black else Color.Gray

    val circleShape = CircleShape // Идеальный круг

    Box(
        modifier = modifier
            .size(32.dp) // Жестко задаем размер, гарантируя идеальные пропорции круга
            .background(finalShadowColor, shape = circleShape)
            .pointerInteropFilter { event ->
                if (!enabled) return@pointerInteropFilter false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> { isPressed = true; true }
                    MotionEvent.ACTION_UP -> {
                        if (isPressed) {
                            SoundManager.playSound(context, R.raw.click)
                            onClick()
                        }
                        isPressed = false
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> { isPressed = false; true }
                    else -> false
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isPressed && enabled) 0.dp else 6.dp)
                .offset { IntOffset(0, animatedOffset.dp.roundToPx()) }
                .background(finalContainerColor, shape = circleShape)
                .clip(circleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                fontFamily = cartoonFontFamily,
                fontSize = 16.sp, // Крупный и легко читаемый шрифт
                fontWeight = FontWeight.Black, // Жирное начертание для символа "?"
                color = finalTextColor
            )
        }
    }
}