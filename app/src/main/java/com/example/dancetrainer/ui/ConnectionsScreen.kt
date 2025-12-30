package com.example.dancetrainer.ui

import android.content.Context
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

    var editingConnection by remember { mutableStateOf<Connection?>(null) }
    var editSmoothnessText by remember { mutableStateOf(TextFieldValue("")) }
    var editNotesText by remember { mutableStateOf(TextFieldValue("")) }

    fun saveConnections(ctx: Context, updated: List<Connection>) {
        Storage.saveConnections(ctx, updated)
        connections = updated
    }

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
                .padding(16.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // LEFT — Moves
            Card(
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                LazyColumn(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(moves, key = { it.id }) { move ->
                        Text(
                            text = move.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMove = move }
                                .padding(8.dp),
                            style = if (move.id == selectedMove?.id)
                                MaterialTheme.typography.titleMedium
                            else
                                MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // RIGHT — Connections
            Card(
                modifier = Modifier.weight(2f).fillMaxHeight()
            ) {
                if (selectedMove == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Select a move to see connections")
                    }
                } else {
                    val move = selectedMove!!

                    // ✅ ONLY POSITIVE CONNECTIONS
                    val outgoing = connections.filter {
                        it.works && it.fromId == move.id
                    }
                    val incoming = connections.filter {
                        it.works && it.toId == move.id
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            "Connections for ${move.name}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text("Leads to:", style = MaterialTheme.typography.titleSmall)
                        if (outgoing.isEmpty()) {
                            Text("No outgoing connections", style = MaterialTheme.typography.bodySmall)
                        } else {
                            outgoing.forEach { conn ->
                                val target = movesById[conn.toId]
                                ConnectionRow(
                                    label = "→ ${target?.name ?: conn.toId}",
                                    smoothness = conn.smoothness.coerceIn(1, 5),
                                    notes = conn.notes,
                                    onClick = {
                                        editingConnection = conn
                                        editSmoothnessText =
                                            TextFieldValue(conn.smoothness.toString())
                                        editNotesText =
                                            TextFieldValue(conn.notes)
                                    }
                                )
                            }
                        }

                        Divider()

                        Text("Comes from:", style = MaterialTheme.typography.titleSmall)
                        if (incoming.isEmpty()) {
                            Text("No incoming connections", style = MaterialTheme.typography.bodySmall)
                        } else {
                            incoming.forEach { conn ->
                                val source = movesById[conn.fromId]
                                ConnectionRow(
                                    label = "← ${source?.name ?: conn.fromId}",
                                    smoothness = conn.smoothness.coerceIn(1, 5),
                                    notes = conn.notes,
                                    onClick = {
                                        editingConnection = conn
                                        editSmoothnessText =
                                            TextFieldValue(conn.smoothness.toString())
                                        editNotesText =
                                            TextFieldValue(conn.notes)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // EDIT DIALOG
    editingConnection?.let { conn ->

        AlertDialog(
            onDismissRequest = { editingConnection = null },
            title = {
                val from = movesById[conn.fromId]?.name ?: conn.fromId
                val to = movesById[conn.toId]?.name ?: conn.toId
                Text("$from → $to")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editSmoothnessText,
                        onValueChange = { editSmoothnessText = it },
                        label = { Text("Smoothness (1–5)") }
                    )
                    OutlinedTextField(
                        value = editNotesText,
                        onValueChange = { editNotesText = it },
                        label = { Text("Note") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val smooth = editSmoothnessText.text
                        .toIntOrNull()
                        ?.coerceIn(1, 5)
                        ?: conn.smoothness

                    val updated = connections.map {
                        if (it === conn) it.copy(
                            smoothness = smooth,
                            notes = editNotesText.text
                        ) else it
                    }

                    saveConnections(ctx, updated)
                    editingConnection = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingConnection = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ConnectionRow(
    label: String,
    smoothness: Int,
    notes: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("Smoothness: $smoothness", style = MaterialTheme.typography.bodySmall)
        if (notes.isNotBlank()) {
            Text(notes, style = MaterialTheme.typography.bodySmall)
        }
    }
}
