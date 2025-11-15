package com.example.dancetrainer.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    // ---- TTS toggle (use Prefs so it’s shared app-wide) ----
    var ttsEnabled by remember {
        mutableStateOf(Prefs.isVoiceEnabled(ctx))
    }

    // ---- Storage folder (SAF) ----
    var treeUri by remember {
        mutableStateOf(Prefs.getTreeUri(ctx) ?: "")
    }

    // ---- Styles list + selection ----
    var styles by remember {
        mutableStateOf(Storage.listStyles(ctx))
    }

    var selectedStyle by remember {
        mutableStateOf(
            Prefs.getStyle(ctx).takeIf { it.isNotBlank() } ?: styles.firstOrNull().orEmpty()
        )
    }

    // When we first enter the screen, if we have a style and styles list,
    // make sure files for that style exist.
    LaunchedEffect(selectedStyle) {
        if (selectedStyle.isNotBlank()) {
            Prefs.setStyle(ctx, selectedStyle)
            // Trigger creation of moves.json / connections.json / sequences.json
            Storage.loadMoves(ctx)
            Storage.loadConnections(ctx)
            Storage.loadSequences(ctx)
        }
    }

    // SAF folder picker
    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                ctx.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) {
                // Some devices might not support persistable perms; ignore
            }

            treeUri = uri.toString()
            Prefs.setTreeUri(ctx, treeUri)

            // Refresh styles from new base folder
            styles = Storage.listStyles(ctx)

            // Pick a reasonable default style from that folder, if any.
            selectedStyle = styles.firstOrNull().orEmpty()

            if (selectedStyle.isNotBlank()) {
                Prefs.setStyle(ctx, selectedStyle)
                Storage.loadMoves(ctx)
                Storage.loadConnections(ctx)
                Storage.loadSequences(ctx)
            }
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
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
            // ---- TTS enabled switch ----
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Switch(
                    checked = ttsEnabled,
                    onCheckedChange = {
                        ttsEnabled = it
                        Prefs.setVoiceEnabled(ctx, it)
                    }
                )
                Text("Text-to-Speech enabled")
            }

            // ---- Storage folder card ----
            ElevatedCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Data Folder", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (treeUri.isBlank())
                            "Internal app storage (no folder selected)"
                        else
                            treeUri
                    )
                    Button(onClick = { folderPicker.launch(null) }) {
                        Text("Choose Folder")
                    }
                }
            }

            // ---- Style dropdown (read-only list of folders) ----
            ElevatedCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Dance Style", style = MaterialTheme.typography.titleMedium)

                    if (styles.isEmpty()) {
                        Text(
                            "No style folders found.\n" +
                                    "Create subfolders in your chosen data folder (or internal storage) " +
                                    "to use them as styles."
                        )
                    } else {
                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedStyle,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Current style") },
                                modifier = Modifier
                                    .menuAnchor(), // required for M3 exposed menu
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                styles.forEach { styleName ->
                                    DropdownMenuItem(
                                        text = { Text(styleName) },
                                        onClick = {
                                            expanded = false
                                            selectedStyle = styleName
                                            Prefs.setStyle(ctx, styleName)

                                            // Force creation of data files for this style
                                            Storage.loadMoves(ctx)
                                            Storage.loadConnections(ctx)
                                            Storage.loadSequences(ctx)

                                            Toast
                                                .makeText(
                                                    ctx,
                                                    "Style switched to $styleName",
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
