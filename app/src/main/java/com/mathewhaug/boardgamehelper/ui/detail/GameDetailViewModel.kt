package com.mathewhaug.boardgamehelper.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathewhaug.boardgamehelper.data.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// the id arrives through the constructor already typed as an Int, because the GameDetail
// navigation key carried it typed: no SavedStateHandle string lookup, no parsing, no default to
// invent. the lookup that used to sit in App.kt moved in here, next to the repository it depends
// on, which is where it belonged all along
class GameDetailViewModel(
    private val gameId: Int,
    private val repository: GameRepository
) : ViewModel() {

    // same shape as Home, derived from the repository with no backing property. Loading is the
    // value stateIn hands out before the first combine lands, so the screen has something honest
    // to show for that first frame instead of a flash of NotFound
    val uiState: StateFlow<GameDetailUiState> =
        combine(repository.games, repository.shortlist) { games, shortlist ->
            val game = games.firstOrNull { it.id == gameId }
            if (game == null) {
                GameDetailUiState.NotFound(gameId)
            } else {
                GameDetailUiState.Ready(game, isShortlisted = shortlist.any { it.id == gameId })
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameDetailUiState.Loading)

    // reads the current state rather than taking a parameter, since there is only ever one game on
    // this screen and the caller has nothing to add. if the state is not Ready yet there is nothing
    // to toggle and the tap is quietly ignored
    fun toggleShortlist() = viewModelScope.launch {
        val current = uiState.value as? GameDetailUiState.Ready ?: return@launch
        if (current.isShortlisted) {
            repository.removeFromShortlist(current.game)
        } else {
            repository.addToShortlist(current.game)
        }
    }
}
