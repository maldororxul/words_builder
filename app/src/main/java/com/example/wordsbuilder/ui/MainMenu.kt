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
    val context = LocalContext.current
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
            val menuButtonModifier = Modifier.fillMaxWidth().height(50.dp)

            // Кампания (становится непрозрачной и заблокированной по прохождении)
            Button(
                onClick = {
                    SoundManager.playSound(context, R.raw.click)
                    onStartCampaign()
                },
                enabled = !isCampaignFinished,
                modifier = menuButtonModifier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF333333),
                    disabledContentColor = Color.Gray
                )
            ) {
                Text(
                    text = if (isCampaignFinished) stringResource(id = R.string.campaign_finished) else stringResource(id = R.string.start_game, currentLevelId),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Случайный уровень
            Button(
                onClick = {
                    SoundManager.playSound(context, R.raw.click)
                    onStartRandom()
                },
                modifier = menuButtonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.White)
            ) {
                Text(text = stringResource(id = R.string.random_level), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // Магазин фонов
            Button(
                onClick = {
                    SoundManager.playSound(context, R.raw.click)
                    onOpenShop()
                },
                modifier = menuButtonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = Color.White)
            ) {
                Text(text = stringResource(id = R.string.backgrounds_store), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // Статистика
            Button(
                onClick = {
                    SoundManager.playSound(context, R.raw.click)
                    onOpenStats()
                },
                modifier = menuButtonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B), contentColor = Color.White)
            ) {
                Text(text = stringResource(id = R.string.statistics), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // Настройки
            Button(
                onClick = {
                    SoundManager.playSound(context, R.raw.click)
                    onOpenSettings()
                },
                modifier = menuButtonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF795548), contentColor = Color.White)
            ) {
                Text(text = stringResource(id = R.string.settings), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Подвал / Отступ снизу для балансировки интерфейса
        Spacer(modifier = Modifier.height(16.dp))
    }
}
