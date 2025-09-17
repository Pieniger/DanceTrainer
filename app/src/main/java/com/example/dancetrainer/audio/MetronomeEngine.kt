package com.example.dancetrainer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import java.io.IOException

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

    private var clickId: Int = 0
    private var bellId: Int = 0

    init {
        // Try to load assets, but don't crash if missing
        try {
            clickId = context.assets.openFd("sounds/click.wav").use { afd -> soundPool.load(afd, 1) }
        } catch (_: IOException) { clickId = 0 }
        try {
            bellId = context.assets.openFd("sounds/bell.wav").use { afd -> soundPool.load(afd, 1) }
        } catch (_: IOException) { bellId = 0 }
    }

    var bpm: Int = 100
    var accentEvery: Int = 4
    var countInBeats: Int = 0

    var onBeat: ((Long) -> Unit)? = null

    private var running = false
    private var beatIndex = 0L

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!running) return
            val isCountIn = beatIndex < countInBeats
            val isAccent = accentEvery > 0 && ((beatIndex + 1) % accentEvery == 0L)
            val sample = when {
                isCountIn -> clickId
                isAccent && bellId != 0 -> bellId
                else -> clickId
            }
            if (sample != 0) soundPool.play(sample, 1f, 1f, 1, 0, 1f)
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
