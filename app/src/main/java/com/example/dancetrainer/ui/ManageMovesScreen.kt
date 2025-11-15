package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    var moves by remember {
        mutableStateOf(Storage.loadMoves(ctx).toMutableList())
    }

    fun persist() {
        Storage.saveMoves(ctx, moves)
    }

    var showAdd by remember { mutableStateOf(false) }
    var showEditFor by remember { mutableStateOf<Move?>(null) }

    Scaffold(
        topBar = {
            SmallTopAppBar(
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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(moves, key = { it.id }) { move ->
                        ElevatedCard {
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
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { showEditFor = move }) {
                                            Text("Edit")
                                        }
                                        OutlinedButton(onClick = {
                                            moves = moves.filterNot { it.id == move.id }.toMutableList()
                                            persist()
                                        }) {
                                            Text("Delete")
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

    if (showAdd) {
        MoveDialog(
            title = "Add Move",
            initialName = "",
            initialNotes = "",
            onDismiss = { showAdd = false },
            onConfirm = { name, notes ->
                val baseId = name.lowercase().replace("\\s+".toRegex(), "_")
                val uniqueId = generateUniqueId(baseId, moves)
                val newMove = Move(
                    id = uniqueId,
                    name = name,
                    notes = notes
                )
                moves = (moves + newMove).toMutableList()
                persist()
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
                editing.name = name
                editing.notes = notes
                moves = moves.toMutableList() // trigger recomposition
                persist()
                showEditFor = null
            }
        )
    }
}

private fun generateUniqueId(base: String, moves: List<Move>): String {
    if (moves.none { it.id == base }) return base
    var i = 1
    while (moves.any { it.id == "${base}_$i" }) i++
    return "${base}_$i"
}

@OptIn(ExperimentalMaterial3Api::class)
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
                    label = { Text("Name") }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val trimmed = name.text.trim()
                if (trimmed.isNotEmpty()) {
                    onConfirm(trimmed, notes.text.trim())
                } else {
                    onDismiss()
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
