package com.example.dancetrainer.ui

import android.widget.Toast
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Connection
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Storage
import kotlin.random.Random

@Composable
fun ConnectionFinderScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Load moves + connections for the currently selected style.
    var moves by remember { mutableStateOf(Storage.loadMoves(context)) }
    var connections by remember { mutableStateOf(Storage.loadConnections(context).toMutableList()) }

    // The current displayed pair.
    var currentFrom by remember { mutableStateOf<Move?>(null) }
    var currentTo by remember { mutableStateOf<Move?>(null) }

    // Pairs we've already evaluated (works OR doesn't work) in this session
    // plus all existing saved connections so we don't re-propose them.
    var triedPairs by remember { mutableStateOf(setOf<Pair<String, String>>()) }

    // Small status / info line.
    var infoMessage by remember { mutableStateOf<String?>(null) }

    // Dialog state for rating a "works" connection.
    var showRateDialog by remember { mutableStateOf(false) }
    var pendingFrom by remember { mutableStateOf<Move?>(null) }
    var pendingTo by remember { mutableStateOf<Move?>(null) }

    // Initial setup: mark existing connections as tried, then pick first random pair.
    LaunchedEffect(Unit) {
        val existing = connections.map { it.from to it.to }.toSet()
        triedPairs = existing
        if (moves.size >= 2) {
            pickRandomPair(
                moves = moves,
                triedPairs = triedPairs,
                connections = connections,
                keepFirst = null,
                onPairPicked = { f, t, msg ->
                    currentFrom = f
                    currentTo = t
                    infoMessage = msg
                }
            )
        } else {
            infoMessage = "You need at least two moves in this style to use the Connection Finder."
        }
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
            // Current pair card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Rate Connections",
                        style = MaterialTheme.typography.titleLarge
                    )

                    if (currentFrom == null || currentTo == null) {
                        Text(
                            "No pair available.\n\n" +
                                "If you just changed styles or moves, try leaving " +
                                "and re-opening this screen.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            "Pick whether these two moves work well together.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        PairDisplay(currentFrom!!, currentTo!!)

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    // "Works" → open rating dialog
                                    pendingFrom = currentFrom
                                    pendingTo = currentTo
                                    showRateDialog = true
                                }
                            ) {
                                Text("Works")
                            }

                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    // "Doesn't work" → mark this pair tried, keep move 1,
                                    // and pick a new move 2 that hasn't been tried with move 1.
                                    val from = currentFrom
                                    val to = currentTo
                                    if (from != null && to != null) {
                                        triedPairs = triedPairs + (from.id to to.id)
                                        pickRandomPair(
                                            moves = moves,
                                            triedPairs = triedPairs,
                                            connections = connections,
                                            keepFirst = from,
                                            onPairPicked = { f, t, msg ->
                                                currentFrom = f
                                                currentTo = t
                                                infoMessage = msg
                                            }
                                        )
                                    }
                                }
                            ) {
                                Text("Doesn't work")
                            }

                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    // "Reroll" → pick a completely new pair.
                                    pickRandomPair(
                                        moves = moves,
                                        triedPairs = triedPairs,
                                        connections = connections,
                                        keepFirst = null,
                                        onPairPicked = { f, t, msg ->
                                            currentFrom = f
                                            currentTo = t
                                            infoMessage = msg
                                        }
                                    )
                                }
                            ) {
                                Text("Reroll")
                            }
                        }
                    }
                }
            }

            // Info / status text
            infoMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                "Tried pairs this session: ${triedPairs.size}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.weight(1f))
        }
    }

    // Rating dialog when user presses "Works"
    if (showRateDialog && pendingFrom != null && pendingTo != null) {
        RateConnectionDialog(
            from = pendingFrom!!,
            to = pendingTo!!,
            onCancel = {
                showRateDialog = false
                pendingFrom = null
                pendingTo = null
            },
            onSave = { smoothness, note ->
                val from = pendingFrom!!
                val to = pendingTo!!
                // Remove any existing connection with same from/to
                val updated = connections.filterNot { it.from == from.id && it.to == to.id }.toMutableList()
                updated += Connection(
                    from = from.id,
                    to = to.id,
                    smoothness = smoothness,
                    note = note
                )
                connections = updated
                Storage.saveConnections(context, connections)

                // Mark this pair as tried
                triedPairs = triedPairs + (from.id to to.id)

                // Now continue from move 2 → move 1 becomes old move 2
                pickRandomPair(
                    moves = moves,
                    triedPairs = triedPairs,
                    connections = connections,
                    keepFirst = to,
                    onPairPicked = { f, t, msg ->
                        currentFrom = f
                        currentTo = t
                        infoMessage = msg
                    }
                )

                val toastText = "Saved connection: ${from.name} → ${to.name} (smoothness $smoothness)"
                Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()

                showRateDialog = false
                pendingFrom = null
                pendingTo = null
            }
        )
    }
}

