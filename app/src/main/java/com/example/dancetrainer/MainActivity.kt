package com.example.dancetrainer
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.audio.MetronomeEngine
import com.example.dancetrainer.audio.MoveAnnouncer
import com.example.dancetrainer.model.StyleSettings
import java.util.Locale

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { AppRoot() }
  }
}
@Composable fun AppRoot() {
  var screen by remember { mutableStateOf("settings") }
  MaterialTheme { Surface {
    when(screen){
      "settings" -> SettingsScreen { screen = "settings" }
    }
  } }
}
@Composable fun SettingsScreen(onBack:()->Unit) {
  var settings by remember { mutableStateOf(StyleSettings()) }
  Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("Settings", style = MaterialTheme.typography.headlineSmall)
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("BPM"); Spacer(Modifier.width(12.dp))
      OutlinedTextField(value = settings.bpm.toString(), onValueChange = { it.toIntOrNull()?.let{v-> settings = settings.copy(bpm=v) } }, modifier=Modifier.width(120.dp))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("Accent every"); Spacer(Modifier.width(12.dp))
      OutlinedTextField(value = settings.accentEvery.toString(), onValueChange = { it.toIntOrNull()?.let{v-> settings = settings.copy(accentEvery=v) } }, modifier=Modifier.width(120.dp))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("Count-in beats"); Spacer(Modifier.width(12.dp))
      OutlinedTextField(value = settings.countInBeats.toString(), onValueChange = { it.toIntOrNull()?.let{v-> settings = settings.copy(countInBeats=v) } }, modifier=Modifier.width(120.dp))
    }
    Divider()
    PreviewWithCurrentBPM(settings)
  }
}
@Composable fun PreviewWithCurrentBPM(settings: StyleSettings) {
  val ctx = androidx.compose.ui.platform.LocalContext.current
  var tts by remember { mutableStateOf<TextToSpeech?>(null) }
  LaunchedEffect(Unit){ tts = TextToSpeech(ctx){ if (it==TextToSpeech.SUCCESS) tts?.language = Locale.getDefault() } }
  var playing by remember { mutableStateOf(false) }
  val sp = remember {
    SoundPool.Builder().setMaxStreams(2).setAudioAttributes(
      AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
    ).build()
  }
  val afdSoft = remember { ctx.assets.openFd("sounds/click.wav") }
  val afdAccent = remember { ctx.assets.openFd("sounds/bell.wav") }
  val softId = remember { sp.load(afdSoft,1) }; val accentId = remember { sp.load(afdAccent,1) }
  val engine = remember { MetronomeEngine(sp, softId, accentId) }
  val announcer = remember { MoveAnnouncer(tts ?: TextToSpeech(ctx){},{ settings.bpm.toDouble() }) }
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Button(enabled=!playing, onClick={
      playing = true
      engine.bpm = settings.bpm.toDouble(); engine.accentEvery = settings.accentEvery; engine.countInBeats = settings.countInBeats
      var beats = 0
      engine.onBeat = {
        beats++
        if (beats == settings.countInBeats + 1) announcer.speakMove("Spin, Slide, Jump")
        if (beats >= settings.countInBeats + 8){ engine.stop(); playing = false }
      }
      engine.start()
    }) { Text("▶ Preview with current BPM") }
  }
}
