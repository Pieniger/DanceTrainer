package com.example.dancetrainer.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Prefs
import com.example.dancetrainer.data.Storage

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    // --- TTS toggle (only one feature flag) ---
    var ttsEnabled by remember {
        mutableStateOf(Prefs.isVoiceEnabled(ctx))
    }

    // --- Base data folder (SAF tree or null -> internal) ---
    var treeUri by remember {
        mutableStateOf(Prefs.getTreeUri(ctx) ?: "")
    }

    // --- Styles = subfolders of current base folder ---
    var styles by remember {
        mutableStateOf(Storage.listStyles(ctx))
    }

    // Currently selected style (just the folder name)
    var selectedStyle by remember {
        mutableStateOf(Prefs.getStyle(ctx))
    }

    // When the screen first appears, make sure JSON files exist
    LaunchedEffect(Unit) {
        if (selectedStyle.isNotBlank()) {
            Storage.loadMoves(ctx)
            Storage.loadConnections(ctx)
            Storage.loadSequences(ctx)
        }
    }

    // SAF folder picker for the base data folder
    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                ctx.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) {
                // Some devices/flows won't support persistable permissions; ignore
            }

            treeUri = uri.toString()
            Prefs.setTreeUri(ctx, treeUri)

            // Refresh styles based on this new base folder
            styles = Storage.listStyles(ctx)
            if (styles.isNotEmpty()) {
                selectedStyle = styles.first()
                Prefs.setStyle(ctx, selectedStyle)
                Storage.loadMoves(ctx)
                Storage.loadConnections(ctx)
                Storage.loadSequences(ctx)
            } else {
                Toast.makeText(
                    ctx,
                    "No subfolders found. Create one per dance style.",
                    Toast.LENGTH_LONG
                ).show()
            }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- TTS enabled switch ---
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = ttsEnabled,
                    onCheckedChange = {
                        ttsEnabled = it
                        Prefs.setVoiceEnabled(ctx, it)
                    }
                )
                Text("Text-to-Speech enabled")
            }

            // --- Data Folder card ---
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Data Folder", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (treeUri.isBlank())
                            "Internal app storage (no external folder selected)"
                        else
                            treeUri,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(onClick = { folderPicker.launch(null) }) {
                        Text("Choose Folder")
                    }
                }
            }

            // --- Style selection card ---
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Dance Style", style = MaterialTheme.typography.titleMedium)

                    if (styles.isEmpty()) {
                        Text(
                            "No style folders found.\n" +
                                    "Create subfolders in the base folder (or internal styles base)\n" +
                                    "and they will appear here as selectable styles.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        styles.forEach { styleName ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = styleName == selectedStyle,
                                    onClick = {
                                        selectedStyle = styleName
                                        Prefs.setStyle(ctx, styleName)
                                        Storage.loadMoves(ctx)
                                        Storage.loadConnections(ctx)
                                        Storage.loadSequences(ctx)
                                        Toast.makeText(
                                            ctx,
                                            "Style switched to $styleName",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                                Text(styleName)
                            }
                        }
                    }
                }
            }
        }
    }
}
