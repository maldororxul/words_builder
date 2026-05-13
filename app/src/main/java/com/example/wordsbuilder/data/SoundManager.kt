import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.wordsbuilder.R

object SoundManager {
    private var soundPool: SoundPool? = null
    private var sounds = mutableMapOf<String, Int>()
    private var mediaPlayer: MediaPlayer? = null

    fun init(context: Context) {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder().setMaxStreams(5).setAudioAttributes(attrs).build()

        // Загружаем эффекты
        sounds["click"] = soundPool?.load(context, R.raw.click, 1) ?: 0
        sounds["success"] = soundPool?.load(context, R.raw.success, 1) ?: 0
        sounds["error"] = soundPool?.load(context, R.raw.error, 1) ?: 0
        sounds["victory"] = soundPool?.load(context, R.raw.victory, 1) ?: 0
    }

    fun playSound(name: String) {
        sounds[name]?.let { soundPool?.play(it, 1f, 1f, 0, 0, 1f) }
    }

    fun startMusic(context: Context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.bg_music).apply {
                isLooping = true
                setVolume(0.3f, 0.3f) // Немного тише, чтобы не мешать эффектам
                start()
            }
        } else if (!mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
        }
    }

    fun pauseMusic() {
        mediaPlayer?.pause()
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}