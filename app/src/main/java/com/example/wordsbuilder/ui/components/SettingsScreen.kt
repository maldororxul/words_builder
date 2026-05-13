package com.example.wordsbuilder.ui

import BackgroundManager
import GameBackground
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R
import changeLocale
import saveRandomWordsCount
import getRandomWordsCount

@Composable
fun SettingsScreen(bgManager: BackgroundManager, onBack: () -> Unit) {
    val context = LocalContext.current
    var musicVol by remember { mutableFloatStateOf(SoundManager.musicVolume) }
    var soundVol by remember { mutableFloatStateOf(SoundManager.soundVolume) }
    var expanded by remember { mutableStateOf(false) }

    val locales = AppCompatDelegate.getApplicationLocales()
    val currentLocale = if (!locales.isEmpty) locales[0]?.language ?: "en" else "en"

    BackHandler {
        onBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GameBackground(bgManager = bgManager)
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Верхняя панель: Кнопка Назад (в точности как в ShopScreen)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        SoundManager.playSound(context, R.raw.click)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    // Используем ваш ресурс для локализации кнопки назад
                    Text(text = stringResource(id = R.string.back), color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Заголовок экрана настроек
            Text(text = stringResource(id = R.string.settings), fontSize = 28.sp, color = Color.White)

            Spacer(modifier = Modifier.height(32.dp))

            // Ползунок музыки
            Text(
                text = stringResource(R.string.music, (musicVol * 100).toInt()),
                color = Color.White,
                fontSize = 18.sp
            )
            Slider(
                value = musicVol,
                onValueChange = {
                    musicVol = it
                    SoundManager.updateMusicVolume(context, it)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ползунок звуков
            Text(
                text = stringResource(R.string.sounds, (soundVol * 100).toInt()),
                color = Color.White,
                fontSize = 18.sp
            )
            Slider(
                value = soundVol,
                onValueChange = {
                    soundVol = it
                    SoundManager.updateSoundVolume(context, it)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Ползунок для выбора количества слов (5-9)
            var randomWordsCount by remember { mutableIntStateOf(getRandomWordsCount(context)) }

            Text(stringResource(R.string.random_level_words_count, randomWordsCount), color = Color.White, fontSize = 18.sp)
            Slider(
                value = randomWordsCount.toFloat(),
                onValueChange = {
                    val roundedValue = it.toInt()
                    randomWordsCount = roundedValue
                    saveRandomWordsCount(context, roundedValue) // Сохраняем целое число на лету
                },
                valueRange = 5f..9f, // Ограничиваем диапазон ползунка
                steps = 3,          // Задает ровно 5 делений: 5, 6, 7, 8, 9
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Ваш оригинальный выпадающий список выбора трех языков
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(text = stringResource(id = R.string.language))
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF222222))
                ) {
                    DropdownMenuItem(
                        text = { Text("English", color = Color.White) },
                        onClick = {
                            expanded = false
                            if (currentLocale != "en") changeLocale(context = context, langCode = "en")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Español", color = Color.White) },
                        onClick = {
                            expanded = false
                            if (currentLocale != "es") changeLocale(context = context, langCode = "es")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Русский", color = Color.White) },
                        onClick = {
                            expanded = false
                            if (currentLocale != "ru") changeLocale(context = context, langCode = "ru")
                        }
                    )
                }
            }
        }
    }
}
