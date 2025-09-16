package com.example.dancetrainer.audio
import android.media.SoundPool
import android.os.SystemClock
import kotlinx.coroutines.*
class MetronomeEngine(private val sp: SoundPool, private val softId: Int, private val accentId: Int){
  private var scope = CoroutineScope(Dispatchers.Default); private var job: Job? = null
  var bpm: Double = 100.0; var accentEvery: Int = 4; var countInBeats: Int = 0; var onBeat: ((Long)->Unit)? = null
  fun start(){ if (job!=null) return; val beatNs = (60e9/bpm).toLong(); val startNs = SystemClock.elapsedRealtimeNanos()+50_000_000L
    job = scope.launch{ var i=0L; while(isActive){ val target = startNs + i*beatNs; var now = SystemClock.elapsedRealtimeNanos(); var wait = target-now
      if (wait>0){ if (wait>2_000_000) delay((wait-1_000_000)/1_000_000); while(SystemClock.elapsedRealtimeNanos()<target){} }
      val isAccent = accentEvery>0 && ((i+1)%accentEvery==0L); val sample = if (i<countInBeats) softId else if (isAccent) accentId else softId
      sp.play(sample,1f,1f,1,0,1f); withContext(Dispatchers.Main){ onBeat?.invoke(i) }; i++ } } }
  fun stop(){ job?.cancel(); job=null }
}
