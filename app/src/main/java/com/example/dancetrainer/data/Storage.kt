package com.example.dancetrainer.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object Storage {

    // Shared JSON configuration for all persisted data
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // -----------------------------
    // Base folders (internal / external)
    // -----------------------------

    /**
     * Internal base folder used when the user has NOT picked a SAF folder.
     * Under this we create one subfolder per dance style.
     *
     * Example:
     *   <internal>/DanceTrainer/Waltz/moves.json
     *   <internal>/DanceTrainer/Tango/moves.json
     */
    private fun internalBaseDir(ctx: Context): File {
        val dir = File(ctx.filesDir, "DanceTrainer")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * External base folder (SAF tree) when the user has chosen a folder
     * in Settings. Each *subfolder* is treated as a style.
     *
     * Example:
     *   <chosenFolder>/Waltz/moves.json
     *   <chosenFolder>/Tango/moves.json
     */
    private fun externalBaseDir(ctx: Context): DocumentFile? {
        val uriStr = Prefs.getTreeUri(ctx) ?: return null
        return DocumentFile.fromTreeUri(ctx, Uri.parse(uriStr))
    }

    // -----------------------------
    // Style-specific folders
    // -----------------------------

    /**
     * Internal folder for the currently selected style.
     * Returns null if no style is selected.
     */
    private fun styleInternalDir(ctx: Context): File? {
        val style = Prefs.getStyle(ctx) ?: return null
        val dir = File(internalBaseDir(ctx), style)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * External folder for the currently selected style.
     * Creates the directory if it doesn’t exist.
     * Returns null if no style is selected or something goes wrong.
     */
    private fun styleExternalDir(ctx: Context): DocumentFile? {
        val style = Prefs.getStyle(ctx) ?: return null
        val base = externalBaseDir(ctx) ?: return null

        val existing = base.findFile(style)
        val dir = when {
            existing == null -> base.createDirectory(style)
            existing.isDirectory -> existing
            else -> null
        }
        return dir
    }

    // -----------------------------
    // Low-level read/write helpers
    // -----------------------------

    private fun writeTextInternal(file: File, text: String) {
        file.parentFile?.mkdirs()
        file.writeText(text)
    }

    private fun readTextInternal(file: File): String? =
        if (file.exists()) file.readText() else null

    private fun writeTextExternal(ctx: Context, dir: DocumentFile, name: String, text: String) {
        val existing = dir.findFile(name)
        val fileDoc = existing ?: dir.createFile("application/json", name) ?: return
        ctx.contentResolver.openOutputStream(fileDoc.uri, "rwt")?.use { out ->
            out.write(text.toByteArray())
        }
    }

    private fun readTextExternal(ctx: Context, dir: DocumentFile, name: String): String? {
        val fileDoc = dir.findFile(name) ?: return null
        return ctx.contentResolver.openInputStream(fileDoc.uri)?.use { input ->
            input.readBytes().decodeToString()
        }
    }

    /**
     * Ensures a given JSON data file exists in the *current style folder*.
     * If it does not exist, it will be created with [defaultContent].
     */
    private fun ensureFileExists(ctx: Context, fileName: String, defaultContent: String) {
        val extDir = styleExternalDir(ctx)
        if (extDir != null) {
            val existing = extDir.findFile(fileName)
            if (existing == null) {
                writeTextExternal(ctx, extDir, fileName, defaultContent)
            }
        } else {
            val dir = styleInternalDir(ctx) ?: return
            val file = File(dir, fileName)
            if (!file.exists()) {
                writeTextInternal(file, defaultContent)
            }
        }
    }

    /**
     * Loads JSON text for [fileName] from the *current style folder*.
     * If the file is missing, it is created with [defaultContent] and that
     * content is returned.
     */
    private fun loadJson(ctx: Context, fileName: String, defaultContent: String): String {
        // Ensure the file exists (or create it) first
        ensureFileExists(ctx, fileName, defaultContent)

        val extDir = styleExternalDir(ctx)
        return if (extDir != null) {
            readTextExternal(ctx, extDir, fileName) ?: defaultContent
        } else {
            val dir = styleInternalDir(ctx) ?: return defaultContent
            readTextInternal(File(dir, fileName)) ?: defaultContent
        }
    }

    /**
     * Saves JSON [content] for [fileName] into the *current style folder*.
     */
    private fun saveJson(ctx: Context, fileName: String, content: String) {
        val extDir = styleExternalDir(ctx)
        if (extDir != null) {
            writeTextExternal(ctx, extDir, fileName, content)
        } else {
            val dir = styleInternalDir(ctx) ?: return
            writeTextInternal(File(dir, fileName), content)
        }
    }

    // -----------------------------
    // Public API – Moves / Connections / Sequences
    // -----------------------------

    fun loadMoves(ctx: Context): List<Move> {
        val text = loadJson(ctx, "moves.json", "[]")
        return try {
            json.decodeFromString(text)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveMoves(ctx: Context, moves: List<Move>) {
        saveJson(ctx, "moves.json", json.encodeToString(moves))
    }

    fun loadConnections(ctx: Context): List<Connection> {
        val text = loadJson(ctx, "connections.json", "[]")
        return try {
            json.decodeFromString(text)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveConnections(ctx: Context, connections: List<Connection>) {
        saveJson(ctx, "connections.json", json.encodeToString(connections))
    }

    fun loadSequences(ctx: Context): List<Sequence> {
        val text = loadJson(ctx, "sequences.json", "[]")
        return try {
            json.decodeFromString(text)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveSequences(ctx: Context, sequences: List<Sequence>) {
        saveJson(ctx, "sequences.json", json.encodeToString(sequences))
    }

    // -----------------------------
    // Style discovery (for Settings)
    // -----------------------------

    /**
     * Scans the currently configured base folder for style folders.
     *
     * - If a SAF base folder is chosen: returns names of all subfolders.
     * - Otherwise: uses the internal base folder and lists its subfolders.
     */
    fun listStyles(ctx: Context): List<String> {
        // Prefer external (user-chosen) base if available
        val extBase = externalBaseDir(ctx)
        if (extBase != null) {
            return extBase
                .listFiles()
                .filter { it.isDirectory }
                .mapNotNull { it.name }
                .sorted()
        }

        // Fallback: internal base
        val base = internalBaseDir(ctx)
        return base
            .listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
    }
}
