package com.example.dancetrainer.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private object PrefsKeys {
    const val PREFS = "settings"
    const val METRONOME_ON = "metronome_on"
    const val SOUND = "metronome_sound"
    const val TTS_SPEED = "tts_speed"
    const val STORAGE_URI = "storage_uri"
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences(PrefsKeys.PREFS, Context.MODE_PRIVATE) }

    var metronomeOn by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.METRONOME_ON, true)) }
    var metronomeSound by remember { mutableStateOf(prefs.getString(PrefsKeys.SOUND, "Click") ?: "Click") }
    var ttsSpeed by remember { mutableStateOf(prefs.getString(PrefsKeys.TTS_SPEED, "Default") ?: "Default") }
    var storageUri by remember { mutableStateOf(prefs.getString(PrefsKeys.STORAGE_URI, "") ?: "") }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
            // Persist permission
            val flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            try {
                ctx.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) { /* ignore if not supported */ }

            storageUri = uri.toString()
            prefs.edit().putString(PrefsKeys.STORAGE_URI, storageUri).apply()
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
            // Metronome on/off
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Switch(
                    checked = metronomeOn,
                    onCheckedChange = {
                        metronomeOn = it
                        prefs.edit().putBoolean(PrefsKeys.METRONOME_ON, it).apply()
                    }
                )
                Text("Metronome enabled")
            }

            // Metronome sound
            DropdownSetting(
                label = "Metronome Sound",
                options = listOf("Click", "Bell"),
                selected = metronomeSound,
                onSelected = {
                    metronomeSound = it
                    prefs.edit().putString(PrefsKeys.SOUND, it).apply()
                }
            )

            // TTS Speed
            DropdownSetting(
                label = "TTS Speed",
                options = listOf("Default", "Slow", "Fast"),
                selected = ttsSpeed,
                onSelected = {
                    ttsSpeed = it
                    prefs.edit().putString(PrefsKeys.TTS_SPEED, it).apply()
                }
            )

            // TTS voice preview (placeholder — integrate real TTS later)
            Button(onClick = {
                Toast.makeText(ctx, "Playing TTS preview ($ttsSpeed)…", Toast.LENGTH_SHORT).show()
            }) {
                Text("Preview TTS Voice")
            }

            // Storage path selection
            ElevatedCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Data Folder", style = MaterialTheme.typography.titleMedium)
                    Text(if (storageUri.isEmpty()) "No folder selected" else storageUri)
                    Button(onClick = { folderPicker.launch(null) }) {
                        Text("Choose Folder")
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownSetting(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = {
                            onSelected(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
