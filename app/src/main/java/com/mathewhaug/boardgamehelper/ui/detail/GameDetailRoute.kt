package com.mathewhaug.boardgamehelper.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mathewhaug.boardgamehelper.di.BoardGameApp

// this factory cannot be a top level val like HomeViewModelFactory, because it has to capture the
// gameId that only this Route knows, so it is built inline. the app is pulled from LocalContext
// here instead of APPLICATION_KEY just to show that both roads lead to the same container
//
// the ViewModel is scoped to this back stack entry, not the Activity, so GameDetail(13) and
// GameDetail(42) sitting on the stack together each get their own instance with their own id
@Composable
fun GameDetailRoute(gameId: Int, onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as BoardGameApp
    val viewModel: GameDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer { GameDetailViewModel(gameId, app.container.repository) }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GameDetailScreen(
        uiState = uiState,
        onToggleShortlist = viewModel::toggleShortlist,
        onBack = onBack,
    )
}
