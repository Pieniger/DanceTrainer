package com.example.dancetrainer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper

class MetronomeEngine(private val context: Context) {

    private val handler = Handler(Looper.getMainLooper())
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()

    // Load small WAVs from assets/sounds/
    private val clickId: Int = context.assets.openFd("sounds/click.wav").use { afd -> soundPool.load(afd, 1) }
    private val bellId: Int  = context.assets.openFd("sounds/bell.wav").use { afd -> soundPool.load(afd, 1) }

    var bpm: Int = 100
    var accentEvery: Int = 4
    var countInBeats: Int = 0

    /** Called on main thread after each beat index (0-based). */
    var onBeat: ((Long) -> Unit)? = null

    private var running = false
    private var beatIndex = 0L

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!running) return
            val isCountIn = beatIndex < countInBeats
            val isAccent = accentEvery > 0 && ((beatIndex + 1) % accentEvery == 0L)
            val sample = if (isCountIn) clickId else if (isAccent) bellId else clickId
            soundPool.play(sample, 1f, 1f, 1, 0, 1f)

            onBeat?.invoke(beatIndex)
            beatIndex++

            val delayMs = (60_000.0 / bpm).toLong().coerceAtLeast(50L)
            handler.postDelayed(this, delayMs)
        }
    }

    fun start() {
        if (running) return
        running = true
        beatIndex = 0
        handler.post(tickRunnable)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(tickRunnable)
    }
}
