import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@Composable
fun LevelCompleteOverlay(
    gameMode: String,
    levelReward: Int,
    context: Context,
    onCoinsUpdate: (Int) -> Unit,     // ← добавили
    onNextLevel: () -> Unit
) {
    LaunchedEffect(gameMode, levelReward) {
        SoundManager.playSound(context, R.raw.victory)
        if (gameMode == "campaign") {
            val currentCoins = getSavedCoins(context)
            val newCoins = currentCoins + levelReward

            saveCoins(context, newCoins)
            onCoinsUpdate(newCoins)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.level_completed),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            GameButton(text = stringResource(id = R.string.next_level), onClick = onNextLevel)
        }
    }
}