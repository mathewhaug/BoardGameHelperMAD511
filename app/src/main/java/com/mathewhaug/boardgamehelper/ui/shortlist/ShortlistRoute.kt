package com.mathewhaug.boardgamehelper.ui.shortlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mathewhaug.boardgamehelper.di.BoardGameApp

// same shape as HomeViewModelFactory. the factory is the only piece that knows the container
// exists, so it sits in the Route file next to the one composable that needs it
val ShortlistViewModelFactory = viewModelFactory {
    initializer {
        val app = this[APPLICATION_KEY] as BoardGameApp
        ShortlistViewModel(app.container.repository)
    }
}

@Composable
fun ShortlistRoute(
    onBack: () -> Unit,
    viewModel: ShortlistViewModel = viewModel(factory = ShortlistViewModelFactory)
) {
    // results replaces shortlist as the list on screen. with an empty query it is the whole
    // shortlist anyway, so the screen never has to know which of the two it is showing
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()

    ShortlistScreen(
        query = query,
        onQueryChange = viewModel::onQueryChange,
        games = results,
        onRemove = viewModel::remove,
        onBack = onBack,
    )
}
