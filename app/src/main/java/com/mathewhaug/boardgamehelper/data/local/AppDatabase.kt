package com.mathewhaug.boardgamehelper.data.local

import android.content.Context
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

// version is a contract with every device that already has this database on it, so the rule is
// bump it and write a migration in the same commit, never one without the other. exportSchema
// writes schemas/.../1.json at build time, and that file gets committed along with the code
@Database(entities = [ShortlistedGame::class], version = 1, exportSchema = true)
@ColumnTypeConverters(InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shortlistDao(): ShortlistDao

    // one database per process, because two open connections to the same file can step on each
    // other. this is double checked locking: the cheap null check outside the lock covers every
    // call after the first, and the second check inside covers two threads racing on the very
    // first one. @Volatile makes the write to INSTANCE visible to every thread straight away
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context.applicationContext).also { INSTANCE = it }
            }

        // setDriver is required in Room 3, it no longer assumes the framework SQLite is there.
        // setQueryCoroutineContext is where every DAO call actually runs, which is why nobody
        // above this class ever wraps a call in withContext(Dispatchers.IO)
        private fun build(context: Context) =
            Room.databaseBuilder<AppDatabase>(context, "boardgames.db")
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
    }
}
