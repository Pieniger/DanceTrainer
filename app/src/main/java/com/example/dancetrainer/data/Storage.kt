package com.example.dancetrainer.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

/**
 * Storage layer focused on:
 * - Remembering the selected base folder (via Prefs.getTreeUri / setTreeUri)
 * - Listing "styles" as subfolders of that base folder
 *
 * NOTE:
 *  - We no longer manage style folders from inside the app.
 *  - You create / rename / delete style folders manually (e.g. in a file manager).
 *  - If no external folder is selected, we fall back to internal storage and/or "Default".
 */
object Storage {

    // ---------- BASE FOLDER HELPERS ----------

    /**
     * Internal base folder used when no tree URI is selected.
     * You can still manually create subfolders here if you want.
     */
    private fun internalBase(ctx: Context): File {
        val base = File(ctx.filesDir, "styles")
        if (!base.exists()) base.mkdirs()
        return base
    }

    /**
     * External base folder from SAF tree URI (if selected).
     * We do NOT force a "styles" subfolder anymore; we treat
     * all immediate subfolders of this base as styles.
     */
    private fun externalBase(ctx: Context): DocumentFile? {
        val uriStr = Prefs.getTreeUri(ctx) ?: return null
        return DocumentFile.fromTreeUri(ctx, Uri.parse(uriStr))
    }

    /**
     * List style names:
     * - If a SAF folder is selected: all immediate subfolders of that folder.
     * - Else: all subfolders under internalBase().
     * - If nothing is found: return ["Default"].
     */
    fun listStyles(context: Context): List<String> {
        // 1) Try external (user-chosen folder)
        externalBase(context)?.let { base ->
            val names = base.listFiles()
                .filter { it.isDirectory }
                .mapNotNull { it.name }
                .sorted()
            if (names.isNotEmpty()) return names
        }

        // 2) Fall back to internal base
        val internal = internalBase(context)
        val internalNames = internal.listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { it.name }
            ?.sorted()
        if (!internalNames.isNullOrEmpty()) return internalNames

        // 3) As a final fallback, just expose a single "Default" style
        return listOf("Default")
    }

    // ---------- STUBS FOR MOVES / CONNECTIONS / SEQUENCES ----------
    // (You can wire these up to real JSON files per-style later.)

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
