package com.example.dancetrainer.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Connection
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Storage

@Composable
fun ConnectionsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    // Load current data
    var moves by remember { mutableStateOf(Storage.loadMoves(ctx)) }
    var connections by remember { mutableStateOf(Storage.loadConnections(ctx)) }

    // Map of moveId -> Move
    val movesById = remember(moves) { moves.associateBy { it.id } }

    var selectedMove by remember { mutableStateOf<Move?>(null) }

    // For editing a connection
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
            // LEFT: moves list
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Text(
                        "Moves",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(moves, key = { it.id }) { move ->
                            val selected = move.id == selectedMove?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMove = move }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        move.name,
                                        style = if (selected)
                                            MaterialTheme.typography.titleMedium
                                        else
                                            MaterialTheme.typography.bodyMedium
                                    )
                                    if (move.notes.isNotBlank()) {
                                        Text(
                                            move.notes,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // RIGHT: connections of selected move
            Card(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            ) {
                if (selectedMove == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Select a move on the left to see its connections.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    val move = selectedMove!!
                    // We treat all connections as "positive" here
                    val outgoing = connections.filter { it.fromId == move.id }
                    val incoming = connections.filter { it.toId == move.id }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Connections for: ${move.name}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        // OUTGOING
                        Text(
                            "Leads to:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (outgoing.isEmpty()) {
                            Text(
                                "No outgoing connections yet.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                outgoing.forEach { conn ->
                                    val target = movesById[conn.toId]
                                    ConnectionRow(
                                        label = "→ ${target?.name ?: conn.toId}",
                                        smoothness = conn.smoothness,
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

                        Divider(Modifier.padding(vertical = 8.dp))

                        // INCOMING
                        Text(
                            "Comes from:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (incoming.isEmpty()) {
                            Text(
                                "No incoming connections yet.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                incoming.forEach { conn ->
                                    val source = movesById[conn.fromId]
                                    ConnectionRow(
                                        label = "← ${source?.name ?: conn.fromId}",
                                        smoothness = conn.smoothness,
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
    }

    // EDIT CONNECTION DIALOG
    val connToEdit = editingConnection
    if (connToEdit != null) {
        val ctxLocal = ctx
        val movesByIdLocal = movesById

        AlertDialog(
            onDismissRequest = { editingConnection = null },
            title = {
                val fromName = movesByIdLocal[connToEdit.fromId]?.name ?: connToEdit.fromId
                val toName = movesByIdLocal[connToEdit.toId]?.name ?: connToEdit.toId
                Text("Edit connection: $fromName → $toName")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editSmoothnessText,
                        onValueChange = { editSmoothnessText = it },
                        label = { Text("Smoothness (1–10)") }
                    )
                    OutlinedTextField(
                        value = editNotesText,
                        onValueChange = { editNotesText = it },
                        label = { Text("Note") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val smooth = editSmoothnessText.text.toIntOrNull()
                            ?.coerceIn(1, 10) ?: connToEdit.smoothness

                        val updatedList = connections.map { existing ->
                            if (existing === connToEdit) {
                                existing.copy(
                                    smoothness = smooth,
                                    notes = editNotesText.text
                                )
                            } else existing
                        }

                        saveConnections(ctxLocal, updatedList)
                        editingConnection = null
                    }
                ) {
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
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("Smoothness: $smoothness", style = MaterialTheme.typography.bodySmall)
        if (notes.isNotBlank()) {
            Text(notes, style = MaterialTheme.typography.bodySmall)
        }
    }
}
