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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    // current values from Prefs
    var treeUri by remember { mutableStateOf(Prefs.getTreeUri(ctx) ?: "") }
    var styles by remember { mutableStateOf(listOf<String>()) }
    var selectedStyle by remember { mutableStateOf(Prefs.getStyle(ctx)) }
    var voiceEnabled by remember { mutableStateOf(Prefs.isVoiceEnabled(ctx)) }

    var styleMenuExpanded by remember { mutableStateOf(false) }

    // Load styles and ensure files on first composition
    LaunchedEffect(Unit) {
        val newStyles = Storage.listStyles(ctx)
        styles = newStyles

        // If Prefs has no style yet but there are folders → pick the first
        if (selectedStyle.isBlank() && newStyles.isNotEmpty()) {
            selectedStyle = newStyles.first()
            Prefs.setStyle(ctx, selectedStyle)
        }

        // Ensure files for whatever style is now active
        if (selectedStyle.isNotBlank()) {
            Storage.ensureFilesForCurrentStyle(ctx)
        }
    }

    // Folder picker for base DanceTrainer folder (external)
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                ctx.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) {
                // Some devices may not support persistable permissions; ignore
            }

            treeUri = uri.toString()
            Prefs.setTreeUri(ctx, treeUri)

            // refresh style list from this new base
            val newStyles = Storage.listStyles(ctx)
            styles = newStyles

            if (newStyles.isNotEmpty()) {
                selectedStyle = newStyles.first()
                Prefs.setStyle(ctx, selectedStyle)
                Storage.ensureFilesForCurrentStyle(ctx)
                Toast.makeText(ctx, "Folder selected. Style: $selectedStyle", Toast.LENGTH_SHORT).show()
            } else {
                selectedStyle = ""
                Prefs.setStyle(ctx, "")
                Toast.makeText(ctx, "Folder selected, but no subfolders found as styles.", Toast.LENGTH_LONG).show()
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
            // Voice / TTS toggle
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Voice announcements", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "If enabled, the app can use TTS to speak move names.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = voiceEnabled,
                        onCheckedChange = {
                            voiceEnabled = it
                            Prefs.setVoiceEnabled(ctx, it)
                        }
                    )
                }
            }

            // Data folder selection
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Data folder", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (treeUri.isBlank())
                            "Currently using internal app storage."
                        else
                            treeUri,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = { folderPicker.launch(null) }) {
                        Text("Choose folder")
                    }
                }
            }

            // Style selection
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Dance style", style = MaterialTheme.typography.titleMedium)

                    if (styles.isEmpty()) {
                        Text(
                            "No style folders found.\n" +
                                "Create one folder per style inside the base folder (or internal 'DanceTrainer' dir).",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            "Select which style (subfolder) to use for moves, connections, and sequences.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { styleMenuExpanded = true }
                        ) {
                            Text(
                                if (selectedStyle.isBlank())
                                    "Select style"
                                else
                                    selectedStyle
                            )
                        }

                        DropdownMenu(
                            expanded = styleMenuExpanded,
                            onDismissRequest = { styleMenuExpanded = false }
                        ) {
                            styles.forEach { styleName ->
                                DropdownMenuItem(
                                    text = { Text(styleName) },
                                    onClick = {
                                        styleMenuExpanded = false
                                        if (selectedStyle != styleName) {
                                            selectedStyle = styleName
                                            Prefs.setStyle(ctx, styleName)
                                            Storage.ensureFilesForCurrentStyle(ctx)
                                            Toast.makeText(
                                                ctx,
                                                "Style switched to $styleName",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
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
