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
import com.example.dancetrainer.data.Storage
import com.example.dancetrainer.data.Prefs
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanceScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    val ttsEnabled = remember { Prefs.isVoiceEnabled(ctx) }

    // Initialize TTS if enabled
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

    // Load data for selected style
    val style = Prefs.getStyle(ctx)
    var moves by remember { mutableStateOf(Storage.loadMoves(ctx, style)) }
    var connections by remember { mutableStateOf(Storage.loadConnections(ctx, style)) }

    // Current state
    var move1 by remember { mutableStateOf<Move?>(null) }
    var move2 by remember { mutableStateOf<Move?>(null) }
    var connectionNote by remember { mutableStateOf<String>("") }

    var errorPopup by remember { mutableStateOf<String?>(null) }

    fun speak(text: String) {
        if (ttsEnabled) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dance")
    }

    fun pickRandomMove(except: Move? = null): Move? {
        val list = moves.filter { it.id != except?.id }
        return if (list.isEmpty()) null else list.random()
    }

    fun findNextMove(from: Move): Move? {
        // Get all connections where from → another with positive smoothness
        val candidates = connections
            .filter { it.fromId == from.id && it.smoothness > 0 }
            .mapNotNull { c -> moves.find { it.id == c.toId } }

        return if (candidates.isEmpty()) null else candidates.random()
    }

    fun updateConnectionNote() {
        connectionNote = if (move1 != null && move2 != null) {
            val c = connections.find { it.fromId == move1!!.id && it.toId == move2!!.id }
            c?.notes ?: ""
        } else ""
    }

    fun startSequence() {
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
        if (move2 == null) return

        move1 = move2
        move2 = findNextMove(move1!!)

        if (move2 == null) {
            errorPopup = "Dead end! '${move1!!.name}' has no valid follow-up.\nReturning to menu."
            return
        }

        updateConnectionNote()
        speak(move1!!.name)
    }

    fun rerollMove2() {
        if (move1 == null) return

        move2 = findNextMove(move1!!)
        if (move2 == null) {
            errorPopup = "No valid follow-up moves for '${move1!!.name}'."
            return
        }

        updateConnectionNote()
    }

    // Start automatically once
    LaunchedEffect(style) {
        startSequence()
    }

    // UI
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

            if (move1 == null || move2 == null) {
                Text("Loading...")
                return@Column
            }

            Text("Current Move", style = MaterialTheme.typography.titleMedium)
            Text(move1!!.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)

            if (connectionNote.isNotBlank()) {
                Text("Note: $connectionNote", style = MaterialTheme.typography.bodyMedium)
            }

            Text("Next Move", style = MaterialTheme.typography.titleMedium)
            Text(move2!!.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)

            Spacer(Modifier.height(40.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Button(onClick = { nextMove() }) {
                    Text("Next Move")
                }

                Button(onClick = { rerollMove2() }) {
                    Text("Reroll")
                }
            }
        }
    }

    // Dead-end popup
    if (errorPopup != null) {
        AlertDialog(
            onDismissRequest = {
                errorPopup = null
                onBack()
            },
            title = { Text("No Valid Move") },
            text = { Text(errorPopup!!) },
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
