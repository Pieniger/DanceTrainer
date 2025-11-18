package com.example.dancetrainer.data

/**
 * A single dance move.
 */
data class Move(
    val id: String,
    var name: String,
    var notes: String = ""
)

/**
 * works == true  → this transition is allowed and has a smoothness rating + optional notes
 * works == false → this transition is known NOT to work; smoothness is ignored
 *
 * smoothness is always in 1..5 (clamped when loading).
 */
data class Connection(
    val fromId: String,
    val toId: String,
    var works: Boolean,
    var smoothness: Int = 3,      // default mid-value in 1..5
    var notes: String = ""
)

/**
 * Placeholder for future use.
 */
data class Sequence(
    val id: String,
    val name: String,
    val moves: List<String> = emptyList()
)
