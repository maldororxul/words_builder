import android.annotation.SuppressLint
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.compose.runtime.key

@SuppressLint("LocalContextResourcesRead")
@Composable
fun GameBackground(
    modifier: Modifier = Modifier,
    bgManager: BackgroundManager,
    selectedId: String = bgManager.selectedBackgroundId, // Принимает ID как ключ для отслеживания
) {
    val context = LocalContext.current
    val currentBg = bgManager.getCurrentBackground()

    // Оператор key заставляет Compose полностью уничтожить старый View и создать новый при смене ID
    key(selectedId) {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { frameLayout ->
                frameLayout.removeAllViews()

                if (currentBg.type == "video") {
                    val videoView = VideoView(context).apply {
                        val resId = context.resources.getIdentifier(currentBg.resourceName, "raw", context.packageName)
                        if (resId != 0) {
                            setVideoURI("android.resource://${context.packageName}/$resId".toUri())
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                mp.setVolume(0f, 0f)
                                start()
                            }
                        }
                    }
                    frameLayout.addView(videoView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                } else {
                    val imageView = ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        val resId = context.resources.getIdentifier(currentBg.resourceName, "drawable", context.packageName)
                        if (resId != 0) {
                            setImageResource(resId)
                        }
                    }
                    frameLayout.addView(imageView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                }
            }
        )
    }
}
