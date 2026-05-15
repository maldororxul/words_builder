import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R
import com.example.wordsbuilder.ui.components.GameButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign

@Composable
fun LevelCompleteOverlay(
    gameMode: String,
    levelReward: Int,
    context: Context,
    onCoinsUpdate: (Int) -> Unit,
    onNextLevel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAdWatched by remember { mutableStateOf(false) }
    val displayReward = if (isAdWatched) levelReward * 2 else levelReward

    // Начисляем базовую награду ОДИН раз автоматически при открытии экрана победы
    LaunchedEffect(Unit) {
        val currentCoins = getSavedCoins(context)
        val updatedCoins = currentCoins + levelReward
        saveCoins(context, updatedCoins)
        onCoinsUpdate(updatedCoins)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xAA000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(
                    color = Color(0xFF2C3E50),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "УРОВЕНЬ ЗАВЕРШЕН!",
                color = Color(0xFFF1C40F),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Ваша награда:",
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    text = "🪙 $displayReward",
                    color = Color(0xFF2ECC71),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isAdWatched) {
                GameButton(
                    text = "Смотреть рекламу x2 📺",
                    onClick = {
                        isAdWatched = true
                        // Реклама просмотрена: доначисляем еще раз сумму levelReward (удвоение)
                        val currentCoins = getSavedCoins(context)
                        val updatedCoins = currentCoins + levelReward
                        saveCoins(context, updatedCoins)
                        onCoinsUpdate(updatedCoins)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Награда удвоена! 🎉",
                    color = Color(0xFFE67E22),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            GameButton(
                text = "Далее",
                onClick = onNextLevel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}