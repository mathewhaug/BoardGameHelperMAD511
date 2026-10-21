package com.mathewhaug.boardgamehelper.ui.shortlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// same as HomeRoute, nothing here needs to know where the repository comes from anymore
@Composable
fun ShortlistRoute(
    onBack: () -> Unit,
    viewModel: ShortlistViewModel = hiltViewModel()
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
