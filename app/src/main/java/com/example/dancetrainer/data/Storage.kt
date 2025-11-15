package com.example.dancetrainer.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

/**
 * Storage for per-style data.
 *
 * For each style we use a subfolder named after the style:
 *
 *  - Internal:  <filesDir>/styles/<StyleName>/
 *  - External:  <SAF tree>/<StyleName>/
 *
 * Inside that folder we keep:
 *  - moves.txt
 *  - connections.txt
 *  - sequences.txt   (future)
 *
 * Moves:
 *   id|name|notes
 *
 * Connections:
 *   fromId|toId|smoothness
 */

object Storage {

    // ---------- PATH RESOLUTION ----------

    /** Root for internal styles: <filesDir>/styles */
    private fun internalStylesRoot(ctx: Context): File {
        val dir = File(ctx.filesDir, "styles")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** Folder for the currently selected style (internal). */
    private fun internalStyleDir(ctx: Context): File {
        val styleName = Prefs.getStyle(ctx).ifBlank { "Default" }
        val dir = File(internalStylesRoot(ctx), styleName)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** Folder for the currently selected style (external), or null if none. */
    private fun externalStyleDir(ctx: Context): DocumentFile? {
        val uriStr = Prefs.getTreeUri(ctx) ?: return null
        val tree = DocumentFile.fromTreeUri(ctx, Uri.parse(uriStr)) ?: return null
        val styleName = Prefs.getStyle(ctx).ifBlank { "Default" }

        val existing = tree.findFile(styleName)
        return when {
            existing == null -> tree.createDirectory(styleName)
            existing.isDirectory -> existing
            else -> null
        }
    }

    // ---------- LOW-LEVEL IO HELPERS ----------

    private fun writeTextInternal(file: File, text: String) {
        file.parentFile?.mkdirs()
        file.writeText(text)
    }

    private fun readTextInternal(file: File): String? =
        if (file.exists()) file.readText() else null

    private fun writeTextExternal(ctx: Context, dir: DocumentFile, name: String, text: String) {
        val existing = dir.findFile(name)
        val fileDoc = existing ?: dir.createFile("text/plain", name) ?: return
        ctx.contentResolver.openOutputStream(fileDoc.uri, "rwt")?.use { out ->
            out.write(text.toByteArray())
        }
    }

    private fun readTextExternal(ctx: Context, dir: DocumentFile, name: String): String? {
        val f = dir.findFile(name) ?: return null
        return ctx.contentResolver.openInputStream(f.uri)?.use { input ->
            input.readBytes().decodeToString()
        }
    }

    private fun saveText(ctx: Context, fileName: String, content: String) {
        val externalDir = externalStyleDir(ctx)
        if (externalDir != null) {
            writeTextExternal(ctx, externalDir, fileName, content)
        } else {
            val f = File(internalStyleDir(ctx), fileName)
            writeTextInternal(f, content)
        }
    }

    private fun loadText(ctx: Context, fileName: String): String? {
        val externalDir = externalStyleDir(ctx)
        return if (externalDir != null) {
            readTextExternal(ctx, externalDir, fileName)
        } else {
            val f = File(internalStyleDir(ctx), fileName)
            readTextInternal(f)
        }
    }

    // ---------- STYLES LIST ----------

    /**
     * Lists available styles as folder names.
     *
     * - If no SAF tree is chosen, we look under <filesDir>/styles.
     * - If a SAF tree is chosen, we list its immediate sub-folders.
     */
    fun listStyles(context: Context): List<String> {
        val tree = Prefs.getTreeUri(context)
        return if (tree == null) {
            val base = internalStylesRoot(context)
            base.listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name ?: "" }
                ?.filter { it.isNotBlank() }
                ?.sorted()
                ?: emptyList()
        } else {
            val root = DocumentFile.fromTreeUri(context, Uri.parse(tree)) ?: return emptyList()
            root.listFiles()
                .filter { it.isDirectory }
                .mapNotNull { it.name }
                .sorted()
        }
    }

    // ---------- MOVES ----------

    private const val MOVES_FILE = "moves.txt"

    private fun encodeMove(m: Move): String {
        // replace newlines with spaces to keep it single-line
        val safeName = m.name.replace("\n", " ")
        val safeNotes = m.notes.replace("\n", " ")
        return listOf(m.id, safeName, safeNotes).joinToString("|")
    }

    private fun decodeMove(line: String): Move? {
        if (line.isBlank()) return null
        val parts = line.split("|")
        if (parts.size < 2) return null
        val id = parts[0]
        val name = parts[1]
        val notes = if (parts.size >= 3) parts[2] else ""
        return Move(id = id, name = name, notes = notes)
    }

    fun saveMoves(context: Context, moves: List<Move>) {
        val content = moves.joinToString("\n") { encodeMove(it) }
        saveText(context, MOVES_FILE, content)
    }

    fun loadMoves(context: Context): List<Move> {
        val text = loadText(context, MOVES_FILE)
        if (text == null) {
            // create an empty file for this style
            saveMoves(context, emptyList())
            return emptyList()
        }
        return text
            .lineSequence()
            .mapNotNull { decodeMove(it) }
            .toList()
    }

    // ---------- CONNECTIONS ----------

    private const val CONNECTIONS_FILE = "connections.txt"

    private fun encodeConnection(c: Connection): String {
        return listOf(c.from, c.to, c.smoothness.toString()).joinToString("|")
    }

    private fun decodeConnection(line: String): Connection? {
        if (line.isBlank()) return null
        val parts = line.split("|")
        if (parts.size < 3) return null
        val from = parts[0]
        val to = parts[1]
        val smoothness = parts[2].toIntOrNull() ?: 0
        return Connection(from = from, to = to, smoothness = smoothness)
    }

    fun saveConnections(context: Context, connections: List<Connection>) {
        val content = connections.joinToString("\n") { encodeConnection(it) }
        saveText(context, CONNECTIONS_FILE, content)
    }

    fun loadConnections(context: Context): List<Connection> {
        val text = loadText(context, CONNECTIONS_FILE)
        if (text == null) {
            // create an empty file for this style
            saveConnections(context, emptyList())
            return emptyList()
        }
        return text
            .lineSequence()
            .mapNotNull { decodeConnection(it) }
            .toList()
    }

    // ---------- SEQUENCES (placeholder for now) ----------

    private const val SEQUENCES_FILE = "sequences.txt"

    fun saveSequences(context: Context, sequences: List<Sequence>) {
        // Placeholder: implement similar to moves/connections once needed.
        val unused = sequences
        unused.size
        // You could serialize as: id|name|difficulty|id1,id2,id3...
        // and store in SEQUENCES_FILE.
    }

    fun loadSequences(context: Context): List<Sequence> {
        // Placeholder for future use.
        return emptyList()
    }
}
