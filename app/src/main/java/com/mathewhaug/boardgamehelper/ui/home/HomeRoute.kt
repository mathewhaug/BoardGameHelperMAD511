package com.mathewhaug.boardgamehelper.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mathewhaug.boardgamehelper.di.BoardGameApp

// viewModel() can only call a no argument constructor on its own, and HomeViewModel needs a
// repository, so a factory is how a ViewModel with parameters gets built. the factory reaches the
// container through APPLICATION_KEY, which the lifecycle library drops into every initializer's
// CreationExtras, so no Context has to be threaded through by hand
//
// this is the one file under ui/home that imports from di/. the Route is the seam between the
// wiring and the screen, so the ViewModel and the Screen both stay free of it
val HomeViewModelFactory = viewModelFactory {
    initializer {
        val app = this[APPLICATION_KEY] as BoardGameApp
        HomeViewModel(app.container.repository)
    }
}

// the Route collects the ViewModel's state and hands plain values down. HomeScreen takes a
// HomeUiState and lambdas, never a ViewModel, which is what keeps it previewable
@Composable
fun HomeRoute(
    onGameClick: (Int) -> Unit,
    onOpenShortlist: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory)
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
