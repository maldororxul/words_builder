package com.example.wordsbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R
import com.example.wordsbuilder.data.PassiveIncomeManager
import kotlinx.coroutines.delay

@Composable
fun PassiveIncomeRow(
    incomeManager: PassiveIncomeManager,
    onCoinsClaimed: (Int) -> Unit, // Коллбэк для обновления кошелька в MainActivity
    modifier: Modifier = Modifier
) {
    // Дробное число монет, обновляемое каждую секунду
    var currentProgressCoins by remember { mutableDoubleStateOf(incomeManager.calculateAccumulatedCoins()) }

    // Бесконечный цикл корутины: тикает раз в секунду, пока открыто Главное Меню
    LaunchedEffect(Unit) {
        while (true) {
            currentProgressCoins = incomeManager.calculateAccumulatedCoins()
            delay(1000L)
        }
    }

    // Вычисляем целое доступное число монет для забора
    val integerCoinsToClaim = currentProgressCoins.toInt()
    val isButtonEnabled = integerCoinsToClaim >= 1

    // Рассчитываем долю для прогресс-бара (от 0.0f до 1.0f)
    val progressFraction = (currentProgressCoins / incomeManager.maxCoinsLimit).toFloat().coerceIn(0f, 1f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(0.7f)
                .padding(end = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(id = R.string.bonus_coins), color = Color.White, fontSize = 14.sp)
                Text(
                    text = "$integerCoinsToClaim",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            // Линейный индикатор прогресса накопления
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFFFC107), // Золотой цвет заполнения
                trackColor = Color.Gray.copy(alpha = 0.5f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
        // Кнопка "Get!" занимает ровно оставшиеся 30% благодаря весу
        Box(
            modifier = Modifier.weight(0.3f),
            contentAlignment = Alignment.CenterEnd
        ) {
            GameButton(
                text = stringResource(id = R.string.get),
                enabled = isButtonEnabled,
                onClick = {
                    // Передаем целую часть накопленных монет в MainActivity для записи на баланс
                    onCoinsClaimed(integerCoinsToClaim)
                    // Обнуляем счетчик времени в памяти (записываем текущий момент)
                    incomeManager.resetTimestamp()
                    // Мгновенно обновляем стейт на UI до нуля
                    currentProgressCoins = incomeManager.calculateAccumulatedCoins()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}