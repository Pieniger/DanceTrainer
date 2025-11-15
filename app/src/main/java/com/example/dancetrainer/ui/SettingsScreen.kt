package com.example.dancetrainer.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dancetrainer.data.Prefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    var voiceEnabled by remember { mutableStateOf(Prefs.isVoiceEnabled(ctx)) }
    var treeUri by remember { mutableStateOf(Prefs.getTreeUri(ctx)) }

    var nextKeyText by remember {
        mutableStateOf(Prefs.getNextMoveKeyCode(ctx)?.toString() ?: "")
    }
    var rerollKeyText by remember {
        mutableStateOf(Prefs.getRerollKeyCode(ctx)?.toString() ?: "")
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                ctx.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) {
            }
            treeUri = uri.toString()
            Prefs.setTreeUri(ctx, treeUri)
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

            // ---- TTS toggle ----
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = voiceEnabled,
                    onCheckedChange = {
                        voiceEnabled = it
                        Prefs.setVoiceEnabled(ctx, it)
                    }
                )
                Text("Voice announcements (TTS)")
            }

            // ---- Data folder selection ----
            ElevatedCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Data folder")
                    Text(
                        treeUri ?: "No folder selected yet. " +
                                "Choose a folder that contains your dance-style subfolders."
                    )
                    Button(onClick = { folderPicker.launch(null) }) {
                        Text("Choose folder")
                    }
                }
            }

            // ---- Bluetooth / remote controls ----
            ElevatedCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Bluetooth / Remote controls")

                    Text(
                        "Enter Android key codes for your remote buttons. " +
                                "If set, those keys will trigger the buttons on the Dance screen."
                    )

                    OutlinedTextField(
                        value = nextKeyText,
                        onValueChange = { text ->
                            val digits = text.filter { it.isDigit() }
                            nextKeyText = digits
                            Prefs.setNextMoveKeyCode(ctx, digits.toIntOrNull())
                        },
                        label = { Text("Next move key code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = rerollKeyText,
                        onValueChange = { text ->
                            val digits = text.filter { it.isDigit() }
                            rerollKeyText = digits
                            Prefs.setRerollKeyCode(ctx, digits.toIntOrNull())
                        },
                        label = { Text("Reroll key code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Tip: Common keys your remote might send:\n" +
                                "- Volume Up / Down\n" +
                                "- Media Next / Previous\n" +
                                "- Play / Pause, etc.\n" +
                                "You can find the numeric key code with a simple key-test app, " +
                                "then enter it here."
                    )
                }
            }
        }
    }
}
