package com.example.wordsbuilder.ui

import CurrentWordDisplay
import ExitConfirmationDialog
import LevelCompleteOverlay
import LevelInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.wordsbuilder.data.LevelManager
import com.example.wordsbuilder.domain.game.RandomWordGenerator
import com.example.wordsbuilder.ui.components.CrosswordGrid
import com.example.wordsbuilder.ui.components.GameHintButton
import com.example.wordsbuilder.ui.components.GameLevelStats
import com.example.wordsbuilder.ui.components.WordWheel
import com.example.wordsbuilder.ui.dialogs.DefinitionDialog
import com.example.wordsbuilder.ui.dialogs.HintConfirmationDialog
import generateCrossword
import getRandomWordsCount
import getSavedCampaignLevelIndex
import getSavedCoins
import getSavedLanguage
import getSavedLevelProgress
import getSavedScore
import handleWordInput
import saveCampaignLevelIndex
import saveCoins
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

    var lastSolvedWord by remember { mutableStateOf("") }
    var wordFlyTrigger by remember { mutableIntStateOf(0) }

    // Состояния для показа определений
    var targetedWordForDefinition by remember { mutableStateOf("") }
    var showDefinitionDialog by remember { mutableStateOf(false) }

    // Основные состояния
    var coins by remember { mutableIntStateOf(getSavedCoins(context)) }
    var totalScore by remember { mutableIntStateOf(getSavedScore(context)) }
    var showHintDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var randomLevelCounter by rememberSaveable { mutableIntStateOf(1) }
    var campaignLevelId by rememberSaveable {
        mutableIntStateOf(getSavedCampaignLevelIndex(context, currentLocale))
    }

    val screenKey = rememberSaveable { mutableIntStateOf(0) }

    // ШАГ 1: Загрузка данных уровня в Map структуры с УМНЫМ ПОДБОРОМ для случайного режима
    val currentLevelWordsMap = remember(currentLocale, randomLevelCounter, campaignLevelId, gameMode, screenKey.intValue) {
        if (gameMode == "campaign") {
            val allLevels = LevelManager.loadCampaignLevels(context, currentLocale)
            val currentLevel = allLevels.find { it.id == campaignLevelId } ?: allLevels.firstOrNull()
            currentLevel?.words ?: emptyMap()
        } else {
            // ЧИТАЕМ НАСТРОЙКУ: количество слов из ползунка настроек
            val targetCount = getRandomWordsCount(context)
            // "кучный" набор слов с общим алфавитом не более 15 букв
            RandomWordGenerator.getRandomWordsMap(context, currentLocale, targetCount)
        }
    }

    // ШАГ 2: Генерация сетки кроссворда и подготовка колеса букв
    val levelData = remember(currentLevelWordsMap) {
        // Берем слова, которые нам уже гарантированно правильно подобрал первый блок
        val targetWords = currentLevelWordsMap.keys.toList()
        var reward = 50
        var hintCost = 20

        if (gameMode == "campaign") {
            val allLevels = LevelManager.loadCampaignLevels(context, currentLocale)
            val currentLevel = allLevels.find { it.id == campaignLevelId } ?: allLevels.firstOrNull()
            currentLevel?.let {
                reward = it.reward
                hintCost = it.hintCost
            }
        } else {
            reward = 10
            hintCost = 20
        }

        val sortedWords = targetWords.sortedByDescending { it.length }
        val grid = generateCrossword(sortedWords)

        // ВАЖНОЕ ИСПРАВЛЕНИЕ: Мультисет максимумов для букв колеса
        val builtWords = grid.map { it.word.lowercase() }
        val finalLettersMap = mutableMapOf<Char, Int>()

        builtWords.forEach { word ->
            val wordFreq = mutableMapOf<Char, Int>()
            word.forEach { char -> wordFreq[char] = wordFreq.getOrDefault(char, 0) + 1 }

            // Гарантируем, что на колесо попадет МАКСИМАЛЬНОЕ количество повторений, нужное для ЭТОГО слова
            wordFreq.forEach { (char, count) ->
                finalLettersMap[char] = maxOf(finalLettersMap.getOrDefault(char, 0), count)
            }
        }

        // Разворачиваем карту частот обратно в плоский список букв с дубликатами и перемешиваем
        val allWheelLetters = finalLettersMap.flatMap { (char, count) ->
            List(count) { char.uppercaseChar() }
        }.shuffled()

        LevelInfo(
            words = grid.map { it.word },
            letters = allWheelLetters, // Передаем на колесо честный, минимально достаточный набор букв (всегда <= 15)
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
        Column(modifier = Modifier.fillMaxSize()) {
            // === КРОССВОРД ===
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                CrosswordGrid(
                    placedWords = crosswordGrid,
                    solvedWords = solvedWords,
                    selectedWord = if (showDefinitionDialog) targetedWordForDefinition else null, // Подсвечиваем слово, пока открыт диалог
                    onWordLongPressed = { word ->
                        targetedWordForDefinition = word
                        showDefinitionDialog = true // Открываем всплывающее окно
                    },
                    modifier = Modifier.fillMaxSize()
                )
                GameHintButton(
                    onClick = { showHintDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd) // Смещение в левый нижний угол
                        .padding(end = 9.dp, bottom = 9.dp) // Безопасные отступы от краев колеса
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Текущее слово
                CurrentWordDisplay(
                    currentWord = currentWord,
                    triggerWord = lastSolvedWord,
                    flyTrigger = wordFlyTrigger,
                    modifier = Modifier.fillMaxWidth()
                )
                // Колесо со всеми оверлеями
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    WordWheel(
                        modifier = Modifier.zIndex(1f),
                        letters = wheelLetters,
                        targetWords = targetWords,
                        onWordComposed = { input ->
                            val isSuccess = handleWordInput(
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

                            if (isSuccess) {
                                // Фиксируем слово для анимации, пока оно не стерлось
                                lastSolvedWord = currentWord
                                // Увеличиваем счетчик, чтобы запустить WordFlyUpEffect
                                wordFlyTrigger++
                            }
                        },
                    )
                }
                // полоса статистики
                GameLevelStats(
                    coins = coins,
                    levelNumber = campaignLevelId,
                    score = totalScore,
                    gameMode = gameMode,
                    modifier = Modifier.padding(top = 4.dp, bottom = 0.dp)
                )
            }
        }

        // === ОКНО ПОБЕДЫ ===
        if (isLevelComplete) {
            LevelCompleteOverlay(
                levelReward = levelReward,
                context = context,
                onCoinsUpdate = { newCoins ->
                    coins = newCoins
                },
                onNextLevel = {
                    if (gameMode == " campaign") {
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
        hintCost = hintCost, // Передаем динамическую стоимость подсказки из JSON конфигурации уровня
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
    // ВСПЛЫВАЮЩИЙ ДИАЛОГ ОПРЕДЕЛЕНИЯ СЛОВА
    DefinitionDialog(
        visible = showDefinitionDialog,
        word = targetedWordForDefinition,
        definition = currentLevelWordsMap[targetedWordForDefinition] ?: "Определение не найдено.",
        solvedWords = solvedWords,
        placedWords = crosswordGrid, // Передаем сетку кроссворда
        onDismiss = { showDefinitionDialog = false }
    )
}