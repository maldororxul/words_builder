package com.example.wordsbuilder.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedCrosswordCell(
    letter: String,
    isRevealed: Boolean,
    modifier: Modifier = Modifier
) {
    // Аниматор масштаба: стартует со значения 1.5f (буква большая, как будто летит сверху) и падает до 1.0f
    val scaleAnim = remember(isRevealed) { Animatable(if (isRevealed) 1.6f else 1.0f) }

    LaunchedEffect(isRevealed) {
        if (isRevealed) {
            // Приземляем букву за 300 миллисекунд
            scaleAnim.animateTo(1f, animationSpec = tween(durationMillis = 300))
        }
    }

    Box(
        modifier = modifier
            .size(36.dp)
            .background(if (isRevealed) Color(0xFF2E7D32) else Color(0xFF222222)) // Зеленый фон для угаданных клеток
            .border(1.dp, Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        if (isRevealed && letter.isNotEmpty()) {
            Text(
                text = letter.uppercase(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.scale(scaleAnim.value) // Аппаратный зум приземления
            )
        }
    }
}