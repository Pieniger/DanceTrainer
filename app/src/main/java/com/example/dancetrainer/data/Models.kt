package com.example.dancetrainer.data

// Simple data models without kotlinx.serialization.
// You can reintroduce serialization later if desired.

data class Move(
    val id: String,
    var name: String,
    var beats: Int = 4
)

data class Connection(
    val from: String,
    val to: String,
    var smoothness: Int
)

/**
 * A saved sequence of moves.
 */
data class Sequence(
    val id: String,
    val name: String,
    val moves: List<String>,
    val difficulty: Int
)
