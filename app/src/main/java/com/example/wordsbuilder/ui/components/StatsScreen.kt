package com.example.wordsbuilder.ui

import BackgroundManager
import GameBackground
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R
import com.example.wordsbuilder.data.StatsManager


@Composable
fun StatsScreen(bgManager: BackgroundManager, onBack: () -> Unit) {
    val context = LocalContext.current
    val statsManager = remember { StatsManager(context) }

    BackHandler {
        onBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GameBackground(bgManager = bgManager)
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = {
                        SoundManager.playSound(context, R.raw.click)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text(text = stringResource(id = R.string.back), color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(text = stringResource(id = R.string.statistics), fontSize = 28.sp, color = Color.White)
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatRow(stringResource(id = R.string.total_score), "${statsManager.totalScore}")
                StatRow(stringResource(id = R.string.total_words_solved), "${statsManager.totalWordsSolved}")
                StatRow(stringResource(id = R.string.uniq_words_solved), "${statsManager.getUniqueWordsCount()}")
                StatRow(stringResource(id = R.string.hints_taken), "${statsManager.hintsUsed}")
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.LightGray, fontSize = 18.sp)
        Text(text = value, color = Color.White, fontSize = 18.sp)
    }
}
