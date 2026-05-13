package com.example.wordsbuilder

import com.example.wordsbuilder.ui.GameScreen
import MainMenu
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.wordsbuilder.ui.theme.WordsBuilderTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import com.example.wordsbuilder.data.LevelManager
import getSavedCampaignLevelIndex
import getSavedCoins
import getSavedLanguage

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Загружаем сохраненный язык и применяем его
        val savedLang = getSavedLanguage(this)
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(savedLang)
        AppCompatDelegate.setApplicationLocales(appLocale)

        SoundManager.init(this)
        SoundManager.startMusic(this)
        enableEdgeToEdge()
        setContent {
            WordsBuilderTheme {
                var currentScreen by rememberSaveable { mutableStateOf("menu") }
                var gameMode by rememberSaveable { mutableStateOf("campaign") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (currentScreen == "menu") {

                            // Получаем текущую локаль системы/приложения
                            val locales = AppCompatDelegate.getApplicationLocales()
                            val currentLocale = if (!locales.isEmpty) locales[0]?.language ?: "en" else "en"

                            // Читаем сохраненный уровень кампании для этой локали
                            val currentCampaignLevel = getSavedCampaignLevelIndex(LocalContext.current, currentLocale)
                            val totalLevels = LevelManager.getCampaignLevelsCount(context = LocalContext.current, currentLocale)

                            // Если текущий уровень игрока строго больше, чем всего уровней в JSON
                            val isCampaignFinished = currentCampaignLevel > totalLevels

                            val currentCoins = getSavedCoins(LocalContext.current)

                            MainMenu(
                                currentLevelId = currentCampaignLevel,
                                isCampaignFinished = isCampaignFinished,
                                coins = currentCoins,
                                onStartCampaign = {
                                    gameMode = "campaign"
                                    currentScreen = "game"
                                },
                                onStartRandom = {
                                    gameMode = "random"
                                    currentScreen = "game"
                                }
                            )
                        } else {
                            GameScreen(
                                gameMode = gameMode, // Передаем режим игры
                                paddingValues = innerPadding,
                                onBackToMenu = { currentScreen = "menu" }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SoundManager.startMusic(this)
    }

    override fun onPause() {
        super.onPause()
        // Приостанавливаем, а не удаляем, чтобы продолжить с того же места
        SoundManager.pauseMusic()
    }
}









