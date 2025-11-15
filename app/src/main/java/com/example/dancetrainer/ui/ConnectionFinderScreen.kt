package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Connection
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Storage

private fun pickRandomUnknownPair(
    moves: List<Move>,
    connections: List<Connection>
): Pair<Move, Move>? {
    if (moves.size < 2) return null
    val used = connections.associateBy { it.fromId to it.toId }
    val candidates = mutableListOf<Pair<Move, Move>>()
    for (a in moves) {
        for (b in moves) {
            if (a.id == b.id) continue
            if ((a.id to b.id) in used) continue
            candidates += a to b
        }
    }
    if (candidates.isEmpty()) return null
    return candidates.random()
}

private fun pickUnknownForFrom(
    from: Move,
    moves: List<Move>,
    connections: List<Connection>,
    excludeToId: String? = null
): Move? {
    val used = connections.associateBy { it.fromId to it.toId }
    val candidates = moves.filter { m ->
        m.id != from.id &&
                (excludeToId == null || m.id != excludeToId) &&
                (from.id to m.id) !in used
    }
    if (candidates.isEmpty()) return null
    return candidates.random()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionFinderScreen(
    startMoveId: String? = null,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    var moves by remember { mutableStateOf<List<Move>>(emptyList()) }
    var connections by remember { mutableStateOf<List<Connection>>(emptyList()) }
    var currentPair by remember { mutableStateOf<Pair<Move, Move>?>(null) }

    var showRateDialog by remember { mutableStateOf(false) }
    var smoothness by remember { mutableStateOf(3f) }
    var noteText by remember { mutableStateOf("") }

    var infoDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        moves = Storage.loadMoves(ctx)
        connections = Storage.loadConnections(ctx)

        val initialPair = run {
            if (startMoveId != null) {
                val from = moves.find { it.id == startMoveId }
                if (from != null) {
                    val to = pickUnknownForFrom(from, moves, connections)
                    if (to != null) from to to else null
                } else null
            } else null
        } ?: pickRandomUnknownPair(moves, connections)

        if (initialPair == null) {
            infoDialog = "No unknown move pairs left. Add more moves or clear connections."
        } else {
            currentPair = initialPair
        }
    }

    fun persistConnections(list: List<Connection>) {
        connections = list
        Storage.saveConnections(ctx, list)
    }

    fun rerollBoth() {
        val pair = pickRandomUnknownPair(moves, connections)
        if (pair == null) {
            infoDialog = "No unknown move pairs left. Add more moves or clear connections."
        } else {
            currentPair = pair
        }
    }

    fun onDoesntWork() {
        val pair = currentPair ?: return
        val from = pair.first
        val to = pair.second

        // store negative connection (works = false)
        val updated = connections.toMutableList()
        val idx = updated.indexOfFirst { it.fromId == from.id && it.toId == to.id }
        if (idx >= 0) {
            updated[idx] = updated[idx].copy(works = false, smoothness = 0, notes = "")
        } else {
            updated += Connection(
                fromId = from.id,
                toId = to.id,
                works = false,
                smoothness = 0,
                notes = ""
            )
        }
        persistConnections(updated)

        // keep from, find new to
        val newTo = pickUnknownForFrom(from, moves, updated, excludeToId = to.id)
        if (newTo == null) {
            // fallback: random unknown anywhere
            val pair2 = pickRandomUnknownPair(moves, updated)
            if (pair2 == null) {
                infoDialog = "No more unknown connections for this move."
            } else {
                currentPair = pair2
            }
        } else {
            currentPair = from to newTo
        }
    }

    fun onWorks() {
        smoothness = 3f
        noteText = ""
        showRateDialog = true
    }

    fun confirmWorks() {
        val pair = currentPair ?: return
        val from = pair.first
        val to = pair.second

        val rating = smoothness.toInt().coerceIn(1, 5)
        val notes = noteText.trim()

        val updated = connections.toMutableList()
        val idx = updated.indexOfFirst { it.fromId == from.id && it.toId == to.id }
        if (idx >= 0) {
            updated[idx] = updated[idx].copy(
                works = true,
                smoothness = rating,
                notes = notes
            )
        } else {
            updated += Connection(
                fromId = from.id,
                toId = to.id,
                works = true,
                smoothness = rating,
                notes = notes
            )
        }
        persistConnections(updated)

        // advance: new from = old to
        val newFrom = to
        val newTo = pickUnknownForFrom(newFrom, moves, updated)
        if (newTo == null) {
            // try random unknown anywhere
            val pair2 = pickRandomUnknownPair(moves, updated)
            if (pair2 == null) {
                infoDialog = "No unknown follow-up connections left."
            } else {
                currentPair = pair2
            }
        } else {
            currentPair = newFrom to newTo
        }

        showRateDialog = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connection Finder") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val pair = currentPair
            if (pair == null) {
                Text("No pair selected.", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { rerollBoth() }) {
                    Text("Try again")
                }
            } else {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Current pair:", style = MaterialTheme.typography.titleSmall)
                        Text("Move 1: ${pair.first.name}", style = MaterialTheme.typography.titleMedium)
                        Text("Move 2: ${pair.second.name}", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onWorks() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Works")
                    }
                    Button(
                        onClick = { onDoesntWork() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Doesn't work")
                    }
                }

                Button(
                    onClick = { rerollBoth() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reroll")
                }
            }
        }
    }

    if (showRateDialog && currentPair != null) {
        val pair = currentPair!!
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("Rate connection") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("How smooth is going from:")
                    Text("${pair.first.name} → ${pair.second.name}")
                    Text("Smoothness: ${smoothness.toInt()} (1–5)")
                    Slider(
                        value = smoothness,
                        onValueChange = { smoothness = it.coerceIn(1f, 5f) },
                        valueRange = 1f..5f
                    )
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { confirmWorks() }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    infoDialog?.let { message ->
        AlertDialog(
            onDismissRequest = { infoDialog = null },
            title = { Text("Info") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    infoDialog = null
                    onBack()
                }) {
                    Text("OK")
                }
            }
        )
    }
}
