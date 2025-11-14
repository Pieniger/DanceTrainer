package com.example.dancetrainer.data

import android.content.Context

/**
 * Minimal storage stub so the project compiles and runs.
 * All functions are safe no-ops or return empty/default data.
 *
 * Later, you can replace these with real JSON file storage.
 */
object Storage {

    fun saveMoves(context: Context, moves: List<Move>) {
        // TODO: implement real persistence if needed
    }

    fun loadMoves(context: Context): List<Move> {
        // TODO: load from disk; for now, return empty list
        return emptyList()
    }

    fun saveConnections(context: Context, connections: List<Connection>) {
        // TODO: implement real persistence
    }

    fun loadConnections(context: Context): List<Connection> {
        // TODO: load from disk; for now, return empty list
        return emptyList()
    }

    fun saveSequences(context: Context, sequences: List<Sequence>) {
        // TODO: implement real persistence
    }

    fun loadSequences(context: Context): List<Sequence> {
        // TODO: load from disk; for now, return empty list
        return emptyList()
    }

    /**
     * Just returns a single default style name for now.
     */
    fun listStyles(context: Context): List<String> = listOf("Default")

    fun createStyle(context: Context, name: String) {
        // TODO: implement real style-folder creation if desired
    }
}
