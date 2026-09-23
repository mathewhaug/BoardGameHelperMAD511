package com.mathewhaug.boardgamehelper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathewhaug.boardgamehelper.data.Game
import com.mathewhaug.boardgamehelper.data.SampleGames
import com.mathewhaug.boardgamehelper.ui.theme.StClairTheme

// Takes a Game, not an id - looking the game up by id is App's job, since App is the only place
// that holds the list of games and knows how the id from the back stack maps to one of them.
// This screen just renders whatever Game it is handed, so onBack is the one thing it exposes -
// the toolbar arrow and the system Back button both end up calling it, so they always agree
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(game: Game, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(game.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("${game.minPlayers} to ${game.maxPlayers} players")
            Text("${game.playTimeMinutes} min play time")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameDetailScreenPreview() {
    StClairTheme {
        GameDetailScreen(game = SampleGames.first(), onBack = {})
    }
}
