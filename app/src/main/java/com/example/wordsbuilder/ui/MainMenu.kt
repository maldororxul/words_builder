import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R
import com.example.wordsbuilder.ui.components.GameButton
import com.example.wordsbuilder.ui.components.PassiveIncomeRow

@Composable
fun MainMenu(
    currentLevelId: Int,
    isCampaignFinished: Boolean,
    coins: Int,
    onStartCampaign: () -> Unit,
    onStartRandom: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Логотип игры
        Image(
            painter = painterResource(id = R.drawable.ic_game_logo),
            contentDescription = "Game Logo",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(180.dp)
                .padding(top = 32.dp),
            contentScale = ContentScale.Fit
        )
        // Блок кнопок меню (Один под другим, общая ширина)
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Кампания (становится непрозрачной и заблокированной по прохождении)
            GameButton(
                text = if (isCampaignFinished) stringResource(id = R.string.campaign_finished) else
                    stringResource(id = R.string.start_game, currentLevelId),
                enabled = !isCampaignFinished,
                onClick = onStartCampaign
            )
            // Случайный уровень
            GameButton(text = stringResource(id = R.string.random_level), onClick = onStartRandom)
            // Магазин фонов
            GameButton(text = stringResource(id = R.string.backgrounds_store), onClick = onOpenShop)
            // Статистика
            GameButton(text = stringResource(id = R.string.statistics), onClick = onOpenStats)
            // Настройки
            GameButton(text = stringResource(id = R.string.settings), onClick = onOpenSettings)
        }
        // Подвал / Отступ снизу для балансировки интерфейса
        Spacer(modifier = Modifier.height(16.dp))
    }
}
