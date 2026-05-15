package com.example.wordsbuilder.ui.dialogs

import PlacedWord
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wordsbuilder.R
import com.example.wordsbuilder.ui.components.GameButton


@Composable
fun DefinitionDialog(
    visible: Boolean,
    word: String,
    definition: String,
    solvedWords: Set<String>,
    placedWords: List<PlacedWord>, // Передаем сетку для вычисления пересечений
    onDismiss: () -> Unit
) {
    if (visible && word.isNotEmpty()) {
        val context = LocalContext.current

        val isWordFullySolved = remember(word, solvedWords) {
            solvedWords.contains(word.lowercase())
        }

        // ВЫЧИСЛЕНИЕ ОТКРЫТЫХ БУКВ ЧЕРЕЗ ПЕРЕСЕЧЕНИЯ СЕТКИ
        val displayWord = remember(word, isWordFullySolved, solvedWords, placedWords) {
            if (isWordFullySolved) {
                word.uppercase().map { it.toString() }.joinToString(" ")
            } else {
                // Приводим целевое слово к нижнему регистру для надежного поиска в сетке
                val currentPlacedWord = placedWords.find { it.word.lowercase() == word.lowercase() }

                if (currentPlacedWord != null) {
                    word.mapIndexed { index, char ->
                        // Вычисляем глобальные координаты X и Y для каждой буквы этого слова
                        val charX = if (currentPlacedWord.isHorizontal) currentPlacedWord.x + index else currentPlacedWord.x
                        val charY = if (currentPlacedWord.isHorizontal) currentPlacedWord.y else currentPlacedWord.y + index

                        // Принудительно приводим весь сет отгаданных слов к нижнему регистру
                        val lowerSolvedWords = solvedWords.map { it.lowercase() }.toSet()

                        // Буква открыта, если эта координата (X, Y) пересекается ХОТЯ БЫ с одним разгаданным словом
                        val isLetterOpenedByCrossroad = placedWords.any { pw ->
                            lowerSolvedWords.contains(pw.word.lowercase()) && pw.word.indices.any { i ->
                                val px = if (pw.isHorizontal) pw.x + i else pw.x
                                val py = if (pw.isHorizontal) pw.y else pw.y + i
                                px == charX && py == charY
                            }
                        }

                        if (isLetterOpenedByCrossroad) char.toString().uppercase() else "?"
                    }.joinToString(" ")
                } else {
                    word.map { "?" }.joinToString(" ")
                }
            }
        }

        Dialog(
            onDismissRequest = {
                SoundManager.playSound(context, R.raw.click)
                onDismiss()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = displayWord,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isWordFullySolved) Color(0xFFFF8000 ) else Color.LightGray,
                            textAlign = TextAlign.Center,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = definition.ifEmpty { "Определение отсутствует." },
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        GameButton(
                            text = "OK",
                            onClick = {
                                SoundManager.playSound(context, R.raw.click)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )
                    }
                }
            }
        }
    }
}