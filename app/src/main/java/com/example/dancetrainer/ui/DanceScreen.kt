package com.example.dancetrainer.ui

import kotlin.random.Random
import com.example.dancetrainer.data.Move
import com.example.dancetrainer.data.Connection

/**
 * Choose the next move from [from] using [connections].
 *
 * Rules:
 * - ONLY considers connections where:
 *   - fromId == from.id
 *   - works == true
 * - If priorityMode == false → uniform random among valid connections
 * - If priorityMode == true  → weighted random by smoothness (1..5, linear)
 *
 * Returns Pair(nextMove, connectionNote)
 */
private fun pickNextMove(
    moves: List<Move>,
    connections: List<Connection>,
    from: Move,
    priorityMode: Boolean
): Pair<Move?, String?> {

    // All valid outgoing connections
    val positiveConnections = connections.filter {
        it.fromId == from.id && it.works
    }

    if (positiveConnections.isEmpty()) {
        return null to null
    }

    // Resolve connections → actual moves
    val resolved = positiveConnections.mapNotNull { conn ->
        val move = moves.firstOrNull { it.id == conn.toId }
        move?.let { it to conn }
    }

    if (resolved.isEmpty()) {
        return null to null
    }

    // No priority mode → uniform random
    if (!priorityMode) {
        val (move, conn) = resolved.random()
        return move to conn.notes
    }

    // Priority mode → linear weighting by smoothness (1..5)
    val weighted = resolved.map { (move, conn) ->
        val smoothness = conn.smoothness.coerceIn(1, 5)
        Triple(move, conn, smoothness)
    }

    val totalWeight = weighted.sumOf { it.third }
    if (totalWeight <= 0) {
        val (move, conn) = resolved.random()
        return move to conn.notes
    }

    var r = Random.nextInt(totalWeight)
    for ((move, conn, weight) in weighted) {
        if (r < weight) {
            return move to conn.notes
        }
        r -= weight
    }

    // Fallback (should never happen)
    val last = weighted.last()
    return last.first to last.second.notes
}
