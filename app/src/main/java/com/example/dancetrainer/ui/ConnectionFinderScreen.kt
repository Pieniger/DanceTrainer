package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Connection
import com.example.dancetrainer.data.ConnectionResult
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionFinderScreen(
    startMoveId: String?,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    var moves by remember { mutableStateOf(Storage.loadMoves(ctx)) }
    var connections by remember { mutableStateOf(Storage.loadConnections(ctx).toMutableList()) }

    fun persistConnections() {
        Storage.saveConnections(ctx, connections)
    }

    var currentMove1 by remember { mutableStateOf<Move?>(null) }
    var currentMove2 by remember { mutableStateOf<Move?>(null) }

    var showWorksDialog by remember { mutableStateOf(false) }
    var worksSmoothness by remember { mutableStateOf(TextFieldValue("5")) }
    var worksNotes by remember { mutableStateOf(TextFieldValue("")) }

    var infoMessage by remember { mutableStateOf<String?>(null) }

    fun isKnownPair(fromId: String, toId: String): Boolean {
        return connections.any { it.fromId == fromId && it.toId == toId }
    }

    fun pickRandomUnknownPair(keepFirst: Move? = null): Pair<Move, Move>? {
        val list = moves
        if (list.size < 2) return null

        val candidates = mutableListOf<Pair<Move, Move>>()

        for (a in list) {
            if (keepFirst != null && a.id != keepFirst.id) continue
            for (b in list) {
                if (a.id == b.id) continue
                if (isKnownPair(a.id, b.id)) continue
                candidates += a to b
            }
        }

        if (candidates.isEmpty()) return null
        return candidates.random()
    }

    fun refreshMovesAndConnections() {
        moves = Storage.loadMoves(ctx)
        connections = Storage.loadConnections(ctx).toMutableList()
    }

    fun initPair() {
        refreshMovesAndConnections()
        if (moves.size < 2) {
            infoMessage = "You need at least two moves in this style to find connections."
            currentMove1 = null
            currentMove2 = null
            return
        }

        val start = startMoveId?.let { id -> moves.firstOrNull { it.id == id } }
        val pair = pickRandomUnknownPair(keepFirst = start)
            ?: pickRandomUnknownPair()

        if (pair == null) {
            infoMessage = "All pairs are already evaluated for this style."
            currentMove1 = null
            currentMove2 = null
        } else {
            infoMessage = null
            currentMove1 = pair.first
            currentMove2 = pair.second
        }
    }

    LaunchedEffect(Unit) {
        initPair()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
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
            if (infoMessage != null) {
                Text(infoMessage!!, style = MaterialTheme.typography.bodyLarge)
            }

            val m1 = currentMove1
            val m2 = currentMove2

            if (m1 != null && m2 != null) {
                Text("Move 1", style = MaterialTheme.typography.titleSmall)
                Text(m1.name, style = MaterialTheme.typography.titleLarge)
                if (m1.notes.isNotBlank()) {
                    Text(m1.notes, style = MaterialTheme.typography.bodyMedium)
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Move 2", style = MaterialTheme.typography.titleSmall)
                Text(m2.name, style = MaterialTheme.typography.titleLarge)
                if (m2.notes.isNotBlank()) {
                    Text(m2.notes, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            // "Works" -> open dialog for smoothness and notes
                            worksSmoothness = TextFieldValue("5")
                            worksNotes = TextFieldValue("")
                            showWorksDialog = true
                        }
                    ) {
                        Text("Works")
                    }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            // "Doesn't work" -> record negative connection, keep move1, new move2
                            val neg = Connection(
                                fromId = m1.id,
                                toId = m2.id,
                                result = ConnectionResult.DOESNT_WORK,
                                smoothness = null,
                                notes = ""
                            )
                            connections.add(neg)
                            persistConnections()

                            val nextPair = pickRandomUnknownPair(keepFirst = m1)
                            if (nextPair == null) {
                                infoMessage =
                                    "No more unknown partners for '${m1.name}'. Try rerolling a completely new pair."
                                currentMove2 = null
                            } else {
                                infoMessage = null
                                currentMove1 = nextPair.first
                                currentMove2 = nextPair.second
                            }
                        }
                    ) {
                        Text("Doesn't work")
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        // Reroll -> keep move1, get a different move2
                        val nextPair = pickRandomUnknownPair(keepFirst = m1)
                        if (nextPair == null) {
                            infoMessage =
                                "No more unknown partners for '${m1.name}'. Try finding a new pair."
                            currentMove2 = null
                        } else {
                            infoMessage = null
                            currentMove1 = nextPair.first
                            currentMove2 = nextPair.second
                        }
                    }
                ) {
                    Text("Reroll")
                }
            }
        }
    }

    if (showWorksDialog) {
        val m1 = currentMove1
        val m2 = currentMove2
        if (m1 != null && m2 != null) {
            AlertDialog(
                onDismissRequest = { showWorksDialog = false },
                title = { Text("Connection details") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("How smooth is this transition?")
                        OutlinedTextField(
                            value = worksSmoothness,
                            onValueChange = { worksSmoothness = it },
                            label = { Text("Smoothness (0–10)") }
                        )
                        OutlinedTextField(
                            value = worksNotes,
                            onValueChange = { worksNotes = it },
                            label = { Text("Notes (optional)") },
                            minLines = 2
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val smooth = worksSmoothness.text.toIntOrNull()?.coerceIn(0, 10)
                        val note = worksNotes.text.trim()

                        val conn = Connection(
                            fromId = m1.id,
                            toId = m2
