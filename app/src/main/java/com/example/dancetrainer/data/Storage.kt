package com.example.dancetrainer.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File

object Storage {
    private val json = Json { prettyPrint = true }

    fun saveMoves(context: Context, moves: List<Move>) {
        val file = File(context.filesDir, "moves.json")
        file.writeText(json.encodeToString(ListSerializer(Move.serializer()), moves))
    }

    fun loadMoves(context: Context): List<Move> {
        val file = File(context.filesDir, "moves.json")
        if (!file.exists()) return emptyList()
        return json.decodeFromString(ListSerializer(Move.serializer()), file.readText())
    }

    fun saveConnections(context: Context, connections: List<Connection>) {
        val file = File(context.filesDir, "connections.json")
        file.writeText(json.encodeToString(ListSerializer(Connection.serializer()), connections))
    }

    fun loadConnections(context: Context): List<Connection> {
        val file = File(context.filesDir, "connections.json")
        if (!file.exists()) return emptyList()
        return json.decodeFromString(ListSerializer(Connection.serializer()), file.readText())
    }

    fun saveSequences(context: Context, sequences: List<Sequence>) {
        val file = File(context.filesDir, "sequences.json")
        file.writeText(json.encodeToString(ListSerializer(Sequence.serializer()), sequences))
    }

    fun loadSequences(context: Context): List<Sequence> {
        val file = File(context.filesDir, "sequences.json")
        if (!file.exists()) return emptyList()
        return json.decodeFromString(ListSerializer(Sequence.serializer()), file.readText())
    }
}
