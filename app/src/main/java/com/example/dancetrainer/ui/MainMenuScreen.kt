package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuScreen(
    onManageMoves: () -> Unit,
    onDance: () -> Unit,
    onSettings: () -> Unit,
    onSequences: () -> Unit,
    onGraph: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("DanceTrainer", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onManageMoves, modifier = Modifier.fillMaxWidth()) { Text("Manage Moves") }
        Button(onClick = onDance, modifier = Modifier.fillMaxWidth()) { Text("Dance") }
        Button(onClick = onSequences, modifier = Modifier.fillMaxWidth()) { Text("Sequences") }
        Button(onClick = onGraph, modifier = Modifier.fillMaxWidth()) { Text("Graph") }
        Button(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("Settings") }
    }
}
