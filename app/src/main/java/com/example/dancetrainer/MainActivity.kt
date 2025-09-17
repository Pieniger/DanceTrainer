package com.example.dancetrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.dancetrainer.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppRoot()
            }
        }
    }
}

private enum class Screen {
    MENU, MANAGE, DANCE, SETTINGS, SEQUENCES, GRAPH
}

@Composable
private fun AppRoot() {
    var screen by remember { mutableStateOf(Screen.MENU) }

    when (screen) {
        Screen.MENU -> MainMenuScreen(
            onManageMoves = { screen = Screen.MANAGE },
            onDance = { screen = Screen.DANCE },
            onSettings = { screen = Screen.SETTINGS },
            onSequences = { screen = Screen.SEQUENCES },
            onGraph = { screen = Screen.GRAPH }
        )
        Screen.MANAGE -> ManageMovesScreen(onBack = { screen = Screen.MENU })
        Screen.DANCE -> DanceScreen(onBack = { screen = Screen.MENU })
        Screen.SETTINGS -> SettingsScreen(onBack = { screen = Screen.MENU })
        Screen.SEQUENCES -> SequencesScreen(onBack = { screen = Screen.MENU })
        Screen.GRAPH -> GraphScreen(onBack = { screen = Screen.MENU })
    }
}
