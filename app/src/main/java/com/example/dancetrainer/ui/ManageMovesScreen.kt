package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Storage
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMovesScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var moves by remember { mutableStateOf(Storage.loadMoves(ctx)) }

    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newBeats by remember { mutableStateOf("4") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add move")
            }
        },
        topBar = {
            TopAppBar(title = { Text("Manage Moves") })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(moves, key = { it.id }) { move ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${move.name}  (${move.beats} beats)")
                            TextButton(onClick = {
                                // TODO: open Connection Finder seeded with this move
                            }) { Text("Find Connection") }
                        }
                    }
                }
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add move") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") })
                    OutlinedTextField(
                        value = newBeats,
                        onValueChange = { s -> newBeats = s.filter { it.isDigit() } },
                        label = { Text("Beats") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = newName.trim()
                    val beats = newBeats.toIntOrNull()?.coerceIn(1, 64) ?: 4
                    if (name.isNotEmpty()) {
                        val updated = moves.toMutableList().apply {
                            add(Move(id = UUID.randomUUID().toString(), name = name, beats = beats))
                        }
                        moves = updated
                        Storage.saveMoves(ctx, updated)
                    }
                    newName = ""; newBeats = "4"; showDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}
