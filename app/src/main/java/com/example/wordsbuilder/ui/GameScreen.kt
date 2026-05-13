package com.example.wordsbuilder.ui

import CrosswordGrid
import ExitConfirmationDialog
import GameBottomPanel
import HintConfirmationDialog
import LevelCompleteOverlay
import LevelInfo
import VideoBackground
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.wordsbuilder.R
import com.example.wordsbuilder.data.LevelManager
import generateCrossword
import generateLevel
import getSavedCampaignLevelIndex
import getSavedCoins
import getSavedLanguage
import getSavedLevelProgress
import getSavedScore
import handleWordInput
import saveCampaignLevelIndex
import saveCurrentLevelProgress
import saveScore

@Composable
fun GameScreen(
    gameMode: String,
    paddingValues: PaddingValues,
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val currentLocale = remember { getSavedLanguage(context) }

    // Основные состояния
    var coins by remember { mutableIntStateOf(getSavedCoins(context)) }
    var totalScore by remember { mutableStateOf(getSavedScore(context)) }
    var showHintDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    var randomLevelCounter by rememberSaveable { mutableIntStateOf(1) }
    var campaignLevelId by rememberSaveable {
        mutableIntStateOf(getSavedCampaignLevelIndex(context, currentLocale))
    }

    val screenKey = rememberSaveable { mutableIntStateOf(0) }

    // Загрузка данных уровня
    val levelData = remember(currentLocale, randomLevelCounter, campaignLevelId, gameMode) {
        var reward = 50
        var hintCost = 20
        var targetWords = listOf<String>()

        if (gameMode == "campaign") {
            val allLevels = LevelManager.loadCampaignLevels(context, currentLocale)
            val currentLevel = allLevels.find { it.id == campaignLevelId } ?: allLevels.firstOrNull()
            currentLevel?.let {
                targetWords = it.words
                reward = it.reward
                hintCost = it.hintCost
            }
        } else {
            val dictionary = DictionaryManager.loadDictionary(context, currentLocale)
            targetWords = generateLevel(dictionary).first
        }

        val sortedWords = targetWords.sortedByDescending { it.length }
        val grid = generateCrossword(sortedWords)
        val wheelLetters = (sortedWords.firstOrNull() ?: "").toList().shuffled()

        LevelInfo(
            words = grid.map { it.word },
            letters = wheelLetters,
            grid = grid,
            reward = reward,
            hintCost = hintCost
        )
    }

    val targetWords = levelData.words
    val wheelLetters = levelData.letters
    val crosswordGrid = levelData.grid
    val levelReward = levelData.reward
    val hintCost = levelData.hintCost

    var currentWord by remember { mutableStateOf("") }

    // Прогресс уровня
    var solvedWords: Set<String> by remember {
        mutableStateOf(
            if (gameMode == "campaign") getSavedLevelProgress(context) else emptySet()
        )
    }

    val isLevelComplete = solvedWords.size == targetWords.size && targetWords.isNotEmpty()

    // Музыка
    DisposableEffect(Unit) {
        SoundManager.startMusic(context)
        onDispose { SoundManager.stopMusic() }
    }

    BackHandler { showExitDialog = true }

    Box(modifier = Modifier.fillMaxSize()) {

        // Фон
        key(screenKey.intValue) {
            VideoBackground(videoResId = R.raw.background)
        }

        Column(modifier = Modifier.fillMaxSize()) {

            // === КРОССВОРД ===
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CrosswordGrid(
                    placedWords = crosswordGrid,
                    solvedWords = solvedWords,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // === НИЖНЯЯ ПАНЕЛЬ ===
            GameBottomPanel(
                currentWord = currentWord,
                totalScore = totalScore,
                coins = coins,
                wheelLetters = wheelLetters,
                targetWords = targetWords,
                campaignLevelId = if (gameMode == "campaign") campaignLevelId else null,
                onWordComposed = { input ->
                    handleWordInput(
                        input = input,
                        targetWords = targetWords,
                        solvedWords = solvedWords,
                        gameMode = gameMode,
                        context = context,
                        onSolvedUpdate = { newSolved ->
                            solvedWords = newSolved
                        },
                        onScoreUpdate = { pointsToAdd ->
                            totalScore += pointsToAdd
                            saveScore(context, totalScore)
                        },
                        onCurrentWordChange = { currentWord = it }
                    )
                },
                onHintClick = { showHintDialog = true }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // === ОКНО ПОБЕДЫ ===
        if (isLevelComplete) {
            LevelCompleteOverlay(
                gameMode = gameMode,
                levelReward = levelReward,
                context = context,
                onCoinsUpdate = { newCoins ->
                    coins = newCoins
                },
                onNextLevel = {
                    if (gameMode == "campaign") {
                        campaignLevelId++
                        saveCampaignLevelIndex(context, currentLocale, campaignLevelId)
                        saveCurrentLevelProgress(context, emptySet())
                    } else {
                        randomLevelCounter++
                    }
                    solvedWords = emptySet()
                    currentWord = ""
                    screenKey.intValue++
                }
            )
        }
    }

    // === ДИАЛОГИ ===
    ExitConfirmationDialog(
        visible = showExitDialog,
        onDismiss = { showExitDialog = false },
        onConfirm = onBackToMenu
    )
    HintConfirmationDialog(
        visible = showHintDialog,
        hintCost = hintCost,
        coins = coins,
        targetWords = targetWords,
        solvedWords = solvedWords,
        gameMode = gameMode,
        context = context,
        onDismiss = { showHintDialog = false },
        onConfirm = { newCoins, newSolved ->
            coins = newCoins
            solvedWords = newSolved
        }
    )
}