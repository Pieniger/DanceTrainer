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

    private fun styleDir(ctx: Context, style: String): File {
        val dir = File(ctx.filesDir, style)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun readFile(file: File): String =
        if (file.exists()) file.readText() else ""

    private fun writeFile(file: File, text: String) {
        file.parentFile?.mkdirs()
        file.writeText(text)
    }

    // ---------- MOVES ----------

    fun loadMoves(ctx: Context): List<Move> {
        val style = currentStyle(ctx) ?: return emptyList()
        val file = File(styleDir(ctx, style), MOVES_FILE)
        val text = readFile(file)
        if (text.isBlank()) return emptyList()

        return text.lines().mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 3) return@mapNotNull null
            Move(
                id = parts[0],
                name = parts[1],
                notes = parts[2]
            )
        }
    }

    fun saveMoves(ctx: Context, moves: List<Move>) {
        val style = currentStyle(ctx) ?: return
        val file = File(styleDir(ctx, style), MOVES_FILE)
        val text = moves.joinToString("\n") {
            "${it.id}|${it.name}|${it.notes}"
        }
        writeFile(file, text)
    }

    // ---------- CONNECTIONS ----------

    fun loadConnections(ctx: Context): List<Connection> {
        val style = currentStyle(ctx) ?: return emptyList()
        val file = File(styleDir(ctx, style), CONNECTIONS_FILE)
        val text = readFile(file)
        if (text.isBlank()) return emptyList()

        return text.lines().mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 6) return@mapNotNull null
            Connection(
                fromId = parts[0],
                toId = parts[1],
                works = parts[2].toBooleanStrictOrNull() ?: true,
                smoothness = parts[3].toIntOrNull()?.coerceIn(1, 5) ?: 3,
                notes = parts[4]
            )
        }
    }

    fun saveConnections(ctx: Context, connections: List<Connection>) {
        val style = currentStyle(ctx) ?: return
        val file = File(styleDir(ctx, style), CONNECTIONS_FILE)
        val text = connections.joinToString("\n") {
            "${it.fromId}|${it.toId}|${it.works}|${it.smoothness}|${it.notes}"
        }
        writeFile(file, text)
    }
}
