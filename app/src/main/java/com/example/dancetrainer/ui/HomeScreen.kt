package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement

@Composable
fun HomeScreen(
    padding: PaddingValues,
    onManageMoves: () -> Unit,
    onConnectionFinder: () -> Unit,
    onSettings: () -> Unit,
    onDance: () -> Unit,
    onGraph: () -> Unit,
    onSequences: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onManageMoves) { Text("Manage Moves") }
        Button(onClick = onConnectionFinder) { Text("Connection Finder") }
        Button(onClick = onDance) { Text("Dance") }
        Button(onClick = onSequences) { Text("Sequences") }
        Button(onClick = onGraph) { Text("Graph") }
        Button(onClick = onSettings) { Text("Settings") }
    }
}
