package com.example.dancetrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.example.dancetrainer.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNav()
            }
        }
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "menu") {
        composable("menu") { MainMenuScreen(
            onManageMoves = { nav.navigate("manage") },
            onDance = { nav.navigate("dance") },
            onSettings = { nav.navigate("settings") },
            onSequences = { nav.navigate("sequences") },
            onGraph = { nav.navigate("graph") }
        )}
        composable("manage") { ManageMovesScreen(onBack = { nav.popBackStack() }) }
        composable("dance") { DanceScreen(onBack = { nav.popBackStack() }) }
        composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
        composable("sequences") { SequencesScreen(onBack = { nav.popBackStack() }) }
        composable("graph") { GraphScreen(onBack = { nav.popBackStack() }) }
    }
}
