package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DanceScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Dance") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Choose a mode and start dancing.",
                style = MaterialTheme.typography.titleMedium)
            // TODO: Hook up your real modes (One-at-a-time, Random Sequence, Sequence Loop)
            ElevatedCard(Modifier.fillMaxSize()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Placeholder dance area")
                    Button(onClick = { /* regenerate random sequence */ }) {
                        Text("Reload sequence")
                    }
                }
            }
        }
    }
}
