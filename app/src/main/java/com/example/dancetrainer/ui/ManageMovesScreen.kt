package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Move

@Composable
fun ManageMovesScreen(onBack: () -> Unit) {
    var moves by remember { mutableStateOf(listOf(
        Move("1", "Step Left"),
        Move("2", "Step Right"),
        Move("3", "Spin"),
        Move("4", "Jump")
    )) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Manage Moves", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        moves.forEach { move ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(move.name)
                Button(onClick = { /* Navigate to ConnectionFinder with this as origin */ }) {
                    Text("Find Connection")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
