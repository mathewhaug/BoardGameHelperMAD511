package com.mathewhaug.boardgamehelper.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// an in memory GameRepository: the whole store is two StateFlows and nothing survives the process.
// this is also the version the previews and the unit tests use, which is why it lives in data/
// next to the interface rather than in a test folder - production, preview and test all build
// against the same fake until Room shows up in a later week
class FakeGameRepository : GameRepository {

    // games never changes in the fake, but the interface promises a Flow so a real repository can
    // push updates later, and MutableStateFlow(SampleGames) is the cheapest way to honour that
    override val games: Flow<List<Game>> = MutableStateFlow(SampleGames)

    // the backing property idiom, one layer below the ViewModel: the private mutable flow is the
    // only thing this class writes to, and asStateFlow() hands out a read only view of it, so no
    // caller can ever emit into the shortlist from the outside
    private val _shortlist = MutableStateFlow<List<Game>>(emptyList())
    override val shortlist: StateFlow<List<Game>> = _shortlist.asStateFlow()

    // update { } reads the current list and writes the new one as one atomic step, so two quick
    // taps cannot race each other into adding the same game twice
    override suspend fun addToShortlist(game: Game) {
        _shortlist.update { current -> if (game in current) current else current + game }
    }

    override suspend fun removeFromShortlist(game: Game) {
        _shortlist.update { current -> current - game }
    }
}
