package com.mathewhaug.boardgamehelper.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathewhaug.boardgamehelper.data.Game
import com.mathewhaug.boardgamehelper.data.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// the repository arrives through the constructor typed as the interface, never as
// FakeGameRepository. this class has no idea which implementation it was handed, which is exactly
// what lets the unit test pass it a fake and the app pass it whatever the container builds
class HomeViewModel(private val repository: GameRepository) : ViewModel() {

    // no backing property and no _uiState here, because nothing in this class ever writes to
    // uiState directly. it is derived from two repository Flows, so whenever either one emits the
    // combined state is rebuilt for us: the write path is toggleShortlist below, the read path is
    // this property, and the two only ever meet inside the repository
    //
    // WhileSubscribed(5_000) keeps the upstream collection alive for five seconds after the last
    // collector leaves, which is long enough to cover a rotation, so the combine is not torn down
    // and restarted (and the list does not flicker through its empty default) every time the
    // Activity is recreated. it does stop when the app goes to the background for real
    val uiState: StateFlow<HomeUiState> =
        combine(repository.games, repository.shortlist) { games, shortlist ->
            HomeUiState(games = games, shortlistIds = shortlist.map { it.id }.toSet())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    // launched in viewModelScope so the suspend call has a coroutine to run in and is cancelled
    // for free if the ViewModel is cleared part way through. nothing is written to uiState here -
    // the repository changes, its Flow emits, and the combine above picks that up on its own
    fun toggleShortlist(game: Game) = viewModelScope.launch {
        if (game.id in uiState.value.shortlistIds) {
            repository.removeFromShortlist(game)
        } else {
            repository.addToShortlist(game)
        }
    }
}
