package com.mathewhaug.boardgamehelper.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mathewhaug.boardgamehelper.data.local.AppDatabase
import com.mathewhaug.boardgamehelper.data.local.ShortlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

// Room builds the database through a builder, not a constructor, so hilt cannot @Inject it and
// needs a @Provides that says how. installed in SingletonComponent so it lives for the whole process
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // @Singleton because two open connections to the same file can step on each other. the name
    // has to stay boardgames.db or every device that already has a shortlist would get an empty one
    //
    // setDriver is required in Room 3, it no longer assumes the framework SQLite is there.
    // setQueryCoroutineContext is where every DAO call actually runs, which is why nobody
    // above this ever wraps a call in withContext(Dispatchers.IO)
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder<AppDatabase>(context, "boardgames.db")
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    // not scoped, the DAO is cheap and the database it comes from is already the single instance
    @Provides
    fun provideShortlistDao(db: AppDatabase): ShortlistDao = db.shortlistDao()
}
