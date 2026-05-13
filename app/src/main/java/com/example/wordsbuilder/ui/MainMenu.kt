import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onStartCampaign: () -> Unit, // Новый параметр
    onStartRandom: () -> Unit,   // Новый параметр
) {
    val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Лого
            Image(
                painter = painterResource(id = R.drawable.ic_game_logo),
                contentDescription = "Game Logo",
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Занимает 80% ширины экрана
                    .height(180.dp)     // Фиксированная высота для адаптивности
                    .padding(top = 32.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка Кампании с номером уровня
            Button(
                onClick = onStartCampaign,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                enabled = !isCampaignFinished,
                modifier = Modifier
            ) {
                Text(
                    text = if (isCampaignFinished) {
                        stringResource(id = R.string.campaign_finished)
                    } else {
                        stringResource(id = R.string.start_game, currentLevelId)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Кнопка запуска Случайного уровня
            // Не забудь добавить строку random_mode в strings.xml, либо пока напиши текст вручную для проверки
            OutlinedButton(
                onClick = onStartRandom,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                ),
                modifier = Modifier
            ) {
                Text(text = stringResource(id = R.string.random_level))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}