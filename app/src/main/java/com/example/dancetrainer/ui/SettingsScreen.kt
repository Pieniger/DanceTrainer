package com.example.dancetrainer.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Prefs
import com.example.dancetrainer.data.Storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    // Currently selected base folder (SAF tree URI)
    var treeUri by remember { mutableStateOf(Prefs.getTreeUri(ctx) ?: "") }

    // Current style name (saved in Prefs)
    var currentStyle by remember { mutableStateOf(Prefs.getStyle(ctx)) }

    // Styles list from Storage (subfolders under base folder, or fallback)
    var styles by remember { mutableStateOf(listStylesSafe(ctx)) }

    // Folder picker (SAF)
    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                ctx.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) {
                // Some devices / flows may not support persistable permission; ignore.
            }

            treeUri = uri.toString()
            Prefs.setTreeUri(ctx, treeUri)

            // After choosing a folder, rescan it for styles
            styles = listStylesSafe(ctx)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ---------- DATA FOLDER ----------
            Text("Data Folder", style = MaterialTheme.typography.titleMedium)
            Text(
                if (treeUri.isEmpty()) {
                    "No folder selected (using internal app storage)."
                } else {
                    treeUri
                },
                style = MaterialTheme.typography.bodySmall
            )
            Button(onClick = { folderPicker.launch(null) }) {
                Text("Choose Data Folder")
            }

            // ---------- DANCE STYLE ----------
            Text("Dance Style", style = MaterialTheme.typography.titleMedium)
            Text(
                "Current style: $currentStyle",
                style = MaterialTheme.typography.bodyMedium
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                styles.forEach { styleName ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            currentStyle = styleName
                            Prefs.setStyle(ctx, styleName)
                        }) {
                            Text(
                                if (styleName == currentStyle)
                                    "$styleName (current)"
                                else
                                    styleName
                            )
                        }
                    }
                }

                if (styles.isEmpty()) {
                    Text(
                        "No style folders found in the selected directory. " +
                                "Create subfolders manually (one per style) and reopen this screen.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * Helper to safely get styles list without crashing UI.
 * Uses Storage.listStyles and falls back to ["Default"] on error.
 */
private fun listStylesSafe(ctx: Context): List<String> =
    try {
        val list = Storage.listStyles(ctx)
        if (list.isEmpty()) listOf("Default") else list
    } catch (_: Exception) {
        listOf("Default")
    }
