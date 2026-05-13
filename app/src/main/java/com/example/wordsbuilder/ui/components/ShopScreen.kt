package com.example.wordsbuilder.ui.components

import BackgroundManager
import GameBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R


@Composable
fun ShopScreen(bgManager: BackgroundManager, onBack: () -> Unit) {
    var playerCoins by remember { mutableIntStateOf(bgManager.coins) }
    var selectedId by remember { mutableStateOf(bgManager.selectedBackgroundId) }
    val backgrounds = remember { bgManager.loadBackgrounds() }
    // Состояние текущей вкладки: 0 - Выбор фона (Гардероб), 1 - Купить новые (Магазин)
    var selectedTab by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Живой предпросмотр выбранного фона прямо на заднем плане экрана
        GameBackground(bgManager = bgManager, selectedId = selectedId)

        // Полупрозрачная маска, чтобы текст интерфейса лучше читался поверх видео/картинок
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Верхняя панель: Назад и Баланс монет
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text(text = stringResource(id = R.string.back), color = Color.White)
                }
                Text("🪙 $playerCoins", fontSize = 20.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Переключатель вкладок: Гардероб / Магазин
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(text = stringResource(id = R.string.my_collection), fontSize = 16.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(text = stringResource(id = R.string.buy_backgrounds), fontSize = 16.sp) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Фильтрация списка в зависимости от выбранной вкладки
            val filteredBackgrounds = remember(selectedTab, selectedId, playerCoins) {
                backgrounds.filter { bg ->
                    val isPurchased = bgManager.isPurchased(bg.id)
                    if (selectedTab == 0) isPurchased else !isPurchased
                }
            }

            if (filteredBackgrounds.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (selectedTab == 0) stringResource(id = R.string.no_backrounds_bought) else stringResource(id = R.string.all_backgrounds_bought),
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredBackgrounds) { bg ->
                        val isSelected = selectedId == bg.id
                        val localizedName = bgManager.getLocalizedName(bg)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.7f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = localizedName,
                                        fontSize = 18.sp,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                    Text(
                                        text = "\uD83C\uDFF7\uFE0F: ${if (bg.type == "video") stringResource(id = R.string.video_background) else stringResource(id = R.string.static_background)}",
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.DarkGray else Color.LightGray
                                    )
                                }

                                // Кнопки действия (Выбрать / Активен / Купить)
                                if (selectedTab == 0) {
                                    // Интерфейс выбора в Гардеробе
                                    if (isSelected) {
                                        Button(onClick = {}, enabled = false) {
                                            Text(text = stringResource(id = R.string.active))
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                // 1. Сохраняем ID нового фона в SharedPreferences через менеджер
                                                bgManager.selectedBackgroundId = bg.id
                                                // 2. Обновляем локальный стейт экрана Jetpack Compose
                                                selectedId = bg.id
                                            }
                                        ) {
                                            Text(text = stringResource(id = R.string.apply))
                                        }
                                    }
                                } else {
                                    // Интерфейс покупки в Магазине
                                    Button(
                                        onClick = {
                                            if (bgManager.buyBackground(bg)) {
                                                playerCoins = bgManager.coins
                                                bgManager.selectedBackgroundId = bg.id
                                                selectedId = bg.id
                                                // После покупки автоматически переводим пользователя в гардероб смотреть на новинку
                                                selectedTab = 0
                                            }
                                        },
                                        enabled = playerCoins >= bg.price,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Зеленая кнопка
                                    ) {
                                        Text("${bg.price} 🪙", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
