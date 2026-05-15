package com.example.wordsbuilder.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R

@Composable
fun LevelStartOverlay(
    levelId: Int,
    splashResName: String,
    description: String,
    context: Context,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Получаем ID картинки по ее строковому имени из json
    val imageResId = context.resources.getIdentifier(splashResName, "drawable", context.packageName)
    val painter = if (imageResId != 0) painterResource(id = imageResId) else painterResource(android.R.drawable.ic_menu_gallery)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xEE1A1A1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color(0xFF2C3E50), shape = RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.level, levelId),
                color = Color(0xFFF1C40F),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            // Заставка уровня
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.Black, shape = RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            // Описание
            Text(
                text = description,
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            GameButton(
                text = stringResource(id = R.string.go),
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}