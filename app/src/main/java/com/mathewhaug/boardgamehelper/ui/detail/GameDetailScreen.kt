package com.mathewhaug.boardgamehelper.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathewhaug.boardgamehelper.data.SampleGames
import com.mathewhaug.boardgamehelper.ui.theme.StClairTheme

// Takes a GameDetailUiState, not a Game and not an id. the lookup that used to happen in App.kt
// now happens in the ViewModel, and this screen just renders whichever of the three states it is
// handed. onBack is still the one navigation thing it exposes - the toolbar arrow and the system
// Back button both end up calling it, so they always agree
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    uiState: GameDetailUiState,
    onToggleShortlist: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                // the title comes from the state too, so the bar is honest while the game loads
                title = {
                    Text(
                        when (uiState) {
                            GameDetailUiState.Loading -> "Loading"
                            is GameDetailUiState.NotFound -> "Game not found"
                            is GameDetailUiState.Ready -> uiState.game.name
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        // an exhaustive when over the sealed interface: add a fourth state to GameDetailUiState
        // and this stops compiling until it is drawn, which is the whole reason it is sealed
        when (uiState) {
            GameDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is GameDetailUiState.NotFound -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No game with id ${uiState.id}")
                }
            }
            is GameDetailUiState.Ready -> {
                Column(
                    modifier = Modifier.padding(innerPadding).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("${uiState.game.minPlayers} to ${uiState.game.maxPlayers} players")
                    Text("${uiState.game.playTimeMinutes} min play time")
                    // the label comes from the state, not from a remembered toggle: tap it and the
                    // repository changes, the ViewModel emits a new Ready, and the label follows
                    Button(onClick = onToggleShortlist) {
                        Text(if (uiState.isShortlisted) "Remove from shortlist" else "Add to shortlist")
                    }
                }
            }
        }
    }
}

// one preview per state, which is the other payoff of a sealed UiState: every branch of the when
// can be seen in the IDE without a device, a repository or a ViewModel
@Preview(showBackground = true)
@Composable
fun GameDetailScreenReadyPreview() {
    StClairTheme {
        GameDetailScreen(
            uiState = GameDetailUiState.Ready(game = SampleGames.first(), isShortlisted = true),
            onToggleShortlist = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameDetailScreenLoadingPreview() {
    StClairTheme {
        GameDetailScreen(uiState = GameDetailUiState.Loading, onToggleShortlist = {}, onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun GameDetailScreenNotFoundPreview() {
    StClairTheme {
        GameDetailScreen(uiState = GameDetailUiState.NotFound(id = 42), onToggleShortlist = {}, onBack = {})
    }
}
