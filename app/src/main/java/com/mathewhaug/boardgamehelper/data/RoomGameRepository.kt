package com.mathewhaug.boardgamehelper.data

import com.mathewhaug.boardgamehelper.data.local.ShortlistDao
import com.mathewhaug.boardgamehelper.data.local.ShortlistedGame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// the same interface FakeGameRepository implements, now backed by a real table. this class owns
// the mapping between the database's shape and the app's shape, so the DAO never hears about Game
// and the screens never hear about ShortlistedGame. nothing above this file imports androidx.room3
//
// the ViewModels do not change at all for this swap, because they depend on GameRepository and not
// on a concrete class. that is the whole point of the Week 4 interface paying off
class RoomGameRepository(private val dao: ShortlistDao) : GameRepository {
    override val games: Flow<List<Game>> = flowOf(SampleGames)  // catalogue stays hard coded, network later
    override val shortlist: Flow<List<Game>> =
        dao.observeAll().map { rows -> rows.map { it.toGame() } }
    override fun search(term: String): Flow<List<Game>> =
        dao.search(term).map { rows -> rows.map { it.toGame() } }
    override suspend fun addToShortlist(game: Game) = dao.upsert(game.toEntity())
    override suspend fun removeFromShortlist(game: Game) = dao.delete(game.toEntity())
}

// private and in this file, so the only code that can convert between the two shapes is the class
// that needs to. addedAt is left to its default, the delete only matches on the primary key anyway
private fun ShortlistedGame.toGame() = Game(id, name, minPlayers, maxPlayers, playTimeMinutes)
private fun Game.toEntity() = ShortlistedGame(id, name, minPlayers, maxPlayers, playTimeMinutes)
