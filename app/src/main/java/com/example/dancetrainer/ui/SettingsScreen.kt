package com.example.dancetrainer.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.documentfile.provider.DocumentFile
import com.example.dancetrainer.data.Prefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    // Read persisted values from Prefs
    val initialTree = remember { Prefs.getTreeUri(ctx) }
    val initialStyle = remember { Prefs.getStyle(ctx) ?: "" }
    val initialVoice = remember { Prefs.isVoiceEnabled(ctx) }

    var baseFolderUri by remember { mutableStateOf(initialTree) }
    var availableStyles by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedStyle by remember { mutableStateOf(initialStyle) }  // always non-null, can be ""
    var voiceEnabled by remember { mutableStateOf(initialVoice) }

    // Helper: scan subfolders of the base folder as styles
    fun scanStyles(uriString: String?): List<String> {
        if (uriString.isNullOrEmpty()) return emptyList()
        val uri = Uri.parse(uriString)
        val tree = DocumentFile.fromTreeUri(ctx, uri) ?: return emptyList()
        return tree.listFiles()
            .filter { it.isDirectory }
            .mapNotNull { it.name }
            .sorted()
    }

    // Initial scan whenever base folder changes
    LaunchedEffect(baseFolderUri) {
        val styles = scanStyles(baseFolderUri)
        availableStyles = styles
        if (styles.isNotEmpty()) {
            if (selectedStyle !in styles) {
                selectedStyle = styles.first()
                Prefs.setStyle(ctx, selectedStyle)
            }
        } else {
            selectedStyle = ""
        }
    }

    // Folder picker (for base DanceTrainer folder)
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                ctx.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) {
                // ignore if not supported
            }

            baseFolderUri = uri.toString()
            Prefs.setTreeUri(ctx, baseFolderUri)

            // Rescan styles for this folder
            val styles = scanStyles(baseFolderUri)
            availableStyles = styles
            if (styles.isNotEmpty()) {
                selectedStyle = styles.first()
                Prefs.setStyle(ctx, selectedStyle)
            } else {
                selectedStyle = ""
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
            // Base folder card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Base Data Folder",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = baseFolderUri ?: "No folder selected",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = { folderPicker.launch(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose Folder")
                    }
                }
            }

            // Style selection (if we have a base folder and styles)
            if (!baseFolderUri.isNullOrEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Dance Style",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (availableStyles.isEmpty()) {
                            Text(
                                text = "No subfolders found. Create one folder per style in the selected base folder.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            var expanded by remember { mutableStateOf(false) }

                            Text(
                                text = if (selectedStyle.isNotEmpty())
                                    selectedStyle
                                else
                                    "No style selected",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            )

                            Button(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Change Style")
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                availableStyles.forEach { styleName ->
                                    DropdownMenuItem(
                                        text = { Text(styleName) },
                                        onClick = {
                                            selectedStyle = styleName
                                            Prefs.setStyle(ctx, styleName)
                                            expanded = false
                                        }
                                    )
                                }
                            }

                            Text(
                                text = "Styles are taken from subfolder names inside the base folder.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Divider()

            // TTS / Voice announcements toggle
            Card(modifier = Modifier.fillMaxWidth()) {
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
                            "If enabled, the app uses TTS to speak the next move.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = voiceEnabled,
                        onCheckedChange = { checked ->
                            voiceEnabled = checked
                            Prefs.setVoiceEnabled(ctx, checked)
                        }
                    )
                }
            }
        }
    }
}
