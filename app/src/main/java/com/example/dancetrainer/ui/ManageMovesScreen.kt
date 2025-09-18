package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

// Simple in-memory model (replace with your real repository later)
data class Move(val id: String, var name: String, var beats: Int = 4)

@Composable
fun ManageMovesScreen(
    onBack: () -> Unit,
    onFindConnectionForMove: (String) -> Unit
) {
    // In real app, load from storage/repo
    var moves by remember { mutableStateOf(sampleMoves().toMutableList()) }

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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(move.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Beats: ${move.beats}", style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { showEditFor = move }) {
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
            initialBeats = 4,
            onDismiss = { showAdd = false },
            onConfirm = { name, beats ->
                val new = Move(id = name.lowercase().replace("\\s+".toRegex(), "_"), name = name, beats = beats)
                moves = (moves + new).toMutableList()
                showAdd = false
            }
        )
    }

    showEditFor?.let { editing ->
        MoveDialog(
            title = "Edit Move",
            initialName = editing.name,
            initialBeats = editing.beats,
            onDismiss = { showEditFor = null },
            onConfirm = { name, beats ->
                editing.name = name
                editing.beats = beats
                moves = moves.toMutableList() // trigger recomposition
                showEditFor = null
            }
        )
    }
}

@Composable
private fun MoveDialog(
    title: String,
    initialName: String,
    initialBeats: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(initialName)) }
    var beatsText by remember { mutableStateOf(TextFieldValue(initialBeats.toString())) }
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
                    value = beatsText,
                    onValueChange = { beatsText = it },
                    label = { Text("Beats (int)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val beats = beatsText.text.toIntOrNull() ?: 4
                val trimmed = name.text.trim()
                if (trimmed.isNotEmpty()) onConfirm(trimmed, beats) else onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun sampleMoves(): List<Move> = listOf(
    Move("step_touch", "Step Touch", 4),
    Move("turn_left", "Turn Left", 4),
    Move("turn_right", "Turn Right", 4),
)
