package com.mathewhaug.boardgamehelper.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// an in memory GameRepository: the whole store is two StateFlows and nothing survives the process.
// this is also the version the previews and the unit tests use, which is why it lives in data/
// next to the interface rather than in a test folder - production, preview and test all build
// against the same fake until Room shows up in a later week
//
// the constructor arguments are for the ViewModel tests: which games are cached, how old the
// cache is, and what a refresh does. the defaults are the plain fake the previews always had
class FakeGameRepository(
    games: List<Game> = SampleGames,
    private val lastFetch: Long? = null,
    private val onRefresh: suspend () -> Result<Unit> = { Result.success(Unit) },
) : GameRepository {

    // games never changes in the fake, but the interface promises a Flow so a real repository can
    // push updates later, and MutableStateFlow is the cheapest way to honour that
    override val games: Flow<List<Game>> = MutableStateFlow(games)

    // the backing property idiom, one layer below the ViewModel: the private mutable flow is the
    // only thing this class writes to, and asStateFlow() hands out a read only view of it, so no
    // caller can ever emit into the shortlist from the outside
    private val _shortlist = MutableStateFlow<List<Game>>(emptyList())
    override val shortlist: StateFlow<List<Game>> = _shortlist.asStateFlow()

    // the in memory stand in for the LIKE query in ShortlistDao: same prefix rule, same case
    // insensitivity, so a preview or a test sees the same matches the real database would give
    override fun search(term: String): Flow<List<Game>> =
        _shortlist.map { games -> games.filter { it.name.startsWith(term, ignoreCase = true) } }

    // update { } reads the current list and writes the new one as one atomic step, so two quick
    // taps cannot race each other into adding the same game twice
    override suspend fun addToShortlist(game: Game) {
        _shortlist.update { current -> if (game in current) current else current + game }
    }

    override suspend fun removeFromShortlist(game: Game) {
        _shortlist.update { current -> current - game }
    }

    // counted so a test can check whether the ViewModel decided to refresh at all
    var refreshCount = 0
        private set

    override suspend fun refresh(): Result<Unit> {
        refreshCount++
        return onRefresh()
    }

    override suspend fun oldestFetch(): Long? = lastFetch
}
