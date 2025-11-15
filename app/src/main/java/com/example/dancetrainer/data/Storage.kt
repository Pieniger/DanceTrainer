package com.example.dancetrainer.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * File layout:
 *
 * Root folder (chosen in Settings, stored as tree URI in Prefs):
 *   /<styleName1>/
 *       moves.txt
 *       connections.txt
 *       sequences.txt
 *   /<styleName2>/
 *       ...
 *
 * We never manage the style folders themselves; they are created manually
 * (or externally). We only create the text files if missing.
 */
object Storage {

    private const val MOVES_FILE = "moves.txt"
    private const val CONNECTIONS_FILE = "connections.txt"
    private const val SEQUENCES_FILE = "sequences.txt"

    // --- Helpers for SAF & style directories --------------------------------

    private fun getRootDocument(ctx: Context): DocumentFile? {
        val uriStr = Prefs.getRootUri(ctx) ?: return null
        val uri = Uri.parse(uriStr)
        return DocumentFile.fromTreeUri(ctx, uri)
    }

    /** List of immediate subdirectories under the root folder – used as style names. */
    fun listStyles(ctx: Context): List<String> {
        val root = getRootDocument(ctx) ?: return emptyList()
        return root.listFiles()
            .filter { it.isDirectory }
            .mapNotNull { it.name }
            .sorted()
    }

    private fun findStyleDir(ctx: Context, style: String?): DocumentFile? {
        val name = style ?: Prefs.getStyle(ctx) ?: return null
        val root = getRootDocument(ctx) ?: return null
        return root.listFiles()
            .firstOrNull { it.isDirectory && it.name == name }
    }

    private fun getOrCreateTextFile(dir: DocumentFile, name: String, ctx: Context): DocumentFile? {
        val existing = dir.listFiles().firstOrNull { it.name == name }
        if (existing != null && existing.isFile) return existing
        return dir.createFile("text/plain", name)
    }

    private fun readTextFile(ctx: Context, file: DocumentFile?): String? {
        if (file == null || !file.isFile) return null
        return ctx.contentResolver.openInputStream(file.uri)?.use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        }
    }

    private fun writeTextFile(ctx: Context, file: DocumentFile?, text: String) {
        if (file == null || !file.isFile) return
        ctx.contentResolver.openOutputStream(file.uri, "rwt")?.use { out ->
            out.write(text.toByteArray(Charsets.UTF_8))
            out.flush()
        }
    }

    /** Ensure the 3 data files exist for the currently selected style. */
    fun ensureFilesForCurrentStyle(ctx: Context) {
        val style = Prefs.getStyle(ctx) ?: return
        val dir = findStyleDir(ctx, style) ?: return
        getOrCreateTextFile(dir, MOVES_FILE, ctx)
        getOrCreateTextFile(dir, CONNECTIONS_FILE, ctx)
        getOrCreateTextFile(dir, SEQUENCES_FILE, ctx)
    }

    // --- Public API used by SettingsScreen for folder selection --------------

    fun setRootFolder(ctx: Context, uri: Uri?) {
        val str = uri?.toString()
        Prefs.setRootUri(ctx, str)
    }

    fun getRootFolderLabel(ctx: Context): String {
        val uri = Prefs.getRootUri(ctx) ?: return "No folder selected"
        return uri
    }

    // --- Moves ---------------------------------------------------------------

    fun loadMoves(ctx: Context): List<Move> {
        val styleDir = findStyleDir(ctx, Prefs.getStyle(ctx)) ?: return emptyList()
        val movesFile = styleDir.listFiles().firstOrNull { it.name == MOVES_FILE } ?: return emptyList()
        val raw = readTextFile(ctx, movesFile) ?: return emptyList()

        return raw
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size >= 2) {
                    val id = parts[0]
                    val name = parts[1]
                    val notes = parts.getOrElse(2) { "" }
                    Move(id = id, name = name, notes = notes)
                } else null
            }
            .toList()
    }

    fun saveMoves(ctx: Context, moves: List<Move>) {
        val styleDir = findStyleDir(ctx, Prefs.getStyle(ctx)) ?: return
        val movesFile = getOrCreateTextFile(styleDir, MOVES_FILE, ctx) ?: return

        // Very simple '|' separated format; avoid '|' in names/notes for now.
        val text = moves.joinToString("\n") { m ->
            listOf(
                m.id.replace("\n", " "),
                m.name.replace("\n", " "),
                m.notes.replace("\n", " ")
            ).joinToString("|")
        }
        writeTextFile(ctx, movesFile, text)
    }

    // --- Connections ---------------------------------------------------------

    fun loadConnections(ctx: Context): List<Connection> {
        val styleDir = findStyleDir(ctx, Prefs.getStyle(ctx)) ?: return emptyList()
        val connFile = styleDir.listFiles().firstOrNull { it.name == CONNECTIONS_FILE } ?: return emptyList()
        val raw = readTextFile(ctx, connFile) ?: return emptyList()

        return raw
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size >= 3) {
                    val fromId = parts[0]
                    val toId = parts[1]
                    val resultStr = parts[2]
                    val result = when (resultStr) {
                        "WORKS" -> ConnectionResult.WORKS
                        "DOESNT_WORK" -> ConnectionResult.DOESNT_WORK
                        else -> return@mapNotNull null
                    }
                    val smoothness = parts.getOrNull(3)?.takeIf { it.isNotBlank() }?.toIntOrNull()
                    val notes = parts.getOrNull(4) ?: ""
                    Connection(
                        fromId = fromId,
                        toId = toId,
                        result = result,
                        smoothness = smoothness,
                        notes = notes
                    )
                } else null
            }
            .toList()
    }

    fun saveConnections(ctx: Context, connections: List<Connection>) {
        val styleDir = findStyleDir(ctx, Prefs.getStyle(ctx)) ?: return
        val connFile = getOrCreateTextFile(styleDir, CONNECTIONS_FILE, ctx) ?: return

        val text = connections.joinToString("\n") { c ->
            listOf(
                c.fromId.replace("\n", " "),
                c.toId.replace("\n", " "),
                c.result.name,
                c.smoothness?.toString() ?: "",
                c.notes.replace("\n", " ")
            ).joinToString("|")
        }
        writeTextFile(ctx, connFile, text)
    }

    // --- Sequences (placeholder) --------------------------------------------

    fun loadSequences(ctx: Context): List<Sequence> {
        val styleDir = findStyleDir(ctx, Prefs.getStyle(ctx)) ?: return emptyList()
        val seqFile = styleDir.listFiles().firstOrNull { it.name == SEQUENCES_FILE } ?: return emptyList()
        val raw = readTextFile(ctx, seqFile) ?: return emptyList()

        return raw
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size >= 3) {
                    val id = parts[0]
                    val name = parts[1]
                    val moves = parts[2].split(",").filter { it.isNotBlank() }
                    val notes = parts.getOrNull(3) ?: ""
                    Sequence(id = id, name = name, moves = moves, notes = notes)
                } else null
            }
            .toList()
    }

    fun saveSequences(ctx: Context, sequences: List<Sequence>) {
        val styleDir = findStyleDir(ctx, Prefs.getStyle(ctx)) ?: return
        val seqFile = getOrCreateTextFile(styleDir, SEQUENCES_FILE, ctx) ?: return

        val text = sequences.joinToString("\n") { s ->
            listOf(
                s.id.replace("\n", " "),
                s.name.replace("\n", " "),
                s.moves.joinToString(","),
                s.notes.replace("\n", " ")
            ).joinToString("|")
        }
        writeTextFile(ctx, seqFile, text)
    }
}
