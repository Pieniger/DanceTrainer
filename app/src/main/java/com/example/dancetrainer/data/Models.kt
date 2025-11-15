package com.example.dancetrainer.data

/**
 * Core data models for the app.
 *
 * We keep them simple POJOs so they are easy to persist
 * in our custom text-based Storage.
 */

data class Move(
    val id: String,
    var name: String,
    var note: String = ""
)

/**
 * A positive, rated connection from one move to another.
 *
 * - from: id of the starting move
 * - to: id of the following move
 * - smoothness: 1..10 (or similar scale)
 * - note: free-form description of why / how it works
 */
data class Connection(
    val from: String,
    val to: String,
    var smoothness: Int,
    var note: String = ""
)

/**
 * Sequences are placeholders for now; kept in case you
 * want to wire them up later.
 */
data class Sequence(
    val id: String,
    val name: String,
    val moves: List<String> = emptyList(),
    val difficulty: Int = 0,
    var note: String = ""
)
