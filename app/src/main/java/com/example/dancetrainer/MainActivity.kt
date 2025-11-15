package com.example.dancetrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.dancetrainer.ui.ConnectionFinderScreen
import com.example.dancetrainer.ui.DanceScreen
import com.example.dancetrainer.ui.GraphScreen
import com.example.dancetrainer.ui.HomeScreen
import com.example.dancetrainer.ui.ManageMovesScreen
import com.example.dancetrainer.ui.SequencesScreen
import com.example.dancetrainer.ui.SettingsScreen
import com.example.dancetrainer.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    AppTheme {
        val nav = rememberNavController()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dance Trainer") }
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

                composable("dance") {
                    DanceScreen(onBack = { nav.popBackStack() })
                }

                composable("sequences") {
                    SequencesScreen(onBack = { nav.popBackStack() })
                }

                composable("graph") {
                    GraphScreen(onBack = { nav.popBackStack() })
                }

                composable("settings") {
                    SettingsScreen(onBack = { nav.popBackStack() })
                }
            }
        }
    }
}
