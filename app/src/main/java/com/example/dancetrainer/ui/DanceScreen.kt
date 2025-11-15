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

/**
 * Result of choosing a next move from a given move.
 */
private data class NextStep(
    val nextMove: Move,
    val note: String?
)

@Composable
fun DanceScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    // Load moves and connections from the currently selected style/folder.
    val movesState = remember { mutableStateOf(Storage.loadMoves(ctx)) }
    val connectionsState = remember { mutableStateOf(Storage.loadConnections(ctx)) }

    val currentMoveState = remember { mutableStateOf<Move?>(null) }
    val nextMoveState = remember { mutableStateOf<Move?>(null) }
    val connectionNoteState = remember { mutableStateOf<String?>(null) }

    val deadEndMoveNameState = remember { mutableStateOf<String?>(null) }

    // TTS announcer
    val announcer = remember { MoveAnnouncer(ctx) }
    DisposableEffect(Unit) {
        onDispose { announcer.shutdown() }
    }

    /**
     * Pick a random *positive* connection from [fromMove] to some other Move.
     * Optionally excludes a specific target move id.
     *
     * Returns NextStep or null if no valid next move is possible.
     */
    fun pickNextFor(
        fromMove: Move,
        moves: List<Move>,
        connections: List<Connection>,
        excludeMoveId: String? = null
    ): NextStep? {
        // All outgoing positive connections from this move
        val outgoing = connections.filter { conn ->
            conn.from == fromMove.id && conn.smoothness > 0
        }
        if (outgoing.isEmpty()) return null

        // Optionally exclude one specific target
        val candidates = if (excludeMoveId != null) {
            outgoing.filter { conn -> conn.to != excludeMoveId }
        } else {
            outgoing
        }
        if (candidates.isEmpty()) return null

        // Choose a connection at random
        val chosen: Connection = candidates.random()

        // Find target Move
        val targetMove: Move = moves.firstOrNull { m -> m.id == chosen.to } ?: return null

        val note: String? = if (!chosen.notes.isNullOrBlank()) chosen.notes else null

        return NextStep(
            nextMove = targetMove,
            note = note
        )
    }

    fun pickRandomStartPair() {
        val moves = movesState.value
        val connections = connectionsState.value

        if (moves.size < 2) {
            deadEndMoveNameState.value =
                if (moves.isNotEmpty()) moves.first().name else "No moves"
            return
        }

        // Start only from moves that have at least one outgoing positive connection
        val candidatesForStart: List<Move> = moves.filter { move ->
            connections.any { conn ->
                conn.from == move.id && conn.smoothness > 0
            }
        }

        if (candidatesForStart.isEmpty()) {
            deadEndMoveNameState.value = "No move has any outgoing connections"
            return
        }

        val start: Move = candidatesForStart.random()
        val nextStep: NextStep? = pickNextFor(start, moves, connections)

        if (nextStep == null) {
            deadEndMoveNameState.value = start.name
            return
        }

        currentMoveState.value = start
        nextMoveState.value = nextStep.nextMove
        connectionNoteState.value = nextStep.note

        // Speak NEXT move
        announcer.announce(nextStep.nextMove.name, 120)
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

        // New current is previous NEXT
        val newCurrent = next
        val nextStep: NextStep? = pickNextFor(newCurrent, moves, connections)

        if (nextStep == null) {
            deadEndMoveNameState.value = newCurrent.name
            return
        }

        currentMoveState.value = newCurrent
        nextMoveState.value = nextStep.nextMove
        connectionNoteState.value = nextStep.note

        // Speak NEXT move
        announcer.announce(nextStep.nextMove.name, 120)
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

        val nextStep: NextStep? = pickNextFor(
            fromMove = current,
            moves = moves,
            connections = connections,
            excludeMoveId = existingNext?.id
        )

        if (nextStep == null) {
            deadEndMoveNameState.value = current.name
            return
        }

        nextMoveState.value = nextStep.nextMove
        connectionNoteState.value = nextStep.note

        // Speak NEXT move
        announcer.announce(nextStep.nextMove.name, 120)
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Move",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = currentMove?.name ?: "—",
                    fontSize = 20.sp,
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
                    fontSize = 30.sp,   // ~1.5× bigger
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
