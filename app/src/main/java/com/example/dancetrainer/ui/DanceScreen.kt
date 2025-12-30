package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Connection
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Storage
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanceScreen(onBack: () -> Unit) {

    val ctx = androidx.compose.ui.platform.LocalContext.current

    val moves = remember { Storage.loadMoves(ctx) }
    val connections = remember { Storage.loadConnections(ctx) }

    var currentMove by remember { mutableStateOf<Move?>(null) }
    var nextMove by remember { mutableStateOf<Move?>(null) }
    var nextNote by remember { mutableStateOf<String?>(null) }

    fun rollNext(from: Move) {
        val (move, note) = pickNextMove(
            moves = moves,
            connections = connections,
            from = from
        )
        nextMove = move
        nextNote = note
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
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // CURRENT MOVE
            Text("Current Move", style = MaterialTheme.typography.titleMedium)

            Text(
                currentMove?.name ?: "—",
                style = MaterialTheme.typography.headlineMedium
            )

            if (currentMove?.notes?.isNotBlank() == true) {
                Text(currentMove!!.notes, style = MaterialTheme.typography.bodySmall)
            }

            Divider()

            // NEXT MOVE
            Text("Next Move", style = MaterialTheme.typography.titleMedium)

            Text(
                nextMove?.name ?: "—",
                style = MaterialTheme.typography.headlineLarge
            )

            if (!nextNote.isNullOrBlank()) {
                Text(nextNote!!, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))

            // CONTROLS
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                Button(
                    enabled = moves.isNotEmpty(),
                    onClick = {
                        currentMove = moves.random()
                        rollNext(currentMove!!)
                    }
                ) {
                    Text("Start")
                }

                Button(
                    enabled = currentMove != null,
                    onClick = {
                        currentMove = nextMove
                        if (currentMove != null) {
                            rollNext(currentMove!!)
                        }
                    }
                ) {
                    Text("Next")
                }

                Button(
                    enabled = currentMove != null,
                    onClick = {
                        rollNext(currentMove!!)
                    }
                ) {
                    Text("Reroll")
                }
            }
        }
    }
}

/**
 * Smoothness-weighted next-move picker.
 *
 * - Excludes:
 *   - the current move
 *   - any connection where works == false
 * - Weight = smoothness (1–5), linear
 */
private fun pickNextMove(
    moves: List<Move>,
    connections: List<Connection>,
    from: Move
): Pair<Move?, String?> {

    if (moves.size < 2) return null to null

    val positive = connections.filter {
        it.fromId == from.id && it.works
    }

    val blockedTargets = connections
        .filter { it.fromId == from.id && !it.works }
        .map { it.toId }
        .toSet()

    val candidates = moves.filter {
        it.id != from.id && it.id !in blockedTargets
    }

    if (candidates.isEmpty()) return null to null

    val weighted = candidates.map { move ->
        val conn = positive.firstOrNull { it.toId == move.id }
        val smoothness = (conn?.smoothness ?: 3).coerceIn(1, 5)
        move to smoothness
    }

    val total = weighted.sumOf { it.second }
    if (total <= 0) {
        val chosen = candidates.random()
        val conn = positive.firstOrNull { it.toId == chosen.id }
        return chosen to conn?.notes
    }

    var r = Random.nextInt(total)
    for ((move, weight) in weighted) {
        if (r < weight) {
            val conn = positive.firstOrNull { it.toId == move.id }
            return move to conn?.notes
        }
        r -= weight
    }

    val fallback = weighted.last().first
    val conn = positive.firstOrNull { it.toId == fallback.id }
    return fallback to conn?.notes
}
