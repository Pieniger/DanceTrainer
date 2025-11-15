package com.example.dancetrainer.ui

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMovesScreen(
    onBack: () -> Unit,
    onFindConnectionForMove: (String) -> Unit
) {
    val ctx = LocalContext.current

    var moves by remember { mutableStateOf<List<Move>>(emptyList()) }

    // Load moves when the screen is first shown
    LaunchedEffect(Unit) {
        moves = Storage.loadMoves(ctx)
    }

    var showAdd by remember { mutableStateOf(false) }
    var showEditFor by remember { mutableStateOf<Move?>(null) }

    androidx.compose.material3.Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Moves") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    Button(onClick = { showAdd = true }) { Text("Add Move") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (moves.isEmpty()) {
                Text("No moves yet. Tap 'Add Move' to create one.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(moves, key = { it.id }) { move ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    move.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (move.notes.isNotBlank()) {
                                    Text(
                                        move.notes,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(onClick = { showEditFor = move }) {
                                        Text("Edit")
                                    }
                                    Button(onClick = { onFindConnectionForMove(move.id) }) {
                                        Text("Find Connection")
                                    }
                                    TextButton(
                                        onClick = {
                                            // Delete move
                                            moves = moves.filter { it.id != move.id }
                                            Storage.saveMoves(ctx, moves)
                                        }
                                    ) {
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

    if (showAdd) {
        MoveDialog(
            title = "Add Move",
            initialName = "",
            initialNotes = "",
            onDismiss = { showAdd = false },
            onConfirm = { name, notes ->
                val id = name.lowercase()
                    .trim()
                    .replace("\\s+".toRegex(), "_")
                    .ifBlank { "move_${System.currentTimeMillis()}" }

                val newMove = Move(id = id, name = name.trim(), notes = notes.trim())
                moves = moves + newMove
                Storage.saveMoves(ctx, moves)
                showAdd = false
            }
        )
    }

    showEditFor?.let { editing ->
        MoveDialog(
            title = "Edit Move",
            initialName = editing.name,
            initialNotes = editing.notes,
            onDismiss = { showEditFor = null },
            onConfirm = { name, notes ->
                val updated = moves.map {
                    if (it.id == editing.id) it.copy(name = name.trim(), notes = notes.trim())
                    else it
                }
                moves = updated
                Storage.saveMoves(ctx, moves)
                showEditFor = null
            }
        )
    }
}

@Composable
private fun MoveDialog(
    title: String,
    initialName: String,
    initialNotes: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(initialName)) }
    var notes by remember { mutableStateOf(TextFieldValue(initialNotes)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val trimmedName = name.text.trim()
                if (trimmedName.isNotEmpty()) {
                    onConfirm(trimmedName, notes.text)
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
}
