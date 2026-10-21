package com.mathewhaug.boardgamehelper.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// hiltViewModel() replaces the hand written factory. hilt already knows how to build a
// HomeViewModel from its @Inject constructor, and it still scopes the instance to this back stack
// entry through the ViewModelStore the nav entry decorator gives it
// the Route collects the ViewModel's state and hands plain values down. HomeScreen takes a
// HomeUiState and lambdas, never a ViewModel, which is what keeps it previewable
@Composable
fun HomeRoute(
    onGameClick: (Int) -> Unit,
    onOpenShortlist: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // collectAsStateWithLifecycle stops collecting when the Activity drops below STARTED and picks
    // up again when it comes back, where the older collectAsState() keeps collecting in the
    // background the whole time. paired with WhileSubscribed in the ViewModel, that is what lets
    // the repository query actually go idle while the app is not on screen
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onGameClick = onGameClick,
        onToggleShortlist = viewModel::toggleShortlist,
        onOpenShortlist = onOpenShortlist,
        onSignOut = onSignOut,
    )
}
