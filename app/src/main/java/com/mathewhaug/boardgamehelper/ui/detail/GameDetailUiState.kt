package com.mathewhaug.boardgamehelper.ui.detail

import com.mathewhaug.boardgamehelper.data.Game

// the detail screen is in exactly one of three situations, and they are mutually exclusive: still
// waiting for the first emission, asked for an id no game has, or holding a game to draw. a sealed
// interface models that directly, so the when in the screen has to handle every case or it will
// not compile. a nullable Game plus an isLoading flag would let a fourth, impossible combination
// (loading and found at the same time) slip through and have to be handled anyway
sealed interface GameDetailUiState {
    data object Loading : GameDetailUiState
    data class NotFound(val id: Int) : GameDetailUiState
    data class Ready(val game: Game, val isShortlisted: Boolean) : GameDetailUiState
}
