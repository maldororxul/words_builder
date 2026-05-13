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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
                enabled = !isCampaignFinished // Кнопка блокируется, если кампания пройдена
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
            OutlinedButton(onClick = onStartRandom) {
                Text(text = stringResource(id = R.string.random_level))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Язык
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(text = stringResource(id = R.string.language))
                }
                val context = LocalContext.current
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("English") },
                        onClick = {
                            expanded = false
                            if (currentLocale != "en") changeLocale(context = context, langCode = "en")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Español") },
                        onClick = {
                            expanded = false
                            if (currentLocale != "es") changeLocale(context = context, langCode = "es")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Русский") },
                        onClick = {
                            expanded = false
                            if (currentLocale != "ru") changeLocale(context = context, langCode = "ru")
                        }
                    )
                }
            }
        }
    }
}