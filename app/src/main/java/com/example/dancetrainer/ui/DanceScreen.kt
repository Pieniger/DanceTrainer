/**
 * Choose the next move from [from] using [connections].
 *
 * - Excludes:
 *   - [from] itself
 *   - any moves with a *negative* connection (works == false)
 * - If [priorityMode] is false → uniform random.
 * - If [priorityMode] is true  → weighted by smoothness (1..5).
 *
 * Returns Pair(nextMove, connectionNote).
 */
private fun pickNextMove(
    moves: List<Move>,
    connections: List<Connection>,
    from: Move,
    priorityMode: Boolean
): Pair<Move?, String?> {
    if (moves.size < 2) return null to null

    val positive = connections.filter { it.fromId == from.id && it.works }
    val negativeTargets = connections
        .filter { it.fromId == from.id && !it.works }
        .map { it.toId }
        .toSet()

    val candidates = moves.filter { m ->
        m.id != from.id && m.id !in negativeTargets
    }
    if (candidates.isEmpty()) return null to null

    // No priority mode → plain random
    if (!priorityMode) {
        val chosen = candidates.random()
        val conn = positive.firstOrNull { it.toId == chosen.id }
        return chosen to conn?.notes
    }

    // Priority mode: linear weighting by smoothness (1..5)
    val weighted = candidates.map { move ->
        val conn = positive.firstOrNull { it.toId == move.id }
        // Default 3 if no explicit smoothness, clamp to 1..5
        val smooth = (conn?.smoothness ?: 3).coerceIn(1, 5)
        move to smooth
    }

    val totalWeight = weighted.sumOf { it.second }
    if (totalWeight <= 0) {
        val chosen = candidates.random()
        val conn = positive.firstOrNull { it.toId == chosen.id }
        return chosen to conn?.notes
    }

    var r = Random.nextInt(totalWeight) // 0 until totalWeight
    for ((move, w) in weighted) {
        if (r < w) {
            val conn = positive.firstOrNull { it.toId == move.id }
            return move to conn?.notes
        }
        r -= w
    }

    // Fallback (shouldn’t normally happen)
    val last = weighted.last().first
    val conn = positive.firstOrNull { it.toId == last.id }
    return last to conn?.notes
}
