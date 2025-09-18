package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionFinderScreen(
    startMoveId: String?,
    onBack: () -> Unit
) {
    val startText = remember(startMoveId) {
        startMoveId?.let { "Starting from: $it" } ?: "Random start"
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Connection Finder") },
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
            Text(startText, style = MaterialTheme.typography.titleMedium)
            Text("This is the placeholder for the pairing UI.\n(Your existing logic can be plugged in here.)")
        }
    }
}
