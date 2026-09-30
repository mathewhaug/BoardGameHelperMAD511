package com.mathewhaug.boardgamehelper.ui.home

import com.mathewhaug.boardgamehelper.data.Game

// one immutable object that describes everything the Home screen needs to draw itself. the
// screen reads it and never writes it, and because it is a data class with defaults a preview can
// build one with a literal and a test can compare two of them with ==
//
// shortlistIds is a Set<Int> rather than the List<Game> the repository hands out, because the only
// question the screen ever asks is "is this row shortlisted", and a set answers that in one look
data class HomeUiState(
    val games: List<Game> = emptyList(),
    val shortlistIds: Set<Int> = emptySet(),
)
