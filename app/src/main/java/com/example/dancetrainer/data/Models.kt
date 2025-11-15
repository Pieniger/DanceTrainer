package com.example.dancetrainer.data

import kotlinx.serialization.Serializable

@Serializable
data class Move(
    val id: String,
    var name: String,
    var notes: String = ""
)

@Serializable
data class Connection(
    val from: String,
    val to: String,
    var smoothness: Int
)

@Serializable
data class Sequence(
    val id: String,
    val name: String,
    val moves: List<String>,
    val difficulty: Int
)
