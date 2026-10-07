package com.mathewhaug.boardgamehelper.data.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.Instant

// the database's shape for a shortlisted game, kept separate from Game so the table can grow
// columns like addedAt without the screens ever seeing them. it mirrors Game field for field on
// purpose: a mapper that quietly drops a field is a bug the compiler cannot see, so every field
// of the domain model gets a column here
//
// the search box filters on name, and a B tree index on that column turns a prefix match into an
// O(log n) lookup instead of reading every row
@Entity(tableName = "shortlist", indices = [Index("name")])
data class ShortlistedGame(
    // this is the BoardGameGeek id, so it is already unique and stable. that makes it the natural
    // primary key, and there is no reason to let Room generate one of its own
    @PrimaryKey val id: Int,
    val name: String,
    // snake case column names are the SQL convention, camelCase stays on the Kotlin side
    @ColumnInfo(name = "min_players") val minPlayers: Int,
    @ColumnInfo(name = "max_players") val maxPlayers: Int,
    @ColumnInfo(name = "play_time_minutes") val playTimeMinutes: Int,
    @ColumnInfo(name = "added_at") val addedAt: Instant = Instant.now(),
)
