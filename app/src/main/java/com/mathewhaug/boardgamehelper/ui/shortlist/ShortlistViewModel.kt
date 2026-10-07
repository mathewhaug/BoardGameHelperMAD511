package com.mathewhaug.boardgamehelper.ui.shortlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathewhaug.boardgamehelper.data.Game
import com.mathewhaug.boardgamehelper.data.GameRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
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

    // the text in the search box, held here and not in the screen so it survives rotation. same
    // backing property idiom as the fake repository: only onQueryChange writes, the screen reads
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun onQueryChange(value: String) { _query.value = value }

    // the search is a pipeline from what the user typed to what the screen draws, and every
    // operator in it is doing one job. the text field itself reads query, not results, so typing
    // never lags while this end of the pipe waits
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<Game>> = _query
        .debounce(300)  // wait for the typing to pause instead of querying on every keystroke
        .distinctUntilChanged()  // "cat", backspace, "t" lands on the same term, so skip it
        .flatMapLatest { term -> if (term.isBlank()) repository.shortlist else repository.search(term) }  // a new term cancels the query still running for the old one
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())  // keeps the query alive across a rotation
}
