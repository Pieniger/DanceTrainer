package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Storage

@Composable
fun DanceScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    // Load all moves from the currently-selected style folder
    val movesState = remember { mutableStateOf(Storage.loadMoves(ctx)) }

    // Current & next move
    val currentMoveState = remember { mutableStateOf<Move?>(null) }
    val nextMoveState = remember { mutableStateOf<Move?>(null) }

    // TTS announcer
    val announcer = remember { MoveAnnouncer(ctx) }
    DisposableEffect(Unit) {
        onDispose { announcer.shutdown() }
    }

    fun pickTwoDistinctRandomMoves() {
        val moves = movesState.value
        if (moves.size < 2) {
            currentMoveState.value = moves.firstOrNull()
            nextMoveState.value = null
            return
        }

        val first = moves.random()
        val second = moves.filter { it.id != first.id }.random()

        currentMoveState.value = first
        nextMoveState.value = second

        // Speak NEXT move (not current)
        announcer.announce(second.name, 120)
    }

    fun advanceToNext() {
        val moves = movesState.value
        val current = currentMoveState.value
        val next = nextMoveState.value

        if (moves.size < 2) {
            currentMoveState.value = moves.firstOrNull()
            nextMoveState.value = null
            return
        }

        // If we don't yet have both moves, just pick fresh
        if (current == null || next == null) {
            pickTwoDistinctRandomMoves()
            return
        }

        // Promote next -> current, pick a new next that is different
        val newCurrent = next
        val newNext = moves.filter { it.id != newCurrent.id }.random()

        currentMoveState.value = newCurrent
        nextMoveState.value = newNext

        // Speak NEXT move
        announcer.announce(newNext.name, 120)
    }

    fun rerollNext() {
        val moves = movesState.value
        val current = currentMoveState.value

        if (moves.size < 2) {
            return
        }

        if (current == null) {
            pickTwoDistinctRandomMoves()
            return
        }

        // Pick a different next move than the current one
        val newNext = moves.filter { it.id != current.id }.random()
        nextMoveState.value = newNext

        // Speak NEXT move
        announcer.announce(newNext.name, 120)
    }

    // Initial pair on first composition
    LaunchedEffect(Unit) {
        if (currentMoveState.value == null && nextMoveState.value == null) {
            pickTwoDistinctRandomMoves()
        }
    }

    val currentMove = currentMoveState.value
    val nextMove = nextMoveState.value

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

                // No connection note in this simplified version
                // (we'll re-introduce it once connection logic is stable)

                Text(
                    text = "Next Move",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = nextMove?.name ?: "—",
                    fontSize = 30.sp, // larger text for next move
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
            text = "Tip: \"Next Move\" is spoken aloud.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
