package com.example.dancetrainer.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

/**
 * Storage helper for the current style.
 *
 * Base folder:
 * - If user selected a tree URI in Settings → that folder
 * - Else → internal app folder (filesDir/DanceTrainer)
 *
 * Inside base, each style is a sub-folder named after the style.
 *
 * Files per style:
 *  - moves.txt        lines: id|name|note
 *  - connections.txt  lines: fromId|toId|smoothness|note
 *  - sequences.txt    lines: id|name|difficulty|note|id1,id2,...
 */
object Storage {

    // ---------- base & style dirs ----------

    private fun internalBaseDir(ctx: Context): File {
        val dir = File(ctx.filesDir, "DanceTrainer")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun internalStyleDir(ctx: Context): File {
        val base = internalBaseDir(ctx)
        val styleName = Prefs.getStyle(ctx)
        val dir = File(base, styleName)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun externalBaseDir(ctx: Context): DocumentFile? {
        val uriStr = Prefs.getTreeUri(ctx) ?: return null
        return DocumentFile.fromTreeUri(ctx, Uri.parse(uriStr))
    }

    private fun externalStyleDir(ctx: Context): DocumentFile? {
        val base = externalBaseDir(ctx) ?: return null
        val styleName = Prefs.getStyle(ctx)
        val existing = base.findFile(styleName)
        return when {
            existing == null -> base.createDirectory(styleName)
            existing.isDirectory -> existing
            else -> null
        }
    }

    private fun hasExternal(ctx: Context): Boolean =
        Prefs.getTreeUri(ctx) != null

    // ---------- text helpers ----------

    private fun writeTextInternal(ctx: Context, fileName: String, text: String) {
        val file = File(internalStyleDir(ctx), fileName)
        file.parentFile?.mkdirs()
        file.writeText(text)
    }

    private fun readTextInternal(ctx: Context, fileName: String): String? {
        val file = File(internalStyleDir(ctx), fileName)
        return if (file.exists()) file.readText() else null
    }

    private fun writeTextExternal(ctx: Context, fileName: String, text: String) {
        val dir = externalStyleDir(ctx) ?: return
        val existing = dir.findFile(fileName)
        val fileDoc = existing ?: dir.createFile("text/plain", fileName) ?: return
        ctx.contentResolver.openOutputStream(fileDoc.uri, "rwt")?.use { out ->
            out.write(text.toByteArray())
        }
    }

    private fun readTextExternal(ctx: Context, fileName: String): String? {
        val dir = externalStyleDir(ctx) ?: return null
        val fileDoc = dir.findFile(fileName) ?: return null
        return ctx.contentResolver.openInputStream(fileDoc.uri)?.use { ins ->
            ins.readBytes().decodeToString()
        }
    }

    private fun writeText(ctx: Context, fileName: String, text: String) {
        if (hasExternal(ctx)) writeTextExternal(ctx, fileName, text)
        else writeTextInternal(ctx, fileName, text)
    }

    private fun readText(ctx: Context, fileName: String): String? {
        return if (hasExternal(ctx)) readTextExternal(ctx, fileName)
        else readTextInternal(ctx, fileName)
    }

    // ---------- Moves ----------

    fun saveMoves(context: Context, moves: List<Move>) {
        val lines = moves.joinToString("\n") { m ->
            val safeNote = m.note.replace("\n", " ")
            "${m.id}|${m.name}|$safeNote"
        }
        writeText(context, "moves.txt", lines)
    }

    fun loadMoves(context: Context): List<Move> {
        val text = readText(context, "moves.txt") ?: return emptyList()
        return text
            .lineSequence()
            .mapNotNull { line ->
                if (line.isBlank()) return@mapNotNull null
                val parts = line.split("|")
                val id = parts.getOrNull(0)?.trim().orEmpty()
                if (id.isEmpty()) return@mapNotNull null
                val name = parts.getOrNull(1)?.trim().orEmpty()
                val note = parts.getOrNull(2)?.trim().orEmpty()
                Move(id = id, name = name, note = note)
            }
            .toList()
    }

    // ---------- Connections ----------

    fun saveConnections(context: Context, connections: List<Connection>) {
        val lines = connections.joinToString("\n") { c ->
            val safeNote = c.note.replace("\n", " ")
            "${c.from}|${c.to}|${c.smoothness}|$safeNote"
        }
        writeText(context, "connections.txt", lines)
    }

    fun loadConnections(context: Context): List<Connection> {
        val text = readText(context, "connections.txt") ?: return emptyList()
        return text
            .lineSequence()
            .mapNotNull { line ->
                if (line.isBlank()) return@mapNotNull null
                val parts = line.split("|")
                val from = parts.getOrNull(0)?.trim().orEmpty()
                val to = parts.getOrNull(1)?.trim().orEmpty()
                if (from.isEmpty() || to.isEmpty()) return@mapNotNull null
                val smoothness = parts.getOrNull(2)?.toIntOrNull() ?: 5
                val note = parts.getOrNull(3)?.trim().orEmpty()
                Connection(from = from, to = to, smoothness = smoothness, note = note)
            }
            .toList()
    }

    // ---------- Sequences (simple text format, placeholder use) ----------

    fun saveSequences(context: Context, sequences: List<Sequence>) {
        val lines = sequences.joinToString("\n") { s ->
            val safeNote = s.note.replace("\n", " ")
            val movesJoined = s.moves.joinToString(",")
            "${s.id}|${s.name}|${s.difficulty}|$safeNote|$movesJoined"
        }
        writeText(context, "sequences.txt", lines)
    }

    fun loadSequences(context: Context): List<Sequence> {
        val text = readText(context, "sequences.txt") ?: return emptyList()
        return text
            .lineSequence()
            .mapNotNull { line ->
                if (line.isBlank()) return@mapNotNull null
                val parts = line.split("|")
                val id = parts.getOrNull(0)?.trim().orEmpty()
                if (id.isEmpty()) return@mapNotNull null
                val name = parts.getOrNull(1)?.trim().orEmpty()
                val difficulty = parts.getOrNull(2)?.toIntOrNull() ?: 0
                val note = parts.getOrNull(3)?.trim().orEmpty()
                val moves = parts.getOrNull(4)
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()
                Sequence(id = id, name = name, difficulty = difficulty, note = note, moves = moves)
            }
            .toList()
    }

    // ---------- Styles ----------

    /**
     * List style folders under the current base folder.
     * (No special "styles" subfolder, just direct children.)
     */
    fun listStyles(context: Context): List<String> {
        val tree = Prefs.getTreeUri(context)
        return if (tree == null) {
            val base = internalBaseDir(context)
            base.listFiles()
                ?.filter { it.isDirectory }
                ?.mapNotNull { it.name }
                ?.filter { it.isNotBlank() }
                ?.sorted()
                ?: emptyList()
        } else {
            val root = externalBaseDir(context) ?: return emptyList()
            root.listFiles()
                .filter { it.isDirectory }
                .mapNotNull { it.name }
                .sorted()
        }
    }

    /**
     * Ensure that moves.txt / connections.txt / sequences.txt exist
     * for the currently selected style (create empty files if needed).
     */
    fun ensureFilesForCurrentStyle(context: Context) {
        if (readText(context, "moves.txt") == null) {
            writeText(context, "moves.txt", "")
        }
        if (readText(context, "connections.txt") == null) {
            writeText(context, "connections.txt", "")
        }
        if (readText(context, "sequences.txt") == null) {
            writeText(context, "sequences.txt", "")
        }
    }
}
