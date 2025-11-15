package com.example.dancetrainer.data

data class Move(
    val id: String,
    var name: String,
    var notes: String = ""
)

/**
 * works == true  → this transition is allowed and has a smoothness rating + optional notes
 * works == false → this transition is known NOT to work
 */
data class Connection(
    val fromId: String,
    val toId: String,
    var works: Boolean,
    var smoothness: Int = 0,      // only meaningful if works == true
    var notes: String = ""
)

// Placeholder for the future – not really used yet.
data class Sequence(
    val id: String,
    val name: String,
    val moves: List<String> = emptyList()
)
