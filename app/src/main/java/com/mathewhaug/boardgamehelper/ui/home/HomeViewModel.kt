package com.mathewhaug.boardgamehelper.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathewhaug.boardgamehelper.data.AppError
import com.mathewhaug.boardgamehelper.data.AppException
import com.mathewhaug.boardgamehelper.data.Game
import com.mathewhaug.boardgamehelper.data.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// the repository arrives through the constructor typed as the interface, never as
// FakeGameRepository. this class has no idea which implementation it was handed, which is exactly
// what lets the unit test pass it a fake and the app pass it whatever hilt has bound
@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: GameRepository) : ViewModel() {

    // the one thing the repository cannot tell us: whether a refresh is running right now and how
    // the last one went. private and only written in here, the screen sees it through uiState.
    // it starts as refreshing because init has not decided yet whether to fetch, and showing
    // Loading for that moment is honest, where Empty would flash "no games" at a user who has some
    private data class Status(val refreshing: Boolean = true, val error: AppError? = null)

    private val status = MutableStateFlow(Status())

    // no _uiState, because nothing writes uiState directly. it is derived from the cached games,
    // the shortlist and the status, so whenever any of them changes the state is rebuilt for us.
    // the order of the when is the "cached data wins" rule from HomeUiState
    //
    // WhileSubscribed(5_000) keeps the upstream collection alive for five seconds after the last
    // collector leaves, which is long enough to cover a rotation, so the combine is not torn down
    // and restarted every time the Activity is recreated. it does stop when the app goes to the
    // background for real
    val uiState: StateFlow<HomeUiState> =
        combine(repository.games, repository.shortlist, status) { games, shortlist, status ->
            when {
                games.isNotEmpty() -> HomeUiState.Success(
                    games = games,
                    shortlistIds = shortlist.map { it.id }.toSet(),
                    refreshing = status.refreshing,
                    notice = status.error,
                )
                status.refreshing -> HomeUiState.Loading
                status.error != null -> HomeUiState.Error(status.error)
                else -> HomeUiState.Empty
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    // BGG's hot list does not change by the minute, so a cache younger than an hour is shown as is
    // and the app does not spend a network call (and BGG's patience) on every launch
    init {
        viewModelScope.launch {
            val oldest = repository.oldestFetch()
            if (oldest == null || System.currentTimeMillis() - oldest > STALE_AFTER_MS) {
                load()
            } else {
                status.value = Status(refreshing = false)
            }
        }
    }

    // pull to refresh and the Retry button both land here. a second pull while one is already
    // running is dropped, so two refreshes never race each other into BGG's rate limit
    fun refresh() {
        if (status.value.refreshing) return
        viewModelScope.launch { load() }
    }

    // the snackbar calls this once it has been shown, so the same notice does not pop up again
    // after a rotation
    fun onNoticeShown() = status.update { it.copy(error = null) }

    // the repository never throws, it hands back a Result, so a failure is just a value to store.
    // safeApiCall always wraps it in an AppException, BadData is only there to satisfy the type
    private suspend fun load() {
        status.value = Status(refreshing = true)
        val failure = repository.refresh().exceptionOrNull()
        status.value = Status(
            refreshing = false,
            error = failure?.let { (it as? AppException)?.error ?: AppError.BadData },
        )
    }

    // launched in viewModelScope so the suspend call has a coroutine to run in and is cancelled
    // for free if the ViewModel is cleared part way through. nothing is written to uiState here,
    // the repository changes, its Flow emits, and the combine above picks that up on its own.
    // only Success has hearts on screen, so in any other state there is nothing to toggle
    fun toggleShortlist(game: Game) = viewModelScope.launch {
        val current = uiState.value as? HomeUiState.Success ?: return@launch
        if (game.id in current.shortlistIds) {
            repository.removeFromShortlist(game)
        } else {
            repository.addToShortlist(game)
        }
    }

    private companion object {
        const val STALE_AFTER_MS = 60 * 60 * 1000L
    }
}
