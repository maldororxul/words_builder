package com.example.wordsbuilder

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.MediaPlayer
import androidx.core.content.edit


object SoundManager {
    // Плеер оставляем только для длинной фоновой музыки
    private var musicPlayer: MediaPlayer? = null

    // Для коротких эффектов используем SoundPool
    private var soundPool: SoundPool? = null

    // Карта для хранения загруженных ID звуков
    private val soundMap = HashMap<Int, Int>()

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

        // Инициализируем SoundPool под стандарты Android 5.0+
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // До 5 одновременных звуков
            .setAudioAttributes(audioAttributes)
            .build()

        // Сразу предзагружаем частые звуки в память, чтобы они не лагали при первом клике
        preloadSound(context, R.raw.click)
        preloadSound(context, R.raw.success)
    }

    // Метод для предварительной загрузки звука в оперативную память
    private fun preloadSound(context: Context, resId: Int) {
        if (!soundMap.containsKey(resId)) {
            soundPool?.load(context, resId, 1)?.let { soundId ->
                soundMap[resId] = soundId
            }
        }
    }

    // Мгновенное воспроизведение без блокировки UI-потока Compose
    fun playSound(context: Context, resId: Int) {
        val pool = soundPool ?: return

        // Если звук уже загружен в память — играем мгновенно
        if (soundMap.containsKey(resId)) {
            val soundId = soundMap[resId] ?: return
            pool.play(soundId, soundVolume, soundVolume, 1, 0, 1f)
        } else {
            // Если звук новый — загружаем и вешаем слушатель на готовность
            pool.load(context, resId, 1).let { soundId ->
                soundMap[resId] = soundId
                pool.setOnLoadCompleteListener { _, sampleId, _ ->
                    if (sampleId == soundId) {
                        pool.play(soundId, soundVolume, soundVolume, 1, 0, 1f)
                    }
                }
            }
        }
    }

    // Динамическое обновление громкости звуков из ползунка настроек
    fun updateSoundVolume(context: Context, volume: Float) {
        soundVolume = volume
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putFloat(KEY_SOUND_VOL, volume) }
    }

    // --- Логика фоновой музыки (MediaPlayer) остается прежней ---
    fun switchMusic(context: Context, resId: Int) {
        if (currentMusicResId == resId && musicPlayer?.isPlaying == true) return
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
        switchMusic(context, R.raw.menu_music)
    }

    fun pauseMusic() { musicPlayer?.pause() }
    fun resumeMusic() { musicPlayer?.start() }

    fun updateMusicVolume(context: Context, volume: Float) {
        musicVolume = volume
        musicPlayer?.setVolume(volume, volume)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putFloat(KEY_MUSIC_VOL, volume) }
    }

    fun stopMusic() {
        musicPlayer?.stop()
        musicPlayer?.release()
        musicPlayer = null
    }
}