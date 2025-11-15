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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Storage
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMovesScreen(
    onBack: () -> Unit,
    onFindConnectionForMove: (String) -> Unit
) {
    val ctx = LocalContext.current

    var moves by remember { mutableStateOf(listOf<Move>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMove by remember { mutableStateOf<Move?>(null) }

    // Load moves on first composition
    LaunchedEffect(Unit) {
        moves = Storage.loadMoves(ctx)
    }

    fun persist(newList: List<Move>) {
        moves = newList
        Storage.saveMoves(ctx, newList)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Moves") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Move")
                    }
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
                Text(
                    "No moves yet for this style.\nTap \"Add Move\" to create your first one.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                if (move.note.isNotBlank()) {
                                    Text(
                                        move.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(onClick = { editingMove = move }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                                        }
                                        IconButton(onClick = {
                                            val remaining = moves.filterNot { it.id == move.id }
                                            persist(remaining)
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                                        }
                                    }

                                    Button(onClick = { onFindConnectionForMove(move.id) }) {
                                        Text("Find Connection")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        MoveDialog(
            title = "Add Move",
            initialName = "",
            initialNote = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, note ->
                val id = generateMoveId(name, moves)
                val newMove = Move(id = id, name = name, note = note)
                persist(moves + newMove)
                showAddDialog = false
            }
        )
    }

    // Edit dialog
    editingMove?.let { move ->
        MoveDialog(
            title = "Edit Move",
            initialName = move.name,
            initialNote = move.note,
            onDismiss = { editingMove = null },
            onConfirm = { name, note ->
                val updatedList = moves.map {
                    if (it.id == move.id) it.copy(name = name, note = note) else it
                }
                persist(updatedList)
                editingMove = null
            }
        )
    }
}

@Composable
private fun MoveDialog(
    title: String,
    initialName: String,
    initialNote: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(initialName)) }
    var note by remember { mutableStateOf(TextFieldValue(initialNote)) }

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
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val trimmedName = name.text.trim()
                val trimmedNote = note.text.trim()
                if (trimmedName.isNotEmpty()) {
                    onConfirm(trimmedName, trimmedNote)
                } else {
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun generateMoveId(name: String, existing: List<Move>): String {
    val base = name
        .lowercase()
        .trim()
        .replace("\\s+".toRegex(), "_")
        .replace("[^a-z0-9_]+".toRegex(), "")

    if (existing.none { it.id == base }) return base
    var i = 2
    while (existing.any { it.id == "${base}_$i" }) {
        i++
    }
    return "${base}_$i"
}
