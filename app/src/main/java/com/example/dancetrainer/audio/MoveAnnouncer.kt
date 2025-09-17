package com.example.dancetrainer.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class MoveAnnouncer(context: Context) {
    private val tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    fun announce(moveName: String, bpm: Int) {
        val beatsPerSec = bpm / 60.0
        val maxDurationMs = (3000 / beatsPerSec).toLong().coerceAtMost(3000)
        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, android.media.AudioManager.STREAM_MUSIC)
        }
        tts.speak(moveName, TextToSpeech.QUEUE_FLUSH, null, "moveId")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
