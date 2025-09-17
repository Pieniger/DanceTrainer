package com.example.dancetrainer.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

object Storage {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private fun file(ctx: Context, name: String) = File(ctx.filesDir, name)

    fun saveMoves(context: Context, moves: List<Move>) {
        file(context, "moves.json").writeText(json.encodeToString(moves))
    }

    fun loadMoves(context: Context): List<Move> {
        val f = file(context, "moves.json")
        if (!f.exists()) return emptyList()
        return json.decodeFromString(f.readText())
    }

    fun saveConnections(context: Context, connections: List<Connection>) {
        file(context, "connections.json").writeText(json.encodeToString(connections))
    }

    fun loadConnections(context: Context): List<Connection> {
        val f = file(context, "connections.json")
        if (!f.exists()) return emptyList()
        return json.decodeFromString(f.readText())
    }

    fun saveSequences(context: Context, sequences: List<Sequence>) {
        file(context, "sequences.json").writeText(json.encodeToString(sequences))
    }

    fun loadSequences(context: Context): List<Sequence> {
        val f = file(context, "sequences.json")
        if (!f.exists()) return emptyList()
        return json.decodeFromString(f.readText())
    }
}
