package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.Move

@Composable
fun ManageMovesScreen(
    padding: PaddingValues,
    moves: List<Move>,
    onAdd: (name: String, beats: Int) -> Unit,
    onEdit: (id: String, name: String, beats: Int) -> Unit,
    onDelete: (id: String) -> Unit,
    onFindConnection: (startMoveId: String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editing: Move? by remember { mutableStateOf(null) }

    Box(Modifier.fillMaxSize().padding(padding)) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            // Add button is at the top (visible and obvious)
            Button(
                onClick = { editing = null; showDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) { Text("Add Move") }

            Spacer(Modifier.height(12.dp))

            LazyColumn(Modifier.fillMaxSize()) {
                items(moves, key = { it.id }) { m ->
                    MoveRow(
                        move = m,
                        onEdit = { editing = m; showDialog = true },
                        onDelete = { onDelete(m.id) },
                        onFindConnection = { onFindConnection(m.id) }
                    )
                    Divider()
                }
            }
        }

        if (showDialog) {
            val isEdit = editing != null
            val initialName = editing?.name ?: ""
            val initialBeats = (editing?.beats ?: 4).toString()

            var name by remember(showDialog) { mutableStateOf(TextFieldValue(initialName)) }
            var beatsText by remember(showDialog) { mutableStateOf(TextFieldValue(initialBeats)) }

            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (isEdit) "Edit Move" else "Add Move") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = beatsText,
                            onValueChange = { beatsText = it },
                            label = { Text("Beats (integer)") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val n = name.text.trim()
                        val b = beatsText.text.toIntOrNull() ?: 4
                        if (n.isNotEmpty()) {
                            if (isEdit) {
                                onEdit(editing!!.id, n, b)
                            } else {
                                onAdd(n, b)
                            }
                            showDialog = false
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun MoveRow(
    move: Move,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFindConnection: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = move.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "Beats: ${move.beats}", style = MaterialTheme.typography.bodyMedium)
        }
        TextButton(onClick = onFindConnection) { Text("Find Connection") }
        TextButton(onClick = onEdit) { Text("Edit") }
        TextButton(onClick = onDelete) { Text("Delete") }
    }
}
