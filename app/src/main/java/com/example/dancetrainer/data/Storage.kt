package com.example.dancetrainer.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

object Storage {

    private const val MOVES_FILE = "moves.txt"
    private const val CONNECTIONS_FILE = "connections.txt"

    // ---------- helpers ----------

    private fun currentStyle(ctx: Context): String? {
        val s = Prefs.getStyle(ctx).trim()
        return if (s.isEmpty()) null else s
    }

    private fun internalStyleDir(ctx: Context, style: String): File =
        File(ctx.filesDir, style).apply { mkdirs() }

    private fun externalRoot(ctx: Context): DocumentFile? {
        val treeStr = Prefs.getTreeUri(ctx) ?: return null
        return DocumentFile.fromTreeUri(ctx, Uri.parse(treeStr))
    }

    private fun externalStyleDir(ctx: Context, style: String): DocumentFile? {
        val root = externalRoot(ctx) ?: return null
        return root.findFile(style) ?: root.createDirectory(style)
    }

    private fun readText(ctx: Context, style: String, name: String): String? {
        val ext = externalStyleDir(ctx, style)
        if (ext != null) {
            val doc = ext.findFile(name) ?: return null
            return ctx.contentResolver.openInputStream(doc.uri)?.use {
                it.readBytes().decodeToString()
            }
        }
        val file = File(internalStyleDir(ctx, style), name)
        return if (file.exists()) file.readText() else null
    }

    private fun writeText(ctx: Context, style: String, name: String, text: String) {
        val ext = externalStyleDir(ctx, style)
        if (ext != null) {
            val doc = ext.findFile(name)
                ?: ext.createFile("text/plain", name)
                ?: return
            ctx.contentResolver.openOutputStream(doc.uri, "rwt")?.use {
                it.write(text.toByteArray())
            }
        } else {
            val file = File(internalStyleDir(ctx, style), name)
            file.writeText(text)
        }
    }

    // ---------- moves ----------

    fun loadMoves(ctx: Context): List<Move> {
        val style = currentStyle(ctx) ?: return emptyList()
        val text = readText(ctx, style, MOVES_FILE) ?: return emptyList()

        return text.lineSequence()
            .mapNotNull { line ->
                val p = line.split('|')
                if (p.size < 2) return@mapNotNull null
                Move(
                    id = p[0],
                    name = p[1],
                    notes = p.getOrElse(2) { "" }
                )
            }
            .toList()
    }

    fun saveMoves(ctx: Context, moves: List<Move>) {
        val style = currentStyle(ctx) ?: return
        val text = moves.joinToString("\n") {
            "${it.id}|${it.name}|${it.notes}"
        }
        writeText(ctx, style, MOVES_FILE, text)
    }

    // ---------- connections ----------

    fun loadConnections(ctx: Context): List<Connection> {
        val style = currentStyle(ctx) ?: return emptyList()
        val text = readText(ctx, style, CONNECTIONS_FILE) ?: return emptyList()

        return text.lineSequence()
            .mapNotNull { line ->
                val p = line.split('|')
                if (p.size < 5) return@mapNotNull null
                Connection(
                    fromId = p[0],
                    toId = p[1],
                    smoothness = p[2].toIntOrNull()?.coerceIn(1, 5) ?: 3,
                    works = p[3].toBoolean(),
                    notes = p[4]
                )
            }
            .toList()
    }

    fun saveConnections(ctx: Context, connections: List<Connection>) {
        val style = currentStyle(ctx) ?: return
        val text = connections.joinToString("\n") {
            "${it.fromId}|${it.toId}|${it.smoothness}|${it.works}|${it.notes}"
        }
        writeText(ctx, style, CONNECTIONS_FILE, text)
    }
}
