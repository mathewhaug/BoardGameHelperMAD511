package com.mathewhaug.boardgamehelper.data.local

import androidx.room3.AutoMigration
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase

// version is a contract with every device that already has this database on it, so the rule is
// bump it and write a migration in the same commit, never one without the other. exportSchema
// writes schemas/.../1.json at build time, and that file gets committed along with the code
//
// version 2 only adds the catalogue table. Room can work that out by comparing 1.json with 2.json,
// so an AutoMigration covers it and every shortlist already on a phone survives the upgrade
@Database(
    entities = [ShortlistedGame::class, CachedGame::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@ColumnTypeConverters(InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shortlistDao(): ShortlistDao
    abstract fun catalogueDao(): CatalogueDao

    // hilt builds this now, so the companion object singleton is gone. see di/DatabaseModule
}
