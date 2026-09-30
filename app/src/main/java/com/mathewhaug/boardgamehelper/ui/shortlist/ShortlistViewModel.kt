package com.mathewhaug.boardgamehelper.ui.shortlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathewhaug.boardgamehelper.data.Game
import com.mathewhaug.boardgamehelper.data.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// the smallest ViewModel in the app: one Flow from the repository turned into a StateFlow, and one
// function that writes back. there is no UiState class here because the state really is just a
// List<Game>, and wrapping it would add a type without adding a fact
class ShortlistViewModel(private val repository: GameRepository) : ViewModel() {

    // stateIn is what turns a repository Flow into something a screen can read right now, and the
    // emptyList() is the value it hands out before the repository has emitted anything
    val shortlist: StateFlow<List<Game>> = repository.shortlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun remove(game: Game) = viewModelScope.launch { repository.removeFromShortlist(game) }
}
