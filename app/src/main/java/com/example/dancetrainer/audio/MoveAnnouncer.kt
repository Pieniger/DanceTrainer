package com.example.dancetrainer.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.util.Locale

class MoveAnnouncer(context: Context) {

    private val tts: TextToSpeech = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.getDefault())
        }
    }

    /**
     * Speak the move name. Tries to fit within ~3 beats, speeding up if necessary.
     */
    fun announce(moveName: String, bpm: Int) {
        val maxMs = (3_000L)  // hard cap ~3 seconds
        val estMs = (moveName.length * 60).coerceAtLeast(250) // crude estimate
        val rateNeeded = (estMs.toDouble() / maxMs.toDouble()).coerceAtLeast(1.0)
        val rate = rateNeeded.toFloat().coerceIn(0.8f, 2.5f)
        tts.setSpeechRate(rate)

        val params = Bundle()
        tts.speak(moveName, TextToSpeech.QUEUE_FLUSH, params, "move-" + System.nanoTime())
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
