package com.example.dancetrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.dancetrainer.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    var currentScreen = remember { mutableStateOf("menu") }

    when (currentScreen.value) {
        "menu" -> MainMenu(onNavigate = { currentScreen.value = it })
        "settings" -> SettingsScreen(onBack = { currentScreen.value = "menu" })
        "manage_moves" -> ManageMovesScreen(onBack = { currentScreen.value = "menu" })
        "connection_finder" -> ConnectionFinderScreen(onBack = { currentScreen.value = "menu" })
        "dance" -> DanceScreen(onBack = { currentScreen.value = "menu" })
        "graph" -> GraphScreen(onBack = { currentScreen.value = "menu" })
        "sequences" -> SequencesScreen(onBack = { currentScreen.value = "menu" })
        else -> MainMenu(onNavigate = { currentScreen.value = it })
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    App()
}
