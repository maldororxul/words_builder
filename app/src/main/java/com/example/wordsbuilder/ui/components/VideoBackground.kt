import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.wordsbuilder.R

@OptIn(UnstableApi::class)
@Composable
fun VideoBackground(videoResId: Int) {
    val context = LocalContext.current
    val videoUri = "android.resource://${context.packageName}/$videoResId"

    // Сильный ключ для полного пересоздания при каждом входе в уровень
    var playerKey by rememberSaveable { mutableIntStateOf(0) }

    val exoPlayer = remember(playerKey) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        exoPlayer.play()
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            // Инфлейтим XML с texture_view
            val view = android.view.LayoutInflater.from(ctx)
                .inflate(R.layout.video_background, null) as androidx.media3.ui.PlayerView

            view.player = exoPlayer
            view
        },
        update = { playerView ->
            if (playerView.player != exoPlayer) {
                playerView.player = exoPlayer
            }
            // Принудительное перерисовывание
            playerView.requestLayout()
            playerView.invalidate()
        }
    )

    // Кнопка для отладки (можно убрать потом)
    // Button(onClick = { playerKey++ }) { Text("Restart Video") }
}