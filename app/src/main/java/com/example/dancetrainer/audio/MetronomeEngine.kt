package com.example.dancetrainer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper

class MetronomeEngine(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private var bpm: Int = 120
    private var isRunning = false
    private var beatSoundId: Int = 0
    private var soundPool: SoundPool

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(2).setAudioAttributes(attrs).build()

        // load default click sound (should be in assets)
        // for now, we simulate by using system tone id 24
        beatSoundId = soundPool.load(context, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, 1)
    }

    fun setBpm(value: Int) { bpm = value }

    private val tick = object : Runnable {
        override fun run() {
            if (isRunning) {
                soundPool.play(beatSoundId, 1f, 1f, 1, 0, 1f)
                val delay = (60000 / bpm).toLong()
                handler.postDelayed(this, delay)
            }
        }
    }

    fun start() {
        if (!isRunning) {
            isRunning = true
            handler.post(tick)
        }
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacks(tick)
    }
}
