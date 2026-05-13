import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdBannerPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.DarkGray.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Text("ADVERTISING BANNER", color = Color.Gray, fontSize = 12.sp)
    }
}