package com.mathewhaug.boardgamehelper.data

import kotlinx.coroutines.flow.Flow

// written entirely in the app's own types - Game and List<Game> - with nothing in it that hints
// at where the data lives. Room, Retrofit or an in memory list all look identical from here, and
// that is the point: the ViewModels depend on this interface and never on a concrete class, so
// swapping the data source later never touches a screen or a ViewModel
//
// two shapes on purpose. anything the UI watches is a Flow, so the screen updates itself when the
// data changes underneath it, and anything that is a one shot operation is a suspend function, so
// the caller waits for it inside a coroutine instead of blocking the main thread
interface GameRepository {
    val games: Flow<List<Game>>
    val shortlist: Flow<List<Game>>
    suspend fun addToShortlist(game: Game)
    suspend fun removeFromShortlist(game: Game)
}
