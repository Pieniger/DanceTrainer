package com.example.dancetrainer.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

class MoveAnnouncer(context: Context) {

    private val tts: TextToSpeech = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            configureVoice()
        }
    }

    private fun configureVoice() {
        val ukLocale = Locale.UK

        // Ensure language support
        val langResult = tts.setLanguage(ukLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA ||
            langResult == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            // Fallback to default locale
            tts.setLanguage(Locale.getDefault())
            return
        }

        // Try to find a British female voice
        val voices: Set<Voice> = tts.voices ?: return

        val preferred = voices
            .filter { it.locale == ukLocale }
            .sortedByDescending { voice ->
                // Heuristics: many engines include gender hints in the name
                when {
                    voice.name.contains("female", ignoreCase = true) -> 3
                    voice.name.contains("f", ignoreCase = true) -> 2
                    else -> 1
                }
            }
            .firstOrNull()

        if (preferred != null) {
            tts.voice = preferred
        }
    }

    /**
     * Speak the move name.
     * Tries to fit within ~3 beats, speeding up if necessary.
     */
    fun announce(moveName: String, bpm: Int) {
        val maxMs = 3_000L
        val estMs = (moveName.length * 60).coerceAtLeast(250)
        val rateNeeded = (estMs.toDouble() / maxMs.toDouble()).coerceAtLeast(1.0)
        val rate = rateNeeded.toFloat().coerceIn(0.8f, 2.5f)

        tts.setSpeechRate(rate)

        val params = Bundle()
        tts.speak(
            moveName,
            TextToSpeech.QUEUE_FLUSH,
            params,
            "move-${System.nanoTime()}"
        )
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
