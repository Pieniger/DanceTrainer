package com.example.dancetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainMenu(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { onNavigate("dance") }, modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Text("Dance Mode")
        }
        Button(onClick = { onNavigate("connection_finder") }, modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Text("Connection Finder")
        }
        Button(onClick = { onNavigate("manage_moves") }, modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Text("Manage Moves")
        }
        Button(onClick = { onNavigate("graph") }, modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Text("Graph View")
        }
        Button(onClick = { onNavigate("sequences") }, modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Text("Sequences")
        }
        Button(onClick = { onNavigate("settings") }, modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Text("Settings")
        }
    }
}
