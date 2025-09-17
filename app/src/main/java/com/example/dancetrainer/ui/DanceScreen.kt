package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.audio.MetronomeEngine
import com.example.dancetrainer.audio.MoveAnnouncer
import com.example.dancetrainer.data.Prefs
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.logic.DanceConductor

@Composable
fun DanceScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    // Settings
    val metronomeEnabled = remember { mutableStateOf(Prefs.isMetronomeEnabled(ctx)) }
    val voiceEnabled = remember { mutableStateOf(Prefs.isVoiceEnabled(ctx)) }
    val metronomeSound = remember { mutableStateOf(Prefs.getMetronomeSound(ctx)) } // "click" | "bell"

    // Engines
    val metronome = remember { MetronomeEngine(ctx) }
    val announcer = remember { MoveAnnouncer(ctx) }

    // State
    var bpm by remember { mutableStateOf(100) }
    var mode by remember { mutableStateOf("single") }
    val conductor = remember { DanceConductor() }
    var sequence by remember { mutableStateOf<List<Move>>(emptyList()) }
    var current by remember { mutableStateOf<Move?>(null) }
    var playing by remember { mutableStateOf(false) }

    // Cleanup TTS on leave
    DisposableEffect(Unit) {
        onDispose {
            metronome.stop()
            announcer.shutdown()
        }
    }

    fun announceIfEnabled(name: String) {
        if (voiceEnabled.value) announcer.announce(name, bpm)
    }

    fun startMetronomeIfEnabled() {
        if (metronomeEnabled.value) {
            metronome.bpm = bpm
            // (We load both sounds; selection affects the "accent" feel via preview elsewhere. For simplicity we just tick.)
            metronome.start()
        }
    }

    fun stopMetronome() = metronome.stop()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dance Mode", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // BPM + Play/Pause
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = bpm.toString(),
                onValueChange = { v -> bpm = v.filter { it.isDigit() }.toIntOrNull()?.coerceIn(40, 240) ?: bpm },
                label = { Text("BPM") },
                modifier = Modifier.weight(1f)
            )
            if (!playing) {
                Button(onClick = {
                    playing = true
                    startMetronomeIfEnabled()
                }) { Text("Start") }
            } else {
                Button(onClick = {
                    playing = false
                    stopMetronome()
                }) { Text("Stop") }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mode buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = {
                mode = "single"
                conductor.reset()
                current = conductor.nextSingleMove()
                current?.name?.let { announceIfEnabled(it) }
            }) { Text("One Move") }

            Button(onClick = {
                mode = "random"
                conductor.generateRandomSequence()
                sequence = conductor.currentSequence
                if (sequence.isNotEmpty()) announceIfEnabled(sequence.first().name)
            }) { Text("Random Seq") }

            Button(onClick = {
                mode = "stored"
                conductor.pickStoredSequence()
                sequence = conductor.currentSequence
                if (sequence.isNotEmpty()) announceIfEnabled(sequence.first().name)
            }) { Text("Stored Seq") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (mode) {
            "single" -> {
                val name = current?.name ?: "None"
                Text("Next Move: $name")
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    current = conductor.nextSingleMove()
                    current?.name?.let { announceIfEnabled(it) }
                }) { Text("Next") }
            }
            "random" -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Random Sequence:")
                    sequence.forEach { Text(it.name) }
                    OutlinedButton(onClick = {
                        conductor.generateRandomSequence()
                        sequence = conductor.currentSequence
                        if (sequence.isNotEmpty()) announceIfEnabled(sequence.first().name)
                    }) { Text("Reload Sequence") }
                }
            }
            "stored" -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Stored Sequence:")
                    sequence.forEach { Text(it.name) }
                    OutlinedButton(onClick = {
                        conductor.pickStoredSequence()
                        sequence = conductor.currentSequence
                        if (sequence.isNotEmpty()) announceIfEnabled(sequence.first().name)
                    }) { Text("Reload Stored Sequence") }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = {
            stopMetronome()
            onBack()
        }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
