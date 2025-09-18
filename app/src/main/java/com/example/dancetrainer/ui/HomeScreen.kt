package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onManageMoves: () -> Unit,
    onConnectionFinder: () -> Unit,
    onSettings: () -> Unit,
    onDance: () -> Unit,
    onGraph: () -> Unit,
    onSequences: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Welcome!",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onManageMoves) { Text("Manage Moves") }
            Button(onClick = onConnectionFinder) { Text("Connection Finder") }
            Button(onClick = onDance) { Text("Dance") }
            Button(onClick = onSequences) { Text("Sequences") }
            Button(onClick = onGraph) { Text("Graph") }
            Button(onClick = onSettings) { Text("Settings") }
        }
    }
}
