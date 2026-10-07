package com.mathewhaug.boardgamehelper.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

// the same two shapes as GameRepository, one layer down. reads the UI watches return a Flow, and
// Room re-emits it whenever the shortlist table changes, so a write anywhere in the app shows up
// on screen with no refresh call. everything else is a suspend function, because Room 3 refuses
// to generate a blocking DAO at all
@Dao
interface ShortlistDao {
    @Query("SELECT * FROM shortlist ORDER BY added_at DESC")
    fun observeAll(): Flow<List<ShortlistedGame>>

    // :term is bound as a parameter, never glued into the SQL string, so there is no concatenation
    // and no way to inject anything through the search box. prefix match on purpose: a leading
    // wildcard ('%' || :term) cannot use the name index and falls back to a full table scan
    @Query("SELECT * FROM shortlist WHERE name LIKE :term || '%' ORDER BY name")
    fun search(term: String): Flow<List<ShortlistedGame>>

    // updates the row if the key exists and inserts it if not, with no hidden delete in between,
    // unlike OnConflictStrategy.REPLACE which deletes the old row first and inserts a new one
    @Upsert suspend fun upsert(game: ShortlistedGame)
    @Delete suspend fun delete(game: ShortlistedGame)
}
