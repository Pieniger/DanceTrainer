package com.example.dancetrainer.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

object Storage {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private fun internalStyleDir(ctx: Context): File {
        val name = Prefs.getStyle(ctx)
        val dir = File(File(ctx.filesDir, "styles"), name)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** If a SAF tree is chosen, returns a DocumentFile for styles/<style>. Else, null. */
    private fun externalStyleDir(ctx: Context): DocumentFile? {
        val str = Prefs.getTreeUri(ctx) ?: return null
        val tree = DocumentFile.fromTreeUri(ctx, Uri.parse(str)) ?: return null

        // styles folder
        val styles = tree.findFile("styles") ?: tree.createDirectory("styles") ?: return null
        val styleName = Prefs.getStyle(ctx)
        return (styles.findFile(styleName) ?: styles.createDirectory(styleName))
    }

    private fun writeTextInternal(file: File, text: String) {
        file.parentFile?.mkdirs()
        file.writeText(text)
    }

    private fun readTextInternal(file: File): String? =
        if (file.exists()) file.readText() else null

    private fun writeTextExternal(ctx: Context, dir: DocumentFile, name: String, text: String) {
        val existing = dir.findFile(name)
        val fileDoc = existing ?: dir.createFile("application/json", name) ?: return
        ctx.contentResolver.openOutputStream(fileDoc.uri, "rwt")?.use { it.write(text.toByteArray()) }
    }

    private fun readTextExternal(ctx: Context, dir: DocumentFile, name: String): String? {
        val f = dir.findFile(name) ?: return null
        return ctx.contentResolver.openInputStream(f.uri)?.use { it.readBytes().decodeToString() }
    }

    private fun saveJson(ctx: Context, fileName: String, content: String) {
        val externalDir = externalStyleDir(ctx)
        if (externalDir != null) writeTextExternal(ctx, externalDir, fileName, content)
        else writeTextInternal(File(internalStyleDir(ctx), fileName), content)
    }

    private fun loadJson(ctx: Context, fileName: String): String? {
        val externalDir = externalStyleDir(ctx)
        return if (externalDir != null) readTextExternal(ctx, externalDir, fileName)
        else readTextInternal(File(internalStyleDir(ctx), fileName))
    }

    fun saveMoves(context: Context, moves: List<Move>) =
        saveJson(context, "moves.json", json.encodeToString(moves))

    fun loadMoves(context: Context): List<Move> =
        loadJson(context, "moves.json")?.let { json.decodeFromString(it) } ?: emptyList()

    fun saveConnections(context: Context, connections: List<Connection>) =
        saveJson(context, "connections.json", json.encodeToString(connections))

    fun loadConnections(context: Context): List<Connection> =
        loadJson(context, "connections.json")?.let { json.decodeFromString(it) } ?: emptyList()

    fun saveSequences(context: Context, sequences: List<Sequence>) =
        saveJson(context, "sequences.json", json.encodeToString(sequences))

    fun loadSequences(context: Context): List<Sequence> =
        loadJson(context, "sequences.json")?.let { json.decodeFromString(it) } ?: emptyList()

    /** List of style folders under current base (internal or external). */
    fun listStyles(context: Context): List<String> {
        val tree = Prefs.getTreeUri(context)
        return if (tree == null) {
            val base = File(context.filesDir, "styles")
            if (!base.exists()) base.mkdirs()
            base.list()?.sorted()?.toList() ?: emptyList()
        } else {
            val root = DocumentFile.fromTreeUri(context, Uri.parse(tree)) ?: return emptyList()
            val styles = root.findFile("styles") ?: return emptyList()
            styles.listFiles().filter { it.isDirectory }.mapNotNull { it.name }.sorted()
        }
    }

    fun createStyle(context: Context, name: String) {
        val safe = name.trim().ifEmpty { "Unnamed" }
        val tree = Prefs.getTreeUri(context)
        if (tree == null) {
            File(File(context.filesDir, "styles"), safe).mkdirs()
        } else {
            val root = DocumentFile.fromTreeUri(context, Uri.parse(tree)) ?: return
            val styles = root.findFile("styles") ?: root.createDirectory("styles") ?: return
            styles.findFile(safe) ?: styles.createDirectory(safe)
        }
    }
}
