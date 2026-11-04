package com.mathewhaug.boardgamehelper.data.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

// the local copy of the BGG catalogue. the screen only ever reads this table, the network only
// ever writes to it, so the list is there straight away on launch and still there with no signal
@Entity(tableName = "catalogue")
data class CachedGame(
    // the BGG id again, so fetching the same game twice updates its row instead of adding a copy
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "min_players") val minPlayers: Int,
    @ColumnInfo(name = "max_players") val maxPlayers: Int,
    @ColumnInfo(name = "play_time_minutes") val playTimeMinutes: Int,
    // nullable because BGG does not promise every game has one
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String?,
    // epoch millis, kept as a plain Long because the only thing it is used for is subtracting it
    // from the clock to see whether the cache is more than an hour old
    @ColumnInfo(name = "fetched_at") val fetchedAt: Long,
)
