package com.example.dancetrainer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Connection
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionFinderScreen(
    startMoveId: String?,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    var moves by remember { mutableStateOf<List<Move>>(emptyList()) }
    var connections by remember { mutableStateOf<List<Connection>>(emptyList()) }

    // Selected start/target for path finding
    var selectedStartId by remember { mutableStateOf<String?>(null) }
    var selectedTargetId by remember { mutableStateOf<String?>(null) }

    // Result path (list of move IDs)
    var pathResult by remember { mutableStateOf<List<String>>(emptyList()) }
    var pathError by remember { mutableStateOf<String?>(null) }

    // Dialog state for picking moves and adding connections
    var pickStartDialogOpen by remember { mutableStateOf(false) }
    var pickTargetDialogOpen by remember { mutableStateOf(false) }
    var addDialogOpen by remember { mutableStateOf(false) }

    // New connection fields
    var newFromId by remember { mutableStateOf<String?>(null) }
    var newToId by remember { mutableStateOf<String?>(null) }
    var newSmoothness by remember { mutableFloatStateOf(5f) }

    // Load data when screen opens
    LaunchedEffect(Unit) {
        moves = Storage.loadMoves(ctx)
        connections = Storage.loadConnections(ctx)

        // Pre-select start if provided
        if (startMoveId != null && moves.any { it.id == startMoveId }) {
            selectedStartId = startMoveId
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
            // --- PATH FINDING SECTION ---

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Find Connection Path", style = MaterialTheme.typography.titleMedium)

                    val startName = selectedStartId?.let { id ->
                        moves.find { it.id == id }?.name ?: "(unknown)"
                    } ?: "-"

                    val targetName = selectedTargetId?.let { id ->
                        moves.find { it.id == id }?.name ?: "(not selected)"
                    } ?: "-"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Start move", style = MaterialTheme.typography.labelSmall)
                            Text(
                                startName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .clickable { pickStartDialogOpen = true }
                            )
                        }
                        Column {
                            Text("Target move", style = MaterialTheme.typography.labelSmall)
                            Text(
                                targetName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .clickable { pickTargetDialogOpen = true }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            pathError = null
                            pathResult = emptyList()

                            val from = selectedStartId
                            val to = selectedTargetId

                            if (from == null || to == null) {
                                pathError = "Please select both a start and a target move."
                            } else if (from == to) {
                                pathResult = listOf(from)
                            } else {
                                val result = findConnectionPath(from, to, connections)
                                if (result.isEmpty()) {
                                    pathError = "No path found from '$startName' to '$targetName'."
                                } else {
                                    pathResult = result
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Find Path")
                    }

                    if (pathError != null) {
                        Text(
                            pathError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (pathResult.isNotEmpty()) {
                        Text("Path:", style = MaterialTheme.typography.labelMedium)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            pathResult.forEachIndexed { index, id ->
                                val name = moves.find { it.id == id }?.name ?: id
                                Text("${index + 1}. $name")
                            }
                        }
                    }
                }
            }

            // --- EXISTING CONNECTIONS LIST ---

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Connections", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { 
                            newFromId = null
                            newToId = null
                            newSmoothness = 5f
                            addDialogOpen = true 
                        }) {
                            Text("Add")
                        }
                    }

                    if (connections.isEmpty()) {
                        Text("No connections yet.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(connections, key = { "${it.from}-${it.to}" }) { c ->
                                val fromName = moves.find { it.id == c.from }?.name ?: c.from
                                val toName = moves.find { it.id == c.to }?.name ?: c.to
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("$fromName → $toName")
                                        Text(
                                            "Smoothness: ${c.smoothness}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    TextButton(onClick = {
                                        connections = connections.filterNot { it.from == c.from && it.to == c.to }
                                        Storage.saveConnections(ctx, connections)
                                    }) {
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS FOR PICKING MOVES ---

    if (pickStartDialogOpen) {
        MovePickerDialog(
            title = "Select Start Move",
            moves = moves,
            onDismiss = { pickStartDialogOpen = false },
            onPick = { id ->
                selectedStartId = id
                pickStartDialogOpen = false
            }
        )
    }

    if (pickTargetDialogOpen) {
        MovePickerDialog(
            title = "Select Target Move",
            moves = moves,
            onDismiss = { pickTargetDialogOpen = false },
            onPick = { id ->
                selectedTargetId = id
                pickTargetDialogOpen = false
            }
        )
    }

    // --- DIALOG FOR ADDING NEW CONNECTION ---

    if (addDialogOpen) {
        AddConnectionDialog(
            moves = moves,
            initialFromId = newFromId,
            initialToId = newToId,
            initialSmoothness = newSmoothness,
            onDismiss = { addDialogOpen = false },
            onConfirm = { fromId, toId, smooth ->
                val updated = connections
                    .filterNot { it.from == fromId && it.to == toId } + Connection(
                    from = fromId,
                    to = toId,
                    smoothness = smooth
                )
                connections = updated
                Storage.saveConnections(ctx, connections)
                addDialogOpen = false
            }
        )
    }
}

/**
 * Simple BFS: find a path from startId to targetId using only connections
 * with smoothness > 0. Returns list of move IDs (including start and target),
 * or empty list if no path.
 */
private fun findConnectionPath(
    startId: String,
    targetId: String,
    connections: List<Connection>
): List<String> {
    if (startId == targetId) return listOf(startId)

    // adjacency: fromId -> [toIds]
    val adj: Map<String, List<String>> = connections
        .filter { it.smoothness > 0 }
        .groupBy { it.from }
        .mapValues { entry -> entry.value.map { it.to } }

    val queue: ArrayDeque<String> = ArrayDeque()
    val visited = mutableSetOf<String>()
    val parent = mutableMapOf<String, String?>()

    queue.add(startId)
    visited.add(startId)
    parent[startId] = null

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (current == targetId) break

        val neighbors = adj[current].orEmpty()
        for (n in neighbors) {
            if (!visited.add(n)) continue
            parent[n] = current
            queue.add(n)
        }
    }

    if (!visited.contains(targetId)) {
        return emptyList()
    }

    // Reconstruct path
    val path = mutableListOf<String>()
    var cur: String? = targetId
    while (cur != null) {
        path.add(cur)
        cur = parent[cur]
    }
    path.reverse()
    return path
}

// ---------- Helper UI composables ----------

@Composable
private fun MovePickerDialog(
    title: String,
    moves: List<Move>,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            if (moves.isEmpty()) {
                Text("No moves available in this style.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(moves, key = { it.id }) { move ->
                        TextButton(
                            onClick = { onPick(move.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(move.name)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun AddConnectionDialog(
    moves: List<Move>,
    initialFromId: String?,
    initialToId: String?,
    initialSmoothness: Float,
    onDismiss: () -> Unit,
    onConfirm: (fromId: String, toId: String, smoothness: Int) -> Unit
) {
    var fromId by remember { mutableStateOf(initialFromId) }
    var toId by remember { mutableStateOf(initialToId) }
    var smooth by remember { mutableFloatStateOf(initialSmoothness.coerceIn(1f, 10f)) }

    var pickFromOpen by remember { mutableStateOf(false) }
    var pickToOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Connection") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text("From move", style = MaterialTheme.typography.labelSmall)
                    val fromName = fromId?.let { id ->
                        moves.find { it.id == id }?.name ?: "(unknown)"
                    } ?: "-"

                    OutlinedButton(
                        onClick = { pickFromOpen = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(fromName)
                    }
                }

                Column {
                    Text("To move", style = MaterialTheme.typography.labelSmall)
                    val toName = toId?.let { id ->
                        moves.find { it.id == id }?.name ?: "(unknown)"
                    } ?: "-"

                    OutlinedButton(
                        onClick = { pickToOpen = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(toName)
                    }
                }

                Column {
                    Text("Smoothness: ${smooth.toInt()}", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = smooth,
                        onValueChange = { smooth = it },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val f = fromId
                val t = toId
                if (f != null && t != null && f != t) {
                    onConfirm(f, t, smooth.toInt().coerceIn(1, 10))
                } else {
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (pickFromOpen) {
        MovePickerDialog(
            title = "Select From Move",
            moves = moves,
            onDismiss = { pickFromOpen = false },
            onPick = {
                fromId = it
                pickFromOpen = false
            }
        )
    }

    if (pickToOpen) {
        MovePickerDialog(
            title = "Select To Move",
            moves = moves,
            onDismiss = { pickToOpen = false },
            onPick = {
                toId = it
                pickToOpen = false
            }
        )
    }
}
