import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun GameBottomPanel(
    currentWord: String,
    totalScore: Int,
    coins: Int,
    wheelLetters: List<Char>,
    targetWords: List<String>,
    campaignLevelId: Int?,
    onWordComposed: (String) -> Unit,
    onHintClick: () -> Unit
) {
    // Колесо со всеми оверлеями
    WordWheelContainer(
        letters = wheelLetters,
        targetWords = targetWords,
        totalScore = totalScore,
        coins = coins,
        campaignLevelId = campaignLevelId,
        onWordComposed = onWordComposed,
        onHintClick = onHintClick
    )
    Spacer(modifier = Modifier.height(10.dp))
    // Баннер рекламы
//    AdBannerPlaceholder()
}