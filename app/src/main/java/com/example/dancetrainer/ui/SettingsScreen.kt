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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.dancetrainer.data.Prefs
import com.example.dancetrainer.data.Storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    var treeUri by remember { mutableStateOf(Prefs.getTreeUri(ctx)) }
    var styles by remember { mutableStateOf(Storage.listStyles(ctx)) }
    var selectedStyle by remember {
        mutableStateOf(
            Prefs.getStyle(ctx).takeIf { it.isNotBlank() } ?: styles.firstOrNull().orEmpty()
        )
    }
    var ttsEnabled by remember { mutableStateOf(Prefs.isVoiceEnabled(ctx)) }

    // Folder picker for root
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                ctx.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) {
            }
            val s = uri.toString()
            treeUri = s
            Prefs.setTreeUri(ctx, s)
            styles = Storage.listStyles(ctx)
            Toast.makeText(ctx, "Root folder set.", Toast.LENGTH_SHORT).show()
        }
    }

    // Whenever root or styles change, ensure selectedStyle is valid
    LaunchedEffect(treeUri) {
        styles = Storage.listStyles(ctx)
        if (styles.isNotEmpty()) {
            if (selectedStyle.isBlank() || selectedStyle !in styles) {
                selectedStyle = styles.first()
                Prefs.setStyle(ctx, selectedStyle)
                Storage.ensureFilesForStyle(ctx, selectedStyle)
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
            // Root folder card
            Card(
                modifier = Modifier.fill
