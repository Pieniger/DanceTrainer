package com.example.dancetrainer.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

object Storage {

    private const val MOVES_FILE = "moves.txt"
    private const val CONNECTIONS_FILE = "connections.txt"
    private const val SEQUENCES_FILE = "sequences.txt"

    // ----- helpers -----

    private fun currentStyle(ctx: Context): String? {
        val s = Prefs.getStyle(ctx).trim()
        return if (s.isEmpty()) null else s
    }

    private fun internalStyleDir(ctx: Context, style: String): File {
        val dir = File(ctx.filesDir, style)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun externalRoot(ctx: Context): DocumentFile? {
        val treeStr = Prefs.getTreeUri(ctx) ?: return null
        val uri = Uri.parse(treeStr)
        return DocumentFile.fromTreeUri(ctx, uri)
    }

    private fun externalStyleDir(ctx: Context, style: String): DocumentFile? {
        val root = externalRoot(ctx) ?: return null
        val existing = root.findFile(style)
        return existing ?: root.createDirectory(style)
    }

    private fun readTextInternal(file: File): String? =
        if (file.exists()) file.readText() else null

    private fun writeTextInternal(file: File, text: String) {
        file.parentFile?.mkdirs()
        file.writeText(text)
    }

    private fun readTextExternal(ctx: Context, dir: DocumentFile, name: String): String? {
        val doc = dir.findFile(name) ?: return null
        return ctx.contentResolver.openInputStream(doc.uri)?.use { input ->
            input.readBytes().decodeToString()
        }
    }

    private fun writeTextExternal(ctx: Context, dir: DocumentFile, name: String, text: String) {
        val existing = dir.findFile(name)
        val doc = existing ?: dir.createFile("text/plain", name) ?: return
        ctx.contentResolver.openOutputStream(doc.uri, "rwt")?.use { out ->
            out.write(text.toByteArray())
        }
    }

    private fun readStyleFile(ctx: Context, style: String, name: String): String? {
        val extDir = externalStyleDir(ctx, style)
        return if (extDir != null) {
            readTextExternal(ctx, extDir, name)
        } else {
            val file = File(internalStyleDir(ctx, style), name)
            readTextInternal(file)
        }
    }

    private fun writeStyleFile(ctx: Context, style: String, name: String, text: String) {
        val extDir = externalStyleDir(ctx, style)
        if (extDir != null) {
            writeTextExternal(ctx, extDir, name, text)
        } else {
            val file = File(internalStyleDir(ctx, style), name)
            writeTextInternal(file, text)
        }
    }

    // ----- public API -----

    /** Ensure empty text files exist for moves / connections / sequences in the given style. */
    fun ensureFilesForStyle(ctx: Context, style: String) {
        // Internal
        run {
            val dir = internalStyleDir(ctx, style)
            val moves = File(dir, MOVES_FILE)
            if (!moves.exists()) moves.writeText("")
            val cons = File(dir, CONNECTIONS_FILE)
            if (!cons.exists()) cons.writeText("")
            val seq = File(dir, SEQUENCES_FILE)
            if (!seq.exists()) seq.writeText("")
        }

        // External
        val extDir = externalStyleDir(ctx, style)
        if (extDir != null) {
            listOf(MOVES_FILE, CONNECTIONS_FILE, SEQUENCES_FILE).forEach { name ->
                val existing = extDir.findFile(name)
                if (existing == null) {
                    writeTextExternal(ctx, extDir, name, "")
                }
            }
        }
    }

    /** List available style folders under the configured root. */
    fun listStyles(ctx: Context): List<String> {
        val extRoot = externalRoot(ctx)
        if (extRoot != null) {
            return extRoot.listFiles()
                .filter { it.isDirectory }
                .mapNotNull { it.name }
                .sorted()
        }

        // Fallback: internal style dirs directly under filesDir
        val base = ctx.filesDir
        return base.listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
    }

    fun loadMoves(ctx: Context): List<Move> {
        val style = currentStyle(ctx) ?: return emptyList()
        val text = readStyleFile(ctx, style, MOVES_FILE) ?: return emptyList()
        if (text.isBlank()) return emptyList()
        return text.lines()
            .mapNotNull { line ->
                if (line.isBlank()) return@mapNotNull null
                val parts = line.split('|')
                if (parts.size < 2) return@mapNotNull null
                val id = parts[0]
                val name = parts[1]
                val notes = if (parts.size >= 3) parts[2] else ""
                Move(id = id, name = name, notes = notes)
            }
    }

    fun saveMoves(ctx: Context, moves: List<Move>) {
        val style = currentStyle(ctx) ?: return
        val lines = moves.joinToString(separator = "\n") { m ->
            val safeNotes = m.notes.replace("\n", " ")
            "${m.id}|${m.name}|$safeNotes"
        }
        writeStyleFile(ctx, style, MOVES_FILE, lines)
    }

    fun loadConnections(ctx: Context): List<Connection> {
        val style = currentStyle(ctx) ?: return emptyList()
        val text = readStyleFile(ctx, style, CONNECTIONS_FILE) ?: return emptyList()
        if (text.isBlank()) return emptyList()
        return text.lines()
            .mapNotNull { line ->
                if (line.isBlank()) return@mapNotNull null
                val parts = line.split('|')
                if (parts.size < 4) return@mapNotNull null
                val fromId = parts[0]
                val toId = parts[1]
                val works = parts[2] == "1"
                val smooth = parts[3].toIntOrNull() ?: 0
                val notes = if (parts.size >= 5) parts[4] else ""
                Connection(
                    fromId = fromId,
                    toId = toId,
                    works = works,
                    smoothness = smooth,
                    notes = notes
                )
            }
    }

    fun saveConnections(ctx: Context, connections: List<Connection>) {
        val style = currentStyle(ctx) ?: return
        val lines = connections.joinToString(separator = "\n") { c ->
            val worksFlag = if (c.works) "1" else "0"
            val safeNotes = c.notes.replace("\n", " ")
            "${c.fromId}|${c.toId}|$worksFlag|${c.smoothness}|$safeNotes"
        }
        writeStyleFile(ctx, style, CONNECTIONS_FILE, lines)
    }

    // Sequences are placeholders for now
    fun loadSequences(ctx: Context): List<Sequence> = emptyList()
    fun saveSequences(ctx: Context, sequences: List<Sequence>) { /* no-op for now */ }
}
