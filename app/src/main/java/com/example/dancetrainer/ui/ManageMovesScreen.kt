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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMovesScreen(
    onBack: () -> Unit,
    onFindConnectionForMove: (String) -> Unit
) {
    val ctx = LocalContext.current

    var moves by remember { mutableStateOf<List<Move>>(emptyList()) }
    var showAdd by remember { mutableStateOf(false) }
    var editingMove by remember { mutableStateOf<Move?>(null) }

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
                    Button(onClick = { showAdd = true }) {
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
                Text("No moves yet. Tap 'Add Move' to create one.")
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
                                Text(move.name, style = MaterialTheme.typography.titleMedium)
                                if (move.notes.isNotBlank()) {
                                    Text(
                                        "Notes: ${move.notes}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(onClick = { editingMove = move }) {
                                        Text("Edit")
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
                val id = name.lowercase().replace("\\s+".toRegex(), "_")
                val newMove = Move(id = id, name = name, notes = notes)
                persist(moves + newMove)
                showAdd = false
            }
        )
    }

    editingMove?.let { move ->
        MoveDialog(
            title = "Edit Move",
            initialName = move.name,
            initialNotes = move.notes,
            onDismiss = { editingMove = null },
            onConfirm = { name, notes ->
                val updated = moves.map {
                    if (it.id == move.id) it.copy(name = name, notes = notes) else it
                }
                persist(updated)
                editingMove = null
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
                val trimmed = name.text.trim()
                if (trimmed.isNotEmpty()) {
                    onConfirm(trimmed, notes.text.trim())
                }
                onDismiss()
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
