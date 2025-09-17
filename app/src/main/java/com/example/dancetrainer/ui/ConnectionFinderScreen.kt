package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.Move
import kotlin.random.Random

@Composable
fun ConnectionFinderScreen(
    padding: PaddingValues,
    allMoves: List<Move>,
    startMoveId: String?
) {
    // Origin = user-specified (from Manage -> Find Connection) or random
    val origin = remember(startMoveId, allMoves) {
        startMoveId?.let { id -> allMoves.firstOrNull { it.id == id } }
            ?: allMoves.randomOrNull()
    }

    var candidate by remember(allMoves, origin) {
        mutableStateOf(allMoves.filter { it.id != origin?.id }.randomOrNull())
    }

    fun pickNewCandidate() {
        candidate = allMoves.filter { it.id != origin?.id }.ifEmpty { emptyList() }.randomOrNull()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Origin: ${origin?.name ?: "—"}")
        Text("Candidate: ${candidate?.name ?: "—"}")
        Divider()

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    // TODO: record positive connection origin -> candidate (+smoothness prompt)
                    pickNewCandidate()
                },
                enabled = origin != null && candidate != null
            ) { Text("Yes (Connect)") }

            Button(
                onClick = {
                    // TODO: record negative / not compatible
                    pickNewCandidate()
                },
                enabled = origin != null && candidate != null
            ) { Text("No / Skip") }

            Button(onClick = { pickNewCandidate() }) { Text("New Pair") }
        }

        if (origin == null || allMoves.size < 2) {
            Spacer(Modifier.height(8.dp))
            Text("Tip: add at least 2 moves, then use Find Connection on a specific move.")
        }
    }
}
