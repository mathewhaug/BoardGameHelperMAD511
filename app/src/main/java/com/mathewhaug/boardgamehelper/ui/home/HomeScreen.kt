package com.mathewhaug.boardgamehelper.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

// This screen never sees the back stack or the ViewModel, it only reports what the user did
// through its lambdas. Everything it draws arrives in one HomeUiState, so the whole screen can be
// put into any state you like just by building that object - that is what lets it preview with
// no app running and no NavDisplay in sight
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onGameClick: (Int) -> Unit,
    onToggleShortlist: (Game) -> Unit,
    onOpenShortlist: () -> Unit,
    onSignOut: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Board Game Helper") },
                actions = {
                    TextButton(onClick = onOpenShortlist) {
                        Text("Shortlist")
                    }
                    TextButton(onClick = onSignOut) {
                        Text("Sign out")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // key gives each row a stable identity tied to the game, not its position in the
            // list, so remembered state (scroll position, a future expanded flag) follows the
            // item instead of the slot - the same call site identity idea from Week 2
            items(uiState.games, key = { it.id }) { game ->
                // the row does not store whether it is shortlisted, it asks the state, so there
                // is no local flag that could drift out of step with the repository
                val shortlisted = game.id in uiState.shortlistIds
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
                        },
                        trailingContent = {
                            IconButton(onClick = { onToggleShortlist(game) }) {
                                Icon(
                                    imageVector = if (shortlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = if (shortlisted) "Remove from shortlist" else "Add to shortlist"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

// previewing the Screen with a UiState literal is why adding a ViewModel makes previews better,
// not worse: the preview needs no ViewModel, no repository and no container, it just says "draw
// this state", and a second preview for the empty case costs one more literal
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    StClairTheme {
        HomeScreen(
            uiState = HomeUiState(games = SampleGames, shortlistIds = setOf(SampleGames.first().id)),
            onGameClick = {},
            onToggleShortlist = {},
            onOpenShortlist = {},
            onSignOut = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenEmptyPreview() {
    StClairTheme {
        HomeScreen(
            uiState = HomeUiState(),
            onGameClick = {},
            onToggleShortlist = {},
            onOpenShortlist = {},
            onSignOut = {}
        )
    }
}
