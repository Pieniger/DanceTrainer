package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.*
import kotlin.random.Random

@Composable
fun ConnectionFinderScreen(onBack: () -> Unit) {
    var move1 by remember { mutableStateOf<Move?>(null) }
    var move2 by remember { mutableStateOf<Move?>(null) }
    var smoothness by remember { mutableStateOf(3) }
    var prioritizeInfrequent by remember { mutableStateOf(false) }

    val sampleMoves = listOf(
        Move("1", "Step Left"),
        Move("2", "Step Right"),
        Move("3", "Spin"),
        Move("4", "Jump")
    )

    fun pickRandomPair() {
        move1 = sampleMoves.random()
        move2 = sampleMoves.filter { it.id != move1!!.id }.random()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Connection Finder", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Checkbox(checked = prioritizeInfrequent, onCheckedChange = { prioritizeInfrequent = it })
            Text("Prioritize infrequent moves")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { pickRandomPair() }) { Text("Find Pair") }

        Spacer(modifier = Modifier.height(16.dp))

        if (move1 != null && move2 != null) {
            Text("Move 1: ${'$'}{move1!!.name}")
            Text("Move 2: ${'$'}{move2!!.name}")
            Spacer(modifier = Modifier.height(8.dp))

            Text("Smoothness:")
            Row {
                (1..5).forEach { i ->
                    Button(onClick = { smoothness = i }) {
                        Text(if (i <= smoothness) "❤️" else "🤍")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { /* Save positive connection */ }) { Text("Yes") }
                Button(onClick = { /* Save negative connection */ }) { Text("No") }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
