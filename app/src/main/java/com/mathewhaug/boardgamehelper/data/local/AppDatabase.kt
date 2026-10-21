package com.mathewhaug.boardgamehelper.data.local

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase

// version is a contract with every device that already has this database on it, so the rule is
// bump it and write a migration in the same commit, never one without the other. exportSchema
// writes schemas/.../1.json at build time, and that file gets committed along with the code
@Database(entities = [ShortlistedGame::class], version = 1, exportSchema = true)
@ColumnTypeConverters(InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shortlistDao(): ShortlistDao

    // hilt builds this now, so the companion object singleton is gone. see di/DatabaseModule
}
