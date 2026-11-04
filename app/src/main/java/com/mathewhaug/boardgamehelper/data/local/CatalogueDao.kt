package com.mathewhaug.boardgamehelper.data.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogueDao {
    // a Flow, so every batch the refresh writes shows up on screen the moment it lands
    @Query("SELECT * FROM catalogue ORDER BY name")
    fun observeAll(): Flow<List<CachedGame>>

    // a whole batch in one transaction, so the screen sees the batch arrive at once, not row by row
    @Upsert suspend fun upsertAll(games: List<CachedGame>)

    // the oldest row decides whether the cache is stale. MIN over an empty table is NULL, which
    // is exactly the "never fetched" answer the caller wants
    @Query("SELECT MIN(fetched_at) FROM catalogue")
    suspend fun oldestFetch(): Long?
}
