import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsbuilder.R
import com.example.wordsbuilder.ui.components.WordFlyUpEffect

@Composable
fun CurrentWordDisplay(
    currentWord: String,
    triggerWord: String,    // Какое слово анимировать
    flyTrigger: Int,        // Триггер запуска анимации (счетчик)
    modifier: Modifier = Modifier
) {

    val cartoonFontFamily = FontFamily(Font(R.font.cartoon))

    Box(
        modifier = modifier.height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        // Отображаем текущее вводимое слово, только если оно не пустое
        if (currentWord.isNotEmpty()) {
            Text(
                text = currentWord.uppercase(),
                fontFamily = cartoonFontFamily,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
        // Эффект взлета букв накладывается поверх.
        // Он сам внутри себя следит за изменением flyTrigger и не запустится при 0.
        WordFlyUpEffect(
            word = triggerWord,
            triggerCount = flyTrigger,
            modifier = Modifier.matchParentSize()
        )
    }
}