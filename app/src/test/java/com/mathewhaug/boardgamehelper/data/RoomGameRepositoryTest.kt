package com.mathewhaug.boardgamehelper.data

import com.mathewhaug.boardgamehelper.data.local.CachedGame
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class RoomGameRepositoryTest {

    private val api = FakeBggApi()
    private val cached = CachedGame(13, "Catan", 3, 4, 120, null, fetchedAt = 1_000L)

    // the io dispatcher shares the test's scheduler, so the withContext switches and the five
    // second delay all run on virtual time and the test finishes in milliseconds
    private fun TestScope.repository(dao: FakeCatalogueDao) =
        RoomGameRepository(api, dao, FakeShortlistDao(), StandardTestDispatcher(testScheduler))

    @Test fun refresh_success_fillsTheCatalogue() = runTest {
        val dao = FakeCatalogueDao()
        val repository = repository(dao)

        val result = repository.refresh()

        assertTrue(result.isSuccess)
        val games = repository.games.first()
        assertEquals(20, games.size)
        // sorted by name, mapped to Game, thumbnail carried across
        assertEquals("7 Wonders", games.first().name)
        assertEquals(
            Game(13, "Catan", 3, 4, 120, "https://example.com/bgg-sample/13_thumb.jpg"),
            games.single { it.id == 13 },
        )
        // 20 hot ids is exactly one batch
        assertEquals(1, api.thingsCalls.size)
        assertEquals(20, api.thingsCalls.single().split(",").size)
        assertTrue(repository.oldestFetch()!! > 0)
    }

    @Test fun refresh_noNetwork_leavesTheCacheAlone() = runTest {
        val dao = FakeCatalogueDao(listOf(cached))
        val repository = repository(dao)
        api.failure = IOException("airplane mode")

        val result = repository.refresh()

        assertEquals(AppError.NoNetwork, (result.exceptionOrNull() as AppException).error)
        assertEquals(listOf(cached), dao.rows.value)
        assertEquals(1_000L, repository.oldestFetch())
    }

    @Test fun refresh_401_isUnauthorized() = runTest {
        val repository = repository(FakeCatalogueDao())
        api.failure = HttpException(Response.error<Unit>(401, "".toResponseBody(null)))

        val result = repository.refresh()

        assertEquals(AppError.Unauthorized, (result.exceptionOrNull() as AppException).error)
        assertTrue(repository.games.first().isEmpty())
    }
}
