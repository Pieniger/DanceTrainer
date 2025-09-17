package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var selectedMetronomeSound by remember { mutableStateOf("Click") }
    var selectedVoice by remember { mutableStateOf("Default") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Metronome Sound")
        DropdownMenuBox(
            items = listOf("Click", "Bell"),
            selected = selectedMetronomeSound,
            onSelect = { selectedMetronomeSound = it })

        Spacer(modifier = Modifier.height(16.dp))

        Text("Voice")
        DropdownMenuBox(
            items = listOf("Default", "Fast", "Slow"),
            selected = selectedVoice,
            onSelect = { selectedVoice = it })

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
fun DropdownMenuBox(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) { Text(selected) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    onSelect(it)
                    expanded = false
                })
            }
        }
    }
}
