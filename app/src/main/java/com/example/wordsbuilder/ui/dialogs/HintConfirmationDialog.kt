package com.example.wordsbuilder.ui.dialogs

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wordsbuilder.R
import com.example.wordsbuilder.ui.components.GameButton
import saveCoins
import saveCurrentLevelProgress

@Composable
fun HintConfirmationDialog(
    visible: Boolean,
    hintCost: Int,
    coins: Int,
    targetWords: List<String>,
    solvedWords: Set<String>, // Оставляем родной Set<String> из вашего репозитория
    gameMode: String,
    context: Context,
    onDismiss: () -> Unit,
    onConfirm: (Int, Set<String>) -> Unit // Родная сигнатура из репозитория на 2 параметра
) {
    if (visible) {
        Dialog(
            onDismissRequest = {
                SoundManager.playSound(context, R.raw.click)
                onDismiss()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            // Красивый кастомный игровой фон затемнения
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                // Игровая карточка диалога
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
                        // Заголовок
                        Text(
                            text = stringResource(id = R.string.hint_confirm_title),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Описание
                        Text(
                            text = stringResource(id = R.string.hint_confirm_msg, hintCost),
                            fontSize = 16.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Кнопки в едином плотном игровом стиле
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val buttonModifier = Modifier.fillMaxWidth().height(48.dp)

                            // Кнопка подтверждения
                            GameButton(
                                text = stringResource(id = R.string.yes),
                                onClick = {
                                    SoundManager.playSound(context, R.raw.click)
                                    if (coins >= hintCost) {
                                        // Пишем в созданную ранее статистику подсказок
                                        com.example.wordsbuilder.data.StatsManager(context).hintsUsed += 1

                                        val newCoins = coins - hintCost
                                        saveCoins(context, newCoins)

                                        val unrevealedWords = targetWords.filter { !solvedWords.contains(it) }
                                        if (unrevealedWords.isNotEmpty()) {
                                            val wordToReveal = unrevealedWords.first()
                                            val newSolvedWords = solvedWords + wordToReveal

                                            if (gameMode == "campaign") {
                                                saveCurrentLevelProgress(context, newSolvedWords)
                                            }

                                            onConfirm(newCoins, newSolvedWords)
                                        } else {
                                            onConfirm(newCoins, solvedWords)
                                        }
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.not_enough_coins), Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                },
                                containerColor = Color(0xFF4CAF50)
                            )
                            // Кнопка отмены
                            GameButton(
                                text = stringResource(id = R.string.no),
                                onClick = onDismiss,
                                containerColor = Color(0xFFF44336)
                            )
                        }
                    }
                }
            }
        }
    }
}
