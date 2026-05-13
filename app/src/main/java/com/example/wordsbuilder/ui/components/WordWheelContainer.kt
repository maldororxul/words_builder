import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R

@Composable
fun WordWheelContainer(
    letters: List<Char>,
    targetWords: List<String>,
    totalScore: Int,
    coins: Int,
    campaignLevelId: Int?,
    onWordComposed: (String) -> Unit,
    onHintClick: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        WordWheel(
            letters = letters,
            targetWords = targetWords,
            onWordComposed = onWordComposed
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = totalScore.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = stringResource(id = R.string.score_label),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🪙 ", fontSize = 16.sp)
            Text(
                text = coins.toString(),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(
            onClick = {
                SoundManager.playSound(context, R.raw.click)
                onHintClick()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Text("?", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        campaignLevelId?.let {     Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.level_short, it),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        } }
    }
}