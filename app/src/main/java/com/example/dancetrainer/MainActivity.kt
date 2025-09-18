package com.example.dancetrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dancetrainer.ui.*
import com.example.dancetrainer.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    AppTheme {
        val nav = rememberNavController()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dance Trainer", color = MaterialTheme.colorScheme.onPrimaryContainer) }
                )
            }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = "home",
                modifier = Modifier.padding(padding)
            ) {
                composable("home") {
                    HomeScreen(
                        onManageMoves = { nav.navigate("manage") },
                        onConnectionFinder = { nav.navigate("finder") },
                        onDance = { nav.navigate("dance") },
                        onSequences = { nav.navigate("sequences") },
                        onGraph = { nav.navigate("graph") },
                        onSettings = { nav.navigate("settings") }
                    )
                }

                composable("manage") {
                    ManageMovesScreen(
                        onBack = { nav.popBackStack() },
                        onFindConnectionForMove = { moveId ->
                            nav.navigate("finder?startId=$moveId")
                        }
                    )
                }

                // Optional startId param to start from a specific move
                composable(
                    route = "finder?startId={startId}",
                    arguments = listOf(
                        navArgument("startId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val startId = backStackEntry.arguments?.getString("startId")
                    ConnectionFinderScreen(
                        startMoveId = startId,
                        onBack = { nav.popBackStack() }
                    )
                }

                composable("dance") { DanceScreen(onBack = { nav.popBackStack() }) }
                composable("sequences") { SequencesScreen(onBack = { nav.popBackStack() }) }
                composable("graph") { GraphScreen(onBack = { nav.popBackStack() }) }
                composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
            }
        }
    }
}
