package com.example.wordsbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R

@Composable
fun GameLevelStats(
    coins: Int,
    levelNumber: Int,
    score: Int,
    gameMode: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
//            .padding(horizontal = 8.dp)
            .background(Color(0x80000000)) // Полупрозрачная подложка
            .padding(vertical = 4.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Уровень
        if (gameMode == "campaign") {
            Text(
                text = stringResource(id = R.string.level_short, levelNumber),
                color = Color.White,
                fontSize = 14.sp
            )
        }
        // Счет
        Text(
            text = stringResource(id = R.string.score_label, score),
            color = Color.White,
            fontSize = 14.sp
        )
        // Баланс монет
        Text(
            text = "🪙 $coins",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}