@Composable
private fun PairDisplay(from: Move, to: Move) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Move 1", style = MaterialTheme.typography.labelSmall)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(from.name, style = MaterialTheme.typography.titleMedium)
                if (from.note.isNotBlank()) {
                    Text(
                        from.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text("Move 2", style = MaterialTheme.typography.labelSmall)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(to.name, style = MaterialTheme.typography.titleMedium)
                if (to.note.isNotBlank()) {
                    Text(
                        to.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RateConnectionDialog(
    from: Move,
    to: Move,
    onCancel: () -> Unit,
    onSave: (smoothness: Int, note: String) -> Unit
) {
    var sliderValue by remember { mutableStateOf(5f) }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("This combination works") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "${from.name} → ${to.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("How smooth does this feel? (1–10)")
                Text("Smoothness: ${sliderValue.toInt()}")

                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 1f..10f,
                    steps = 8
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
            Button(onClick = {
                val s = sliderValue.toInt().coerceIn(1, 10)
                onSave(s, noteText.trim())
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Pick a random pair of moves that:
 *  - are different moves
 *  - do not already have a saved connection (in [connections])
 *  - are not in [triedPairs] (already evaluated this session)
 *
 * If [keepFirst] is non-null, we fix that as Move 1 and only choose Move 2.
 */
private fun pickRandomPair(
    moves: List<Move>,
    triedPairs: Set<Pair<String, String>>,
    connections: List<Connection>,
    keepFirst: Move?,
    onPairPicked: (from: Move?, to: Move?, infoMessage: String) -> Unit
) {
    if (moves.size < 2) {
        onPairPicked(null, null, "You need at least two moves to rate connections.")
        return
    }

    // Treat existing saved connections as already tried.
    val alreadyConnectedPairs = connections.map { it.from to it.to }.toSet()
    val forbiddenPairs = triedPairs + alreadyConnectedPairs

    val candidates: List<Pair<Move, Move>> =
        if (keepFirst != null) {
            val from = keepFirst
            moves.filter { it.id != from.id }
                .map { from to it }
                .filter { (f, t) -> (f.id to t.id) !in forbiddenPairs }
        } else {
            moves.flatMap { from ->
                moves.filter { it.id != from.id }.map { to -> from to to }
            }.filter { (f, t) -> (f.id to t.id) !in forbiddenPairs }
        }

    if (candidates.isEmpty()) {
        val msg = if (keepFirst != null) {
            "No more unused partners for ${keepFirst.name}.\nTry Reroll or adjust your moves."
        } else {
            "No unused move pairs left.\nYou may have rated all possible pairs for this style."
        }
        onPairPicked(null, null, msg)
        return
    }

    val (from, to) = candidates[Random.nextInt(candidates.size)]
    val baseMsg = if (keepFirst != null) {
        "Keeping ${from.name} and trying a new partner."
    } else {
        "Random pair selected."
    }
    onPairPicked(from, to, baseMsg)
}
