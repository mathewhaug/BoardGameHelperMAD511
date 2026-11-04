package com.mathewhaug.boardgamehelper.data

import com.mathewhaug.boardgamehelper.data.local.CachedGame
import com.mathewhaug.boardgamehelper.data.local.CatalogueDao
import com.mathewhaug.boardgamehelper.data.local.ShortlistDao
import com.mathewhaug.boardgamehelper.data.local.ShortlistedGame
import com.mathewhaug.boardgamehelper.data.remote.BggApi
import com.mathewhaug.boardgamehelper.data.remote.ThingItemDto
import com.mathewhaug.boardgamehelper.data.remote.parseHot
import com.mathewhaug.boardgamehelper.data.remote.parseThings
import com.mathewhaug.boardgamehelper.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

// the same interface FakeGameRepository implements, now backed by a real table. this class owns
// the mapping between the database's shape and the app's shape, so the DAO never hears about Game
// and the screens never hear about ShortlistedGame. nothing above this file imports androidx.room3
//
// the ViewModels do not change at all for this swap, because they depend on GameRepository and not
// on a concrete class. that is the whole point of the Week 4 interface paying off
//
// the catalogue follows one rule: the network only writes to Room, and the screen only reads
// from Room. so games is always whatever is cached, and refresh() is just a way to top it up
class RoomGameRepository @Inject constructor(
    private val api: BggApi,
    private val catalogueDao: CatalogueDao,
    private val dao: ShortlistDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : GameRepository {
    override val games: Flow<List<Game>> =
        catalogueDao.observeAll().map { rows -> rows.map { it.toGame() } }
    override val shortlist: Flow<List<Game>> =
        dao.observeAll().map { rows -> rows.map { it.toGame() } }
    override fun search(term: String): Flow<List<Game>> =
        dao.search(term).map { rows -> rows.map { it.toGame() } }
    override suspend fun addToShortlist(game: Game) = dao.upsert(game.toEntity())
    override suspend fun removeFromShortlist(game: Game) = dao.delete(game.toEntity())

    // everything inside safeApiCall, so a failure part way through comes back as an AppError and
    // whatever batches already landed stay in the cache. nothing is ever deleted here, so a failed
    // refresh can never leave the screen emptier than it was
    override suspend fun refresh(): Result<Unit> = safeApiCall {
        val hot = api.hot(TYPE_BOARDGAME)
        // parsing walks the whole document, which is real work, so it stays off the main thread
        val ids = withContext(io) { hot.use { parseHot(it.byteStream()) } }.map { it.id }

        ids.chunked(BATCH_SIZE).forEachIndexed { index, batch ->
            // BGG throttles the XML API, and asking again straight away tends to get a 429 (or a
            // 500 or 503 under load) instead of data. five seconds between batches keeps the app
            // under that limit. a suspending delay, so no thread sits blocked while it waits
            if (index > 0) delay(BATCH_DELAY_MS)
            val body = api.things(batch.joinToString(","))
            val things = withContext(io) { body.use { parseThings(it.byteStream()) } }
            // written as soon as it arrives, so the list on screen fills in batch by batch
            // instead of waiting for the slowest one
            val now = System.currentTimeMillis()
            catalogueDao.upsertAll(things.map { it.toCached(now) })
        }
    }

    override suspend fun oldestFetch(): Long? = catalogueDao.oldestFetch()

    private companion object {
        const val TYPE_BOARDGAME = "boardgame"
        const val BATCH_SIZE = 20
        const val BATCH_DELAY_MS = 5_000L
    }
}

// private and in this file, so the only code that can convert between the two shapes is the class
// that needs to. addedAt is left to its default, the delete only matches on the primary key anyway
private fun ShortlistedGame.toGame() = Game(id, name, minPlayers, maxPlayers, playTimeMinutes)
private fun Game.toEntity() = ShortlistedGame(id, name, minPlayers, maxPlayers, playTimeMinutes)

private fun CachedGame.toGame() = Game(id, name, minPlayers, maxPlayers, playTimeMinutes, thumbnailUrl)
private fun ThingItemDto.toCached(fetchedAt: Long) =
    CachedGame(id, name, minPlayers, maxPlayers, playingTimeMinutes, thumbnailUrl, fetchedAt)
