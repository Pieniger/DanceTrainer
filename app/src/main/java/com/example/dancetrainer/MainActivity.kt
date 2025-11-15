package com.example.dancetrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dancetrainer.ui.ConnectionFinderScreen
import com.example.dancetrainer.ui.DanceScreen
import com.example.dancetrainer.ui.GraphScreen
import com.example.dancetrainer.ui.HomeScreen
import com.example.dancetrainer.ui.ManageMovesScreen
import com.example.dancetrainer.ui.SequenceScreen
import com.example.dancetrainer.ui.SettingsScreen
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
                TopAppBar(title = { Text("Dance Trainer") })
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
                        // We ignore the particular move for now and just open the global
                        // ConnectionFinder, as per the new spec (random pairs).
                        onFindConnectionForMove = { _ ->
                            nav.navigate("finder")
                        }
                    )
                }

                composable("finder") {
                    ConnectionFinderScreen(
                        onBack = { nav.popBackStack() }
                    )
                }

                composable("dance") {
                    DanceScreen(onBack = { nav.popBackStack() })
                }

                composable("sequences") {
                    SequenceScreen(onBack = { nav.popBackStack() })
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
