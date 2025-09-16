package com.example.dancetrainer.model
import kotlinx.serialization.Serializable
@Serializable
data class StyleSettings(
  var bpm: Int = 100,
  var accentEvery: Int = 4,
  var countInBeats: Int = 4,
  var tickVolume: Float = 0.7f,
  var tickSound: String = "builtin:click.wav",
  var accentSound: String = "builtin:bell.wav",
  var ttsEnabled: Boolean = true,
  var ttsVoice: String = "system:default"
)
