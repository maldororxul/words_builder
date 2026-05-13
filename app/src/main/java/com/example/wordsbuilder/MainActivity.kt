package com.example.wordsbuilder

import BackgroundManager
import GameBackground
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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.wordsbuilder.ui.theme.WordsBuilderTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.wordsbuilder.data.LevelManager
import com.example.wordsbuilder.ui.components.ShopScreen
import getSavedCampaignLevelIndex
import getSavedLanguage

class MainActivity : AppCompatActivity() {

    private lateinit var bgManager: BackgroundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bgManager = BackgroundManager(this)

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
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

                        // Задний фон отрисовывается всегда под всеми экранами
                        if (currentScreen != "shop") {
                            GameBackground(bgManager = bgManager)
                        }

                        when (currentScreen) {
                            "menu" -> {
                                val locales = AppCompatDelegate.getApplicationLocales()
                                val currentLocale = if (!locales.isEmpty) locales[0]?.language ?: "en" else "en"

                                val currentCampaignLevel = getSavedCampaignLevelIndex(LocalContext.current, currentLocale)
                                val totalLevels = LevelManager.getCampaignLevelsCount(context = LocalContext.current, currentLocale)

                                val isCampaignFinished = currentCampaignLevel > totalLevels
                                // Используем монеты из нашего менеджера
                                val currentCoins = bgManager.coins

                                Box(modifier = Modifier.fillMaxSize()) {
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

                                    Text(
                                        text = "🪙 $currentCoins",
                                        fontSize = 22.sp,
                                        color = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 16.dp, end = 16.dp)
                                    )

                                    // Маленькая аккуратная кнопка Магазина поверх Главного Меню
                                    Button(
                                        onClick = { currentScreen = "shop" },
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 80.dp)
                                    ) {
                                        Text(text = stringResource(id = R.string.backgrounds_store))
                                    }
                                }
                            }
                            "game" -> {
                                GameScreen(
                                    gameMode = gameMode,
                                    paddingValues = innerPadding,
                                    onBackToMenu = { currentScreen = "menu" }
                                )
                            }
                            "shop" -> {
                                ShopScreen(
                                    bgManager = bgManager,
                                    onBack = { currentScreen = "menu" }
                                )
                            }
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
        SoundManager.pauseMusic()
    }
}
