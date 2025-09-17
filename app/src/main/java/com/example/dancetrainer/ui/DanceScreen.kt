package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.logic.DanceConductor

@Composable
fun DanceScreen(onBack: () -> Unit) {
    var mode by remember { mutableStateOf("single") }
    val conductor = remember { DanceConductor() }
    var sequence by remember { mutableStateOf<List<Move>>(emptyList()) }
    var current by remember { mutableStateOf<Move?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dance Mode", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = {
                mode = "single"
                conductor.reset()
                current = conductor.nextSingleMove()
            }) { Text("One Move") }

            Button(onClick = {
                mode = "random"
                conductor.generateRandomSequence()
                sequence = conductor.currentSequence
            }) { Text("Random Seq") }

            Button(onClick = {
                mode = "stored"
                conductor.pickStoredSequence()
                sequence = conductor.currentSequence
            }) { Text("Stored Seq") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (mode) {
            "single" -> {
                val name = current?.name ?: "None"
                Text("Next Move: $name")
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    current = conductor.nextSingleMove()
                }) { Text("Next") }
            }
            "random" -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Random Sequence:")
                    sequence.forEach { Text(it.name) }
                    OutlinedButton(onClick = {
                        conductor.generateRandomSequence()
                        sequence = conductor.currentSequence
                    }) { Text("Reload Sequence") }
                }
            }
            "stored" -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Stored Sequence:")
                    sequence.forEach { Text(it.name) }
                    OutlinedButton(onClick = {
                        conductor.pickStoredSequence()
                        sequence = conductor.currentSequence
                    }) { Text("Reload Stored Sequence") }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
