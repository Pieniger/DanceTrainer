package com.example.dancetrainer.ui

import android.view.KeyEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.audio.MoveAnnouncer
import com.example.dancetrainer.data.Connection
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Prefs
import com.example.dancetrainer.data.Storage
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanceScreen(onBack: () -> Unit) {

    val ctx = LocalContext.current

    val moves = remember { Storage.loadMoves(ctx) }
    val connections = remember { Storage.loadConnections(ctx) }

    var currentMove by remember { mutableStateOf<Move?>(null) }
    var lastConnectionNote by remember { mutableStateOf<String?>(null) }

    val announcer = remember { MoveAnnouncer(ctx) }

    DisposableEffect(Unit) {
        onDispose { announcer.shutdown() }
    }

    fun advance(reroll: Boolean = false) {
        val from = if (reroll) null else currentMove
        val (next, note) =
            if (from == null) {
                moves.randomOrNull() to null
            } else {
                pickNextMove(
                    moves = moves,
                    connections = connections,
                    from = from
                )
            }

        if (next != null) {
            currentMove = next
            lastConnectionNote = note
            announcer.announce(next.name, bpm = 100)
        }
    }

    // Start automatically
    LaunchedEffect(Unit) {
        if (currentMove == null) advance()
    }

    // Bluetooth key handling
    val nextKey = Prefs.getNextMoveKeyCode(ctx)
    val rerollKey = Prefs.getRerollKeyCode(ctx)

    Scaffold(
        topBar = {
            TopAppBar(
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
                .padding(24.dp)
                .fillMaxSize()
                .onKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false

                    when (event.nativeKeyEvent.keyCode) {
                        nextKey -> {
                            advance()
                            true
                        }
                        rerollKey -> {
                            advance(reroll = true)
                            true
                        }
                        else -> false
                    }
                },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = currentMove?.name ?: "—",
                style = MaterialTheme.typography.headlineMedium
            )

            lastConnectionNote?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { advance() }) {
                    Text("Next")
                }
                Button(onClick = { advance(reroll = true) }) {
                    Text("Reroll")
                }
            }
        }
    }
}

/**
 * Priority-only weighted selection.
 * Smoothness scale: 1–5 (linear).
 * Excludes works == false.
 */
private fun pickNextMove(
    moves: List<Move>,
    connections: List<Connection>,
    from: Move
): Pair<Move?, String?> {

    val positive = connections.filter {
        it.fromId == from.id && it.works
    }

    val blocked = connections
        .filter { it.fromId == from.id && !it.works }
        .map { it.toId }
        .toSet()

    val candidates = moves.filter {
        it.id != from.id && it.id !in blocked
    }

    if (candidates.isEmpty()) return null to null

    val weighted = candidates.map { move ->
        val conn = positive.firstOrNull { it.toId == move.id }
        val weight = (conn?.smoothness ?: 3).coerceIn(1, 5)
        move to weight
    }

    val total = weighted.sumOf { it.second }
    var r = Random.nextInt(total)

    for ((move, w) in weighted) {
        if (r < w) {
            val note = positive.firstOrNull { it.toId == move.id }?.notes
            return move to note
        }
        r -= w
    }

    val fallback = weighted.last().first
    val note = positive.firstOrNull { it.toId == fallback.id }?.notes
    return fallback to note
}
