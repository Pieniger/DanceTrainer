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
import com.example.dancetrainer.data.Storage
import com.example.dancetrainer.logic.DanceConductor

@Composable
fun DanceScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    // Load moves once
    var moves by remember { mutableStateOf(Storage.loadMoves(ctx)) }

    // Settings
    val metronomeEnabled = remember { mutableStateOf(Prefs.isMetronomeEnabled(ctx)) }
    val voiceEnabled = remember { mutableStateOf(Prefs.isVoiceEnabled(ctx)) }

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
    var info by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            metronome.stop()
            announcer.shutdown()
        }
    }

    fun announceIfEnabled(name: String?) {
        if (voiceEnabled.value && !name.isNullOrBlank()) announcer.announce(name, bpm)
    }

    fun startMetronomeIfEnabled() {
        if (metronomeEnabled.value) {
            metronome.bpm = bpm
            metronome.start()
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dance Mode", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        // Empty-state message
        if (moves.isEmpty()) {
            Text("No moves yet. Add some in Manage Moves.")
            Spacer(Modifier.height(12.dp))
        }

        // BPM + Play/Pause
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = bpm.toString(),
                onValueChange = { v -> bpm = v.filter { it.isDigit() }.toIntOrNull()?.coerceIn(40, 240) ?: bpm },
                label = { Text("BPM") },
                modifier = Modifier.weight(1f)
            )
            if (!playing) {
                Button(
                    enabled = moves.isNotEmpty(),
                    onClick = { playing = true; startMetronomeIfEnabled() }
                ) { Text("Start") }
            } else {
                Button(onClick = { playing = false; metronome.stop() }) { Text("Stop") }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Mode buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                enabled = moves.isNotEmpty(),
                onClick = {
                    mode = "single"
                    conductor.reset()
                    current = conductor.nextSingleMoveFrom(moves)
                    announceIfEnabled(current?.name)
                    info = if (current == null) "No moves available." else null
                }
            ) { Text("One Move") }

            Button(
                enabled = moves.isNotEmpty(),
                onClick = {
                    mode = "random"
                    conductor.generateRandomSequenceFrom(moves)
                    sequence = conductor.currentSequence
                    announceIfEnabled(sequence.firstOrNull()?.name)
                    info = if (sequence.isEmpty()) "No moves available." else null
                }
            ) { Text("Random Seq") }

            Button(
                enabled = moves.isNotEmpty(),
                onClick = {
                    mode = "stored"
                    conductor.pickStoredSequence()
                    sequence = conductor.currentSequence
                    announceIfEnabled(sequence.firstOrNull()?.name)
                    info = if (sequence.isEmpty()) "No stored sequences yet." else null
                }
            ) { Text("Stored Seq") }
        }

        Spacer(Modifier.height(16.dp))

        when (mode) {
            "single" -> {
                val name = current?.name ?: "—"
                Text("Next Move: $name")
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    enabled = moves.isNotEmpty(),
                    onClick = {
                        current = conductor.nextSingleMoveFrom(moves)
                        announceIfEnabled(current?.name)
                    }
                ) { Text("Next") }
            }
            "random" -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Random Sequence:")
                    sequence.forEach { Text(it.name) }
                    OutlinedButton(
                        enabled = moves.isNotEmpty(),
                        onClick = {
                            conductor.generateRandomSequenceFrom(moves)
                            sequence = conductor.currentSequence
                            announceIfEnabled(sequence.firstOrNull()?.name)
                        }
                    ) { Text("Reload Sequence") }
                }
            }
            "stored" -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Stored Sequence:")
                    sequence.forEach { Text(it.name) }
                    OutlinedButton(
                        onClick = {
                            conductor.pickStoredSequence()
                            sequence = conductor.currentSequence
                            announceIfEnabled(sequence.firstOrNull()?.name)
                        }
                    ) { Text("Reload Stored Sequence") }
                }
            }
        }

        info?.let { msg ->
            Spacer(Modifier.height(8.dp))
            AssistChip(onClick = {}, label = { Text(msg) })
        }

        Spacer(Modifier.weight(1f))
        Button(onClick = { metronome.stop(); onBack() }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
