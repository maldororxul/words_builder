import android.content.Context
import android.media.MediaPlayer
import com.example.wordsbuilder.R
import androidx.core.content.edit

object SoundManager {
    private var musicPlayer: MediaPlayer? = null
    private var soundPlayer: MediaPlayer? = null

    private const val PREFS_NAME = "game_prefs"
    private const val KEY_MUSIC_VOL = "music_volume"
    private const val KEY_SOUND_VOL = "sound_volume"

    var musicVolume: Float = 0.8f
    var soundVolume: Float = 0.8f

    private var currentMusicResId: Int? = null

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        musicVolume = prefs.getFloat(KEY_MUSIC_VOL, 0.8f)
        soundVolume = prefs.getFloat(KEY_SOUND_VOL, 0.8f)
    }

    fun switchMusic(context: Context, resId: Int) {
        // Проверка: если этот трек уже играет, игнорируем вызов
        if (currentMusicResId == resId && musicPlayer?.isPlaying == true) {
            return
        }

        musicPlayer?.stop()
        musicPlayer?.release()

        currentMusicResId = resId
        musicPlayer = MediaPlayer.create(context, resId).apply {
            isLooping = true // Бесконечный повтор для фоновой музыки
            setVolume(musicVolume, musicVolume)
            start()
        }
    }

    fun startMusic(context: Context) {
        switchMusic(context, R.raw.menu_music)
    }

    fun pauseMusic() {
        musicPlayer?.pause()
    }

    fun resumeMusic() {
        musicPlayer?.start()
    }

    fun updateMusicVolume(context: Context, volume: Float) {
        musicVolume = volume
        musicPlayer?.setVolume(volume, volume)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putFloat(KEY_MUSIC_VOL, volume) }
    }

    fun updateSoundVolume(context: Context, volume: Float) {
        soundVolume = volume
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putFloat(KEY_SOUND_VOL, volume) }
    }

    fun playSound(context: Context, resId: Int) {
        soundPlayer?.release()
        soundPlayer = MediaPlayer.create(context, resId).apply {
            setVolume(soundVolume, soundVolume)
            start()
            setOnCompletionListener { release() }
        }
    }

    // Метод для одиночных немузыкальных событий (если нужен)
    fun playMusic(context: Context, resId: Int) {
        switchMusic(context, resId)
    }

    fun playMusicByName(context: Context, musicResName: String) {
        val musicResId = context.resources.getIdentifier(musicResName, "raw", context.packageName)
        if (musicResId != 0) {
            switchMusic(context, musicResId)
        } else {
            switchMusic(context, R.raw.menu_music)
        }
    }

    fun stopMusic() {
        musicPlayer?.stop()
        musicPlayer?.release()
        musicPlayer = null
        currentMusicResId = null
    }
}