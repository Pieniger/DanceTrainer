package com.example.dancetrainer.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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

private object PrefsKeys {
    const val PREFS = "settings"
    const val METRONOME_ON = "metronome_on"
    const val SOUND = "metronome_sound"
    const val TTS_SPEED = "tts_speed"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences(PrefsKeys.PREFS, Context.MODE_PRIVATE) }

    var metronomeOn by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.METRONOME_ON, true)) }
    var metronomeSound by remember { mutableStateOf(prefs.getString(PrefsKeys.SOUND, "Click") ?: "Click") }
    var ttsSpeed by remember { mutableStateOf(prefs.getString(PrefsKeys.TTS_SPEED, "Default") ?: "Default") }

    fun saveSound(value: String) {
        metronomeSound = value
        prefs.edit().putString(PrefsKeys.SOUND, value).apply()
    }

    fun saveSpeed(value: String) {
        ttsSpeed = value
        prefs.edit().putString(PrefsKeys.TTS_SPEED, value).apply()
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

            // Metronome sound selection (simple buttons)
            Text("Metronome Sound", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Click", "Bell").forEach { option ->
                    Button(onClick = { saveSound(option) }) {
                        Text(
                            if (option == metronomeSound) "$option (current)" else option
                        )
                    }
                }
            }

            // TTS Speed selection
            Text("TTS Speed", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Default", "Slow", "Fast").forEach { option ->
                    Button(onClick = { saveSpeed(option) }) {
                        Text(
                            if (option == ttsSpeed) "$option (current)" else option
                        )
                    }
                }
            }

            Text(
                "More advanced settings (folder selection, TTS preview, etc.) " +
                        "can be added later once the core app is stable.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
