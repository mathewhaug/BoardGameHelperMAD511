package com.mathewhaug.boardgamehelper.data.local

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// this lives in androidTest, not test, because SQLite needs a device or an emulator to run on. an
// in memory database is still a real SQLite running the real generated DAO, it just vanishes when
// the test ends, so every test starts from an empty table and nothing leaks between them
@RunWith(AndroidJUnit4::class)
class ShortlistDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: ShortlistDao

    @Before fun open() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder<AppDatabase>(context)
            .setDriver(BundledSQLiteDriver()).build()
        dao = db.shortlistDao()
    }
    @After fun close() = db.close()

    // first() takes the Flow's current value and stops collecting, which is all a one shot
    // assertion needs from a query that would otherwise keep emitting forever
    @Test fun upsertThenObserve_emitsTheGame() = runTest {
        dao.upsert(ShortlistedGame(13, "Catan", 3, 4, 90))
        assertEquals("Catan", dao.observeAll().first().single().name)
    }

    // "cat" finds Catan and not Pandemic, which checks both halves at once: LIKE is case
    // insensitive for ASCII in SQLite, and the wildcard only sits at the end of the term
    @Test fun search_isPrefixAndCaseInsensitive() = runTest {
        dao.upsert(ShortlistedGame(13, "Catan", 3, 4, 90))
        dao.upsert(ShortlistedGame(30549, "Pandemic", 2, 4, 45))
        assertEquals(listOf("Catan"), dao.search("cat").first().map { it.name })
    }
}
