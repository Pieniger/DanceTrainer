package com.example.dancetrainer.ui

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Prefs
import com.example.dancetrainer.data.Storage
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanceScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    val ttsEnabled = remember { Prefs.isVoiceEnabled(ctx) }

    // Init / dispose TTS
    LaunchedEffect(ttsEnabled) {
        if (ttsEnabled) {
            tts = TextToSpeech(ctx) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                }
            }
        } else {
            tts?.shutdown()
            tts = null
        }
    }

    // Load data for current style (Storage already looks at Prefs.getStyle)
    var moves by remember { mutableStateOf(Storage.loadMoves(ctx)) }
    var connections by remember { mutableStateOf(Storage.loadConnections(ctx)) }

    // Current state
    var move1 by remember { mutableStateOf<Move?>(null) }
    var move2 by remember { mutableStateOf<Move?>(null) }
    var connectionNote by remember { mutableStateOf("") }

    var errorPopup by remember { mutableStateOf<String?>(null) }

    fun speak(text: String) {
        if (ttsEnabled) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dance-${System.nanoTime()}")
        }
    }

    fun pickRandomMove(except: Move? = null): Move? {
        val list = moves.filter { it.id != except?.id }
        return if (list.isEmpty()) null else list.random()
    }

    fun findNextMove(from: Move): Move? {
        val candidates = connections
            .filter { it.fromId == from.id && it.smoothness > 0 }
            .mapNotNull { c -> moves.find { it.id == c.toId } }

        return if (candidates.isEmpty()) null else candidates.random()
    }

    fun updateConnectionNote() {
        connectionNote = if (move1 != null && move2 != null) {
            connections
                .find { it.fromId == move1!!.id && it.toId == move2!!.id }
                ?.notes.orEmpty()
        } else ""
    }

    fun startSequence() {
        // Reload from disk in case style/files changed
        moves = Storage.loadMoves(ctx)
        connections = Storage.loadConnections(ctx)

        val first = pickRandomMove()
        if (first == null) {
            errorPopup = "No moves exist in this style!"
            return
        }

        move1 = first
        move2 = findNextMove(first)

        if (move2 == null) {
            errorPopup = "Move '${first.name}' has no compatible follow-ups."
            return
        }

        updateConnectionNote()
        speak(first.name)
    }

    fun nextMove() {
        val currentNext = move2 ?: return

        move1 = currentNext
        move2 = findNextMove(currentNext)

        if (move2 == null) {
            errorPopup = "Dead end! '${currentNext.name}' has no valid follow-up.\nReturning to menu."
        } else {
            updateConnectionNote()
            speak(currentNext.name)
        }
    }

    fun rerollMove2() {
        val base = move1 ?: return

        val newNext = findNextMove(base)
        if (newNext == null) {
            errorPopup = "No valid follow-up moves for '${base.name}'."
        } else {
            move2 = newNext
            updateConnectionNote()
        }
    }

    // Start automatically once per composition
    LaunchedEffect(Unit) {
        startSequence()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dance") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val m1 = move1
            val m2 = move2

            if (m1 == null || m2 == null) {
                Text("Loading…")
                return@Column
            }

            Text("Current move", style = MaterialTheme.typography.titleMedium)
            Text(
                m1.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )

            if (connectionNote.isNotBlank()) {
                Text("Note: $connectionNote", style = MaterialTheme.typography.bodyMedium)
            }

            Text("Next move", style = MaterialTheme.typography.titleMedium)
            Text(
                m2.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(40.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Button(onClick = { nextMove() }) {
                    Text("Next move")
                }
                Button(onClick = { rerollMove2() }) {
                    Text("Reroll")
                }
            }
        }
    }

    // Dead-end / error dialog
    errorPopup?.let { msg ->
        AlertDialog(
            onDismissRequest = {
                errorPopup = null
                onBack()
            },
            title = { Text("No valid move") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(
                    onClick = {
                        errorPopup = null
                        onBack()
                    }
                ) { Text("OK") }
            }
        )
    }
}
