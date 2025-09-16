package com.example.dancetrainer.audio
import android.speech.tts.TextToSpeech
import android.os.Handler
import android.os.Looper
class MoveAnnouncer(private val tts: TextToSpeech, private val bpmProvider:()->Double){
  private val handler = Handler(Looper.getMainLooper()); private var msPerChar = 60.0
  fun speakMove(name:String){
    val bpm = bpmProvider().coerceAtLeast(40.0)
    val maxMs = (3000*(100.0/bpm)).toLong()
    val predicted = (name.length*msPerChar).toLong().coerceAtLeast(250L)
    val rateNeeded = (predicted.toDouble()/maxMs).coerceAtLeast(1.0).toFloat()
    tts.setSpeechRate(rateNeeded.coerceIn(0.8f,2.5f))
    tts.speak(name, TextToSpeech.QUEUE_FLUSH, null, "move-"+System.nanoTime())
    handler.postDelayed({ tts.stop() }, (maxMs-40L).coerceAtLeast(100L))
  }
}
