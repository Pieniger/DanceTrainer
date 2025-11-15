package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dancetrainer.audio.MoveAnnouncer
import com.example.dancetrainer.data.Connection
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Storage

@Composable
fun DanceScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    // Load data once for this screen
    val movesState = remember { mutableStateOf(Storage.loadMoves(ctx)) }
    val connectionsState = remember { mutableStateOf(Storage.loadConnections(ctx)) }

    val currentMoveState = remember { mutableStateOf<Move?>(null) }
    val nextMoveState = remember { mutableStateOf<Move?>(null) }
    val connectionNoteState = remember { mutableStateOf<String?>(null) }

    val deadEndMoveNameState = remember { mutableStateOf<String?>(null) }

    val announcer = remember { MoveAnnouncer(ctx) }
    DisposableEffect(Unit) {
        onDispose { announcer.shutdown() }
    }

    fun pickNextFor(
        from: Move,
        moves: List<Move>,
        connections: List<Connection>,
        excludeMoveId: String? = null
    ): Pair<Move, String?>? {
        val outgoing = connections.filter { it.from == from.id && it.smoothness > 0 }
        if (outgoing.isEmpty()) return null

        val candidateConnections = outgoing.filter { it.to != excludeMoveId }
        if (candidateConnections.isEmpty()) return null

        val chosenConnection = candidateConnections.random()
        val targetMove = moves.find { it.id == chosenConnection.to } ?: return null
        return targetMove to chosenConnection.notes
    }

    fun pickRandomStartPair() {
        val moves = movesState.value
        val connections = connectionsState.value

        if (moves.size < 2) {
            deadEndMoveNameState.value = if (moves.isNotEmpty()) moves.first().name else "No moves"
            return
        }

        // Pick a random starting move that actually has at least one outgoing connection
        val candidatesForStart = moves.filter { move ->
            connections.any { it.from == move.id && it.smoothness > 0 }
        }

        if (candidatesForStart.isEmpty()) {
            deadEndMoveNameState.value = "No move has any outgoing connections"
            return
        }

        val start = candidatesForStart.random()
        val pair = pickNextFor(start, moves, connections) ?: run {
            deadEndMoveNameState.value = start.name
            return
        }

        val (next, note) = pair
        currentMoveState.value = start
        nextMoveState.value = next
        connectionNoteState.value = note

        // Speak NEXT move
        announcer.announce(next.name, 120)
    }

    fun advanceToNext() {
        val moves = movesState.value
        val connections = connectionsState.value
        val current = currentMoveState.value
        val next = nextMoveState.value

        if (current == null || next == null) {
            pickRandomStartPair()
            return
        }

        // New current is previous next
        val newCurrent = next
        val pair = pickNextFor(newCurrent, moves, connections)

        if (pair == null) {
            // Dead end
            deadEndMoveNameState.value = newCurrent.name
            return
        }

        val (newNext, note) = pair
        currentMoveState.value = newCurrent
        nextMoveState.value = newNext
        connectionNoteState.value = note

        // Speak NEXT move
        announcer.announce(newNext.name, 120)
    }

    fun rerollNext() {
        val moves = movesState.value
        val connections = connectionsState.value
        val current = currentMoveState.value
        val existingNext = nextMoveState.value

        if (current == null) {
            pickRandomStartPair()
            return
        }

        val pair = pickNextFor(
            from = current,
            moves = moves,
            connections = connections,
            excludeMoveId = existingNext?.id
        )

        if (pair == null) {
            // No alternative, treat as dead end
            deadEndMoveNameState.value = current.name
            return
        }

        val (newNext, note) = pair
        nextMoveState.value = newNext
        connectionNoteState.value = note

        // Speak NEXT move
        announcer.announce(newNext.name, 120)
    }

    // Initialize first pair when screen appears
    LaunchedEffect(Unit) {
        if (currentMoveState.value == null || nextMoveState.value == null) {
            pickRandomStartPair()
        }
    }

    // Dead-end dialog
    deadEndMoveNameState.value?.let { moveName ->
        AlertDialog(
            onDismissRequest = { onBack() },
            confirmButton = {
                TextButton(onClick = onBack) {
                    Text("OK")
                }
            },
            title = { Text("No further connections") },
            text = {
                Text("Move \"$moveName\" has no compatible next moves. Returning to the main menu.")
            }
        )
    }

    val currentMove = currentMoveState.value
    val nextMove = nextMoveState.value
    val connectionNote = connectionNoteState.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Move",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = currentMove?.name ?: "—",
                    fontSize = 20.sp, // base size
                    style = MaterialTheme.typography.headlineSmall
                )

                if (!connectionNote.isNullOrBlank()) {
                    Text(
                        text = connectionNote,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "Next Move",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = nextMove?.name ?: "—",
                    fontSize = 30.sp, // ~1.5× bigger
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Button(
            onClick = { advanceToNext() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Next Move")
        }

        Button(
            onClick = { rerollNext() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reroll")
        }

        Text(
            text = "Tip: \"Next Move\" is also spoken aloud.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
