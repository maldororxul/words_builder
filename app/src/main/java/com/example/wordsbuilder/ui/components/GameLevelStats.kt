package com.example.wordsbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameLevelStats(
    coins: Int,
    levelNumber: Int,
    score: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(Color(0x80000000), RoundedCornerShape(16.dp)) // Полупрозрачная подложка
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Уровень
        Text(
            text = "Уровень $levelNumber",
            color = Color.White,
            fontSize = 18.sp
        )
        // Счет
        Text(
            text = "Счет: $score",
            color = Color.White,
            fontSize = 18.sp
        )
        // Баланс монет
        Text(
            text = "🪙 $coins",
            color = Color.White,
            fontSize = 18.sp
        )
    }
}
