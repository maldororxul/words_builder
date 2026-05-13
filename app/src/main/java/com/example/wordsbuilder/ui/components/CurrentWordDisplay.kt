import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CurrentWordDisplay(currentWord: String) {
    Box(
        modifier = Modifier.height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = currentWord,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}