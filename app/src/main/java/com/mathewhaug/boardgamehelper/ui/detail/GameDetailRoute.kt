package com.mathewhaug.boardgamehelper.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// gameId is the one thing hilt cannot know, so creationCallback hands it to the assisted factory,
// and the callback only runs when this entry has no GameDetailViewModel yet, so a rotation gets the
// stored instance back and create() is not called again
//
// the ViewModel is scoped to this back stack entry, not the Activity, so GameDetail(13) and
// GameDetail(42) sitting on the stack together each get their own instance with their own id
@Composable
fun GameDetailRoute(gameId: Int, onBack: () -> Unit) {
    val viewModel = hiltViewModel<GameDetailViewModel, GameDetailViewModel.Factory>(
        creationCallback = { it.create(gameId) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GameDetailScreen(
        uiState = uiState,
        onToggleShortlist = viewModel::toggleShortlist,
        onBack = onBack,
    )
}
