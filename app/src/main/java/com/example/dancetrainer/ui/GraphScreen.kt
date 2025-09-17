package com.example.dancetrainer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.*

@Composable
fun GraphScreen(onBack: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    var highlighted by remember { mutableStateOf<String?>(null) }
    var tappedSequence by remember { mutableStateOf(listOf<String>()) }

    val moves = listOf(
        Move("1", "Step Left"),
        Move("2", "Step Right"),
        Move("3", "Spin"),
        Move("4", "Jump")
    )
    val connections = listOf(
        Connection("1", "2", 3),
        Connection("2", "3", 5),
        Connection("3", "4", 2)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale *= zoom
                    offset += pan
                }
            }
        ) {
            val spacing = 200f
            val positions = moves.mapIndexed { i, move ->
                move.id to Offset(i * spacing * scale + offset.x + 200, size.height / 2 + offset.y)
            }.toMap()

            // Draw connections
            connections.forEach { conn ->
                val from = positions[conn.from]
                val to = positions[conn.to]
                if (from != null && to != null) {
                    drawLine(
                        color = Color.White,
                        start = from,
                        end = to,
                        strokeWidth = (conn.smoothness * 2).toFloat()
                    )
                }
            }

            // Draw nodes
            moves.forEach { move ->
                val pos = positions[move.id] ?: return@forEach
                drawIntoCanvas { canvas ->
                    val paint = androidx.compose.ui.graphics.Paint().apply {
                        color = if (highlighted == move.id) Color.Yellow else Color.Cyan
                    }
                    canvas.drawCircle(pos, 40f, paint)
                }
            }
        }

        Text("Tapped Sequence: ${'$'}tappedSequence")

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { if (tappedSequence.isNotEmpty()) tappedSequence = tappedSequence.dropLast(1) }) {
                Text("Back")
            }
            Button(onClick = { tappedSequence = emptyList() }) { Text("Clear") }
            Button(onClick = { /* Save sequence to storage */ }) { Text("Save") }
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(8.dp)) { Text("Back to Menu") }
    }
}
