package com.example.dancetrainer.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

/**
 * Minimal storage layer.
 *
 * For now:
 * - save/load methods are stubs (no-op or empty).
 * - style selection + folder structure are real:
 *      base folder (internal or SAF) / styles / <StyleName> / ...
 */
object Storage {

    // ---------- STYLE FOLDER HELPERS ----------

    /** Internal base for all styles: <filesDir>/styles */
    private fun internalStylesBase(ctx: Context): File {
        val base = File(ctx.filesDir, "styles")
        if (!base.exists()) base.mkdirs()
        return base
    }

    /**
     * External base (via SAF tree URI), under a "styles" directory:
     * <tree-root>/styles
     */
    private fun externalStylesBase(ctx: Context): DocumentFile? {
        val uriStr = Prefs.getTreeUri(ctx) ?: return null
        val tree = DocumentFile.fromTreeUri(ctx, Uri.parse(uriStr)) ?: return null
        return tree.findFile("styles") ?: tree.createDirectory("styles")
    }

    /**
     * List all style names (directory names) under the current base (internal or external).
     */
    fun listStyles(context: Context): List<String> {
        val external = externalStylesBase(context)
        return if (external != null) {
            external
                .listFiles()
                .filter { it.isDirectory }
                .mapNotNull { it.name }
                .sorted()
        } else {
            val base = internalStylesBase(context)
            base.list()?.sorted()?.toList() ?: emptyList()
        }
    }

    /**
     * Ensure a style folder exists under the current base.
     */
    fun createStyle(context: Context, name: String) {
        val safe = name.trim().ifEmpty { "Unnamed" }
        val external = externalStylesBase(context)
        if (external != null) {
            external.findFile(safe) ?: external.createDirectory(safe)
        } else {
            File(internalStylesBase(context), safe).mkdirs()
        }
    }

    // ---------- STUBS FOR MOVES / CONNECTIONS / SEQUENCES ----------

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
}
