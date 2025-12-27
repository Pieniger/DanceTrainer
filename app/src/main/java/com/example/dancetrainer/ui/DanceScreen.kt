package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Connection
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Prefs
import com.example.dancetrainer.data.Storage
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanceScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    val priorityMode = Prefs.isPriorityMode(ctx)

    var moves by remember { mutableStateOf(Storage.loadMoves(ctx)) }
    var connections by remember { mutableStateOf(Storage.loadConnections(ctx)) }

    var currentMove by remember { mutableStateOf<Move?>(moves.randomOrNull()) }
    var note by remember { mutableStateOf<String?>(null) }

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
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                currentMove?.name ?: "No move",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(8.dp))

            if (!note.isNullOrBlank()) {
                Text(note!!, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                val (next, n) = pickNextMove(
                    moves = moves,
                    connections = connections,
                    from = currentMove ?: return@Button,
                    priorityMode = priorityMode
                )
                currentMove = next
                note = n
            }) {
                Text("Next move")
            }
        }
    }
}

/**
 * Choose next move:
 * - excludes same move
 * - excludes connections where works == false
 * - priority mode weights by smoothness (1–5)
 */
private fun pickNextMove(
    moves: List<Move>,
    connections: List<Connection>,
    from: Move,
    priorityMode: Boolean
): Pair<Move?, String?> {

    val blocked = connections
        .filter { it.fromId == from.id && !it.works }
        .map { it.toId }
        .toSet()

    val positive = connections.filter { it.fromId == from.id && it.works }

    val candidates = moves.filter {
        it.id != from.id && it.id !in blocked
    }

    if (candidates.isEmpty()) return null to null

    if (!priorityMode) {
        val chosen = candidates.random()
        val conn = positive.firstOrNull { it.toId == chosen.id }
        return chosen to conn?.notes
    }

    // linear weighting: 1–5
    val weighted = candidates.map { move ->
        val smooth = positive
            .firstOrNull { it.toId == move.id }
            ?.smoothness
            ?.coerceIn(1, 5)
            ?: 3
        move to smooth
    }

    val total = weighted.sumOf { it.second }
    var r = Random.nextInt(total)

    for ((move, w) in weighted) {
        if (r < w) {
            val conn = positive.firstOrNull { it.toId == move.id }
            return move to conn?.notes
        }
        r -= w
    }

    val fallback = weighted.last().first
    val conn = positive.firstOrNull { it.toId == fallback.id }
    return fallback to conn?.notes
}
