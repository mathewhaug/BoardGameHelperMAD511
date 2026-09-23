package com.mathewhaug.boardgamehelper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathewhaug.boardgamehelper.data.Game
import com.mathewhaug.boardgamehelper.data.SampleGames
import com.mathewhaug.boardgamehelper.ui.theme.StClairTheme

// This screen never sees the back stack, it only reports what the user did through onGameClick
// and onSignOut. That is what lets it preview with no app running and no NavDisplay in sight -
// App.kt is the only piece of code that has to know navigation exists at all
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    games: List<Game>,
    onGameClick: (Int) -> Unit,
    onSignOut: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Board Game Helper") },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text("Sign out")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // key gives each row a stable identity tied to the game, not its position in the
            // list, so remembered state (scroll position, a future expanded/favourited flag)
            // follows the item instead of the slot - the same call site identity idea from Week 2
            items(games, key = { it.id }) { game ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onGameClick(game.id) }
                ) {
                    ListItem(
                        headlineContent = { Text(game.name) },
                        supportingContent = {
                            Text("${game.minPlayers} to ${game.maxPlayers} players, ${game.playTimeMinutes} min")
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    StClairTheme {
        HomeScreen(games = SampleGames, onGameClick = {}, onSignOut = {})
    }
}
