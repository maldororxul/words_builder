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

    // Храним ID текущего играющего трека, чтобы не перезапускать его с начала, если он уже играет
    private var currentMusicResId: Int? = null

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        musicVolume = prefs.getFloat(KEY_MUSIC_VOL, 0.8f)
        soundVolume = prefs.getFloat(KEY_SOUND_VOL, 0.8f)
    }

    // Универсальный метод переключения музыки
    fun switchMusic(context: Context, resId: Int) {
        // Если этот трек уже играет прямо сейчас, ничего не делаем (избегаем заикания звука)
        if (currentMusicResId == resId && musicPlayer?.isPlaying == true) {
            return
        }

        // Освобождаем старый плеер
        musicPlayer?.stop()
        musicPlayer?.release()

        currentMusicResId = resId
        musicPlayer = MediaPlayer.create(context, resId).apply {
            isLooping = true
            setVolume(musicVolume, musicVolume)
            start()
        }
    }

    fun startMusic(context: Context) {
        // По умолчанию при холодном старте включаем музыку меню
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

    fun playMusic(context: Context, resId: Int) {
        musicPlayer?.release()
        musicPlayer = MediaPlayer.create(context, resId).apply {
            setVolume(musicVolume, musicVolume)
            start()
            setOnCompletionListener { release() }
        }
    }

    fun playMusicByName(context: Context, musicResName: String) {
        val musicResId = context.resources.getIdentifier(musicResName, "raw", context.packageName)
        if (musicResId != 0) {
            // Вызываем ваш существующий метод воспроизведения музыки по ID
            playMusic(context, musicResId)
        } else {
            // Фоллбэк, если трек не найден — играем дефолтную музыку меню
            val defaultId = context.resources.getIdentifier("menu_music", "raw", context.packageName)
            if (defaultId != 0) playMusic(context, defaultId)
        }
    }

    fun stopMusic() {
        musicPlayer?.stop()
        musicPlayer?.release()
        musicPlayer = null
    }

}