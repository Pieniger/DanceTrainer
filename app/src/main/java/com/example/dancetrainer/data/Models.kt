package com.example.dancetrainer.data

/**
 * Core domain models.
 *
 * We keep them as plain data classes (no kotlinx.serialization) and
 * handle persistence manually in Storage.kt.
 */

data class Move(
    val id: String,
    var name: String,
    var notes: String = ""
)

data class Connection(
    val from: String,
    val to: String,
    var smoothness: Int
)

data class Sequence(
    val id: String,
    val name: String,
    val moves: List<String>,
    val difficulty: Int
)
