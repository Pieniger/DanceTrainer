package com.example.dancetrainer.ui

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Prefs
import com.example.dancetrainer.data.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    // Prefs
    var metronomeEnabled by remember { mutableStateOf(Prefs.isMetronomeEnabled(ctx)) }
    var voiceEnabled by remember { mutableStateOf(Prefs.isVoiceEnabled(ctx)) }
    var metronomeSound by remember { mutableStateOf(Prefs.getMetronomeSound(ctx)) } // "click" | "bell"
    var treeUri by remember { mutableStateOf(Prefs.getTreeUri(ctx)) }

    // TTS
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var availableVoices by remember { mutableStateOf<List<Voice>>(emptyList()) }
    var selectedVoiceName by remember { mutableStateOf(Prefs.getTtsVoiceName(ctx)) }

    // Styles
    var styles by remember { mutableStateOf(Storage.listStyles(ctx)) }
    var currentStyle by remember { mutableStateOf(Prefs.getStyle(ctx)) }
    var showNewStyleDialog by remember { mutableStateOf(false) }
    var newStyleName by remember { mutableStateOf("") }

    // SAF launcher
    val chooseFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            // Persist access
            val flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            ctx.contentResolver.takePersistableUriPermission(uri, flags)
            Prefs.setTreeUri(ctx, uri.toString())
            treeUri = uri.toString()

            // Refresh styles list for new base
            styles = Storage.listStyles(ctx)
        }
    }

    // Init TTS and read voices
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main) {
            val engine = TextToSpeech(ctx) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    engine.language = Locale.getDefault()
                    val voices = engine.voices?.toList()?.sortedBy { it.name } ?: emptyList()
                    availableVoices = voices
                    // If user had a saved voice, try to set it
                    voices.firstOrNull { it.name == selectedVoiceName }?.let { engine.voice = it }
                }
            }
            tts = engine
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        // --- Storage folder ---
        Text("Storage Location", style = MaterialTheme.typography.titleMedium)
        Text(if (treeUri == null) "Internal (private to app)" else "Custom folder selected")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { chooseFolderLauncher.launch(null) }) { Text("Choose folder") }
            if (treeUri != null) {
                OutlinedButton(onClick = {
                    // Clear to internal storage
                    Prefs.setTreeUri(ctx, null)
                    treeUri = null
                    styles = Storage.listStyles(ctx)
                }) { Text("Use internal") }
            }
        }

        Divider()

        // --- Dance Style ---
        Text("Dance Style", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            var expanded by remember { mutableStateOf(false) }
            OutlinedButton(onClick = { expanded = true }) { Text(currentStyle) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                styles.forEach { name ->
                    DropdownMenuItem(text = { Text(name) }, onClick = {
                        currentStyle = name
                        Prefs.setStyle(ctx, name)
                        expanded = false
                    })
                }
            }
            OutlinedButton(onClick = { showNewStyleDialog = true }) { Text("New style") }
        }

        Divider()

        // --- Metronome ---
        Text("Metronome", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Switch(checked = metronomeEnabled, onCheckedChange = {
                metronomeEnabled = it; Prefs.setMetronomeEnabled(ctx, it)
            })
            Text(if (metronomeEnabled) "Enabled" else "Disabled")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sound:")
            var expanded by remember { mutableStateOf(false) }
            OutlinedButton(onClick = { expanded = true }) { Text(if (metronomeSound == "bell") "Bell" else "Click") }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("Click") }, onClick = { metronomeSound = "click"; Prefs.setMetronomeSound(ctx, "click"); expanded = false })
                DropdownMenuItem(text = { Text("Bell") }, onClick = { metronomeSound = "bell"; Prefs.setMetronomeSound(ctx, "bell"); expanded = false })
            }
            OutlinedButton(onClick = {
                tts?.speak(if (metronomeSound == "bell") "ding" else "tick", TextToSpeech.QUEUE_FLUSH, null, "metronome-preview")
            }) { Text("Preview") }
        }

        Divider()

        // --- Voice (TTS) ---
        Text("Voice (TTS)", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Switch(checked = voiceEnabled, onCheckedChange = { voiceEnabled = it; Prefs.setVoiceEnabled(ctx, it) })
            Text(if (voiceEnabled) "Enabled" else "Disabled")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Voice:")
            var expanded by remember { mutableStateOf(false) }
            val label = availableVoices.firstOrNull { it.name == selectedVoiceName }?.name ?: "default"
            OutlinedButton(onClick = { expanded = true }) { Text(label) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("default") }, onClick = {
                    selectedVoiceName = "default"; Prefs.setTtsVoiceName(ctx, "default"); expanded = false
                })
                availableVoices.forEach { v ->
                    DropdownMenuItem(text = { Text(v.name) }, onClick = {
                        selectedVoiceName = v.name
                        Prefs.setTtsVoiceName(ctx, v.name)
                        tts?.voice = v
                        expanded = false
                    })
                }
            }
            OutlinedButton(onClick = {
                tts?.voice = availableVoices.firstOrNull { it.name == selectedVoiceName } ?: tts?.voice
                tts?.speak("This is a preview.", TextToSpeech.QUEUE_FLUSH, null, "tts-preview")
            }) { Text("Preview") }
        }

        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }

    if (showNewStyleDialog) {
        AlertDialog(
            onDismissRequest = { showNewStyleDialog = false },
            title = { Text("Create new style") },
            text = {
                OutlinedTextField(value = newStyleName, onValueChange = { newStyleName = it }, label = { Text("Style name") })
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = newStyleName.trim().ifEmpty { "New Style" }
                    Storage.createStyle(ctx, name)
                    Prefs.setStyle(ctx, name)
                    currentStyle = name
                    styles = Storage.listStyles(ctx)
                    newStyleName = ""
                    showNewStyleDialog = false
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showNewStyleDialog = false }) { Text("Cancel") } }
        )
    }
}
