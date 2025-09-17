package com.example.dancetrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.example.dancetrainer.ui.ConnectionFinderScreen
import com.example.dancetrainer.ui.HomeScreen
import com.example.dancetrainer.ui.ManageMovesScreen
import com.example.dancetrainer.ui.SettingsScreen

/**
 * Very small in-memory app state.
 * If you already have persistent Storage, you can wire this through there later;
 * for now it unblocks UI + navigation and the "Find Connection" deep-link.
 */
data class Move(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    var beats: Int = 4
)

class AppState {
    val moves = mutableStateListOf<Move>()
    fun addMove(name: String, beats: Int) { moves += Move(name = name, beats = beats) }
    fun updateMove(id: String, name: String, beats: Int) {
        moves.indexOfFirst { it.id == id }.takeIf { it >= 0 }?.let { idx ->
            moves[idx] = moves[idx].copy(name = name, beats = beats)
        }
    }
    fun deleteMove(id: String) {
        moves.removeAll { it.id == id }
        // TODO: if you have connections/sequences storage, clean references here.
    }
    fun getMove(id: String): Move? = moves.firstOrNull { it.id == id }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = onBack?.let {
                    {
                        IconButton(onClick = it) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                    }
                }
            )
        }
    ) { padding -> content(padding) }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme()
            ) {
                val nav = rememberNavController()
                val app = remember { AppState() }

                NavHost(
                    navController = nav,
                    startDestination = "home"
                ) {
                    composable("home") {
                        AppScaffold("DanceTrainer") {
                            HomeScreen(
                                padding = it,
                                onManageMoves = { nav.navigate("manageMoves") },
                                onConnectionFinder = { nav.navigate("connectionFinder") },
                                onSettings = { nav.navigate("settings") },
                                onDance = { /* TODO: hook Dance screen here */ },
                                onGraph = { /* TODO */ },
                                onSequences = { /* TODO */ }
                            )
                        }
                    }

                    composable("manageMoves") {
                        AppScaffold("Manage Moves", onBack = { nav.popBackStack() }) { pad ->
                            ManageMovesScreen(
                                padding = pad,
                                moves = app.moves,
                                onAdd = { name, beats -> app.addMove(name, beats) },
                                onEdit = { id, name, beats -> app.updateMove(id, name, beats) },
                                onDelete = { id -> app.deleteMove(id) },
                                onFindConnection = { startId ->
                                    nav.navigate("connectionFinder?startMoveId=$startId")
                                }
                            )
                        }
                    }

                    composable(
                        route = "connectionFinder?startMoveId={startMoveId}",
                        arguments = listOf(
                            navArgument("startMoveId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStack ->
                        val startId = backStack.arguments?.getString("startMoveId")
                        AppScaffold("Connection Finder", onBack = { nav.popBackStack() }) { pad ->
                            ConnectionFinderScreen(
                                padding = pad,
                                allMoves = app.moves,
                                startMoveId = startId
                                // TODO: pass/save connection decisions to your storage here
                            )
                        }
                    }

                    composable("settings") {
                        AppScaffold("Settings", onBack = { nav.popBackStack() }) { pad ->
                            SettingsScreen(padding = pad)
                        }
                    }
                }
            }
        }
    }
}
