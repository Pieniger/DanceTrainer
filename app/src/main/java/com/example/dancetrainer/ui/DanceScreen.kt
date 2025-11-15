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
import androidx.compose.ui.unit.sp
import com.example.dancetrainer.MainActivity
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

    fun speak(text: String) {
        if (ttsEnabled) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts-${System.nanoTime()}")
        }
    }

    // Load data for selected style
    var moves by remember { mutableStateOf(Storage.loadMoves(ctx)) }
    var connections by remember { mutableStateOf(Storage.loadConnections(ctx)) }

    var move1 by remember { mutableStateOf<Move?>(null) }
    var move2 by remember { mutableStateOf<Move?>(null) }
    var connectionNote by remember { mutableStateOf("") }

    var errorPopup by remember { mutableStateOf<String?>(null) }

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
        connectionNote =
            if (move1 != null && move2 != null)
                connections.find { it.fromId == move1!!.id && it.toId == move2!!.id }?.notes.orEmpty()
            else ""
    }

    fun startSequence() {
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

        // Speak NEXT move (move2)
        speak(move2!!.name)
    }

    fun nextMove() {
        val next = move2 ?: return

        move1 = next
        move2 = findNextMove(next)

        if (move2 == null) {
            errorPopup = "Dead end! '${next.name}' has no valid follow-up.\nReturning to menu."
        } else {
            updateConnectionNote()
            // Speak new NEXT move
            speak(move2!!.name)
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
            // Speak new move2 on reroll
            speak(newNext.name)
        }
    }

    // Register Bluetooth / hardware key handler while this screen is visible
    val nextKeyCode = remember { Prefs.getNextMoveKeyCode(ctx) }
    val rerollKeyCode = remember { Prefs.getRerollKeyCode(ctx) }

    DisposableEffect(nextKeyCode, rerollKeyCode) {
        MainActivity.danceKeyHandler = { keyCode ->
            when (keyCode) {
                nextKeyCode -> {
                    nextMove()
                    true
                }
                rerollKeyCode -> {
                    rerollMove2()
                    true
                }
                else -> false
            }
        }
        onDispose {
            MainActivity.danceKeyHandler = null
        }
    }

    // Start initial pair
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

            // Current Move
            Text("Current move", style = MaterialTheme.typography.titleMedium)
            Text(
                m1.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )

            if (connectionNote.isNotBlank()) {
                Text("Note: $connectionNote", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(30.dp))

            // Next Move (bigger)
            Text("Next move", style = MaterialTheme.typography.titleMedium)
            Text(
                m2.name,
                fontWeight = FontWeight.Bold,
                fontSize = (MaterialTheme.typography.headlineMedium.fontSize.value * 1.5f).sp
            )

            Spacer(Modifier.height(40.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Button(onClick = { nextMove() }) { Text("Next move") }
                Button(onClick = { rerollMove2() }) { Text("Reroll") }
            }
        }
    }

    errorPopup?.let { msg ->
        AlertDialog(
            onDismissRequest = { onBack() },
            title = { Text("No valid move") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = onBack) { Text("OK") }
            }
        )
    }
}
