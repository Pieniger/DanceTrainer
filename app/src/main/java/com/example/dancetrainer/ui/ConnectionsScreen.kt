package com.example.dancetrainer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Connection
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    var moves by remember { mutableStateOf(Storage.loadMoves(ctx)) }
    var connections by remember { mutableStateOf(Storage.loadConnections(ctx)) }

    val movesById = remember(moves) { moves.associateBy { it.id } }
    var selectedMove by remember { mutableStateOf<Move?>(null) }

    var editing by remember { mutableStateOf<Connection?>(null) }
    var smoothText by remember { mutableStateOf(TextFieldValue("")) }
    var notesText by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connections") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .padding(padding)
                .padding(12.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ---- MOVES ----
            Card(Modifier.weight(1f)) {
                LazyColumn(Modifier.padding(8.dp)) {
                    items(moves, key = { it.id }) { move ->
                        Text(
                            text = move.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMove = move }
                                .padding(8.dp),
                            style = if (move == selectedMove)
                                MaterialTheme.typography.titleMedium
                            else MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ---- CONNECTIONS ----
            Card(Modifier.weight(2f)) {
                if (selectedMove == null) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Select a move")
                    }
                } else {
                    val move = selectedMove!!
                    val outgoing = connections.filter { it.fromId == move.id }
                    val incoming = connections.filter { it.toId == move.id }

                    Column(Modifier.padding(12.dp)) {
                        Text("From ${move.name}", style = MaterialTheme.typography.titleMedium)

                        Spacer(Modifier.height(8.dp))
                        Text("Leads to")

                        outgoing.forEach { c ->
                            val name = movesById[c.toId]?.name ?: c.toId
                            ConnectionRow("→ $name", c) {
                                editing = c
                                smoothText = TextFieldValue(c.smoothness.toString())
                                notesText = TextFieldValue(c.notes)
                            }
                        }

                        Divider(Modifier.padding(vertical = 8.dp))
                        Text("Comes from")

                        incoming.forEach { c ->
                            val name = movesById[c.fromId]?.name ?: c.fromId
                            ConnectionRow("← $name", c) {
                                editing = c
                                smoothText = TextFieldValue(c.smoothness.toString())
                                notesText = TextFieldValue(c.notes)
                            }
                        }
                    }
                }
            }
        }
    }

    // ---- EDIT DIALOG ----
    editing?.let { conn ->
        AlertDialog(
            onDismissRequest = { editing = null },
            confirmButton = {
                TextButton(onClick = {
                    val updated = connections.map {
                        if (it === conn)
                            it.copy(
                                smoothness = smoothText.text.toIntOrNull()?.coerceIn(1, 5)
                                    ?: it.smoothness,
                                notes = notesText.text
                            )
                        else it
                    }
                    Storage.saveConnections(ctx, updated)
                    connections = updated
                    editing = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text("Cancel") }
            },
            title = { Text("Edit connection") },
            text = {
                Column {
                    OutlinedTextField(
                        value = smoothText,
                        onValueChange = { smoothText = it },
                        label = { Text("Smoothness (1–5)") }
                    )
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Notes") }
                    )
                }
            }
        )
    }
}

@Composable
private fun ConnectionRow(
    label: String,
    conn: Connection,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Text(label)
        Text("Smoothness: ${conn.smoothness}", style = MaterialTheme.typography.bodySmall)
        if (conn.notes.isNotBlank())
            Text(conn.notes, style = MaterialTheme.typography.bodySmall)
    }
}
