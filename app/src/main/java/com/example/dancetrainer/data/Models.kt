package com.example.dancetrainer.data

/**
 * A dance move belonging to a given style.
 * Notes can hold any extra explanation (e.g. lead, follower hints, etc.).
 */
data class Move(
    val id: String,
    var name: String,
    var notes: String = ""
)

/**
 * Result of a connection evaluation between two moves.
 */
enum class ConnectionResult {
    WORKS,
    DOESNT_WORK
}

/**
 * A connection from one move to another.
 *
 * - result = WORKS / DOESNT_WORK (from ConnectionFinder)
 * - smoothness = optional numeric "how nice it feels" (only meaningful for WORKS)
 * - notes = any extra comments about the transition
 */
data class Connection(
    val fromId: String,
    val toId: String,
    val result: ConnectionResult,
    val smoothness: Int? = null,
    val notes: String = ""
)

/**
 * Placeholder for future use. Not used by Dance yet, but wired for storage.
 */
data class Sequence(
    val id: String,
    var name: String,
    val moves: List<String>,
    val notes: String = ""
)
