package com.mathewhaug.boardgamehelper.ui.home

import com.mathewhaug.boardgamehelper.data.AppError
import com.mathewhaug.boardgamehelper.data.Game

// Home is now in exactly one of four situations, and the ViewModel picks one with a simple rule:
// cached data wins. if there are games, it is Success, even while a refresh runs or after one
// failed, because a list the user can read beats a spinner or an error page every time
sealed interface HomeUiState {
    // nothing cached yet and a refresh is on its way
    data object Loading : HomeUiState

    // the refresh worked and still came back with nothing
    data object Empty : HomeUiState

    // nothing cached and the refresh failed, so there is nothing to show but the reason
    data class Error(val error: AppError) : HomeUiState

    // shortlistIds is a Set<Int> rather than a List<Game>, because the only question the screen
    // asks is "is this row shortlisted", and a set answers that in one look. refreshing drives the
    // pull to refresh spinner, and notice is a failed refresh worth a snackbar but not a whole
    // error screen, because the cached list is still perfectly good
    data class Success(
        val games: List<Game>,
        val shortlistIds: Set<Int> = emptySet(),
        val refreshing: Boolean = false,
        val notice: AppError? = null,
    ) : HomeUiState
}

// the game night row lives beside the game list rather than inside one of the four states above,
// because the timer keeps running whether the list is loading, empty, failed or showing games.
// elapsedSeconds is null when no timer is running. lowBattery rides along here for the same
// reason, it is about the phone, not about which state the list is in
data class GameNightUiState(
    val elapsedSeconds: Long? = null,
    val lowBattery: Boolean = false,
)
