package com.mathewhaug.boardgamehelper.di

import com.mathewhaug.boardgamehelper.data.AppError
import com.mathewhaug.boardgamehelper.data.AppException
import com.mathewhaug.boardgamehelper.data.FakeCatalogueDao
import com.mathewhaug.boardgamehelper.data.FakeShortlistDao
import com.mathewhaug.boardgamehelper.data.RoomGameRepository
import com.mathewhaug.boardgamehelper.data.remote.BggApi
import com.mathewhaug.boardgamehelper.data.remote.sampleAsset
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.create

// the real OkHttpClient and Retrofit from NetworkModule, pointed at a local server instead of
// BGG. it proves what the app actually sends: the right path under xmlapi2/ and the token in the
// Authorization header, with no BGG account and no network needed
class NetworkModuleTest {
    private val server = MockWebServer()
    private lateinit var api: BggApi

    @Before fun setUp() {
        server.start()
        val client = NetworkModule.okHttpClient(token = "test-token")
        api = NetworkModule.retrofit(server.url("/xmlapi2/").toString(), client).create()
    }

    @After fun tearDown() = server.shutdown()

    private fun xml(body: String) = MockResponse().setBody(body).setHeader("Content-Type", "text/xml")

    @Test fun hot_sendsPathAndBearerToken() = runTest {
        server.enqueue(xml(sampleAsset("sample_hot.xml")))

        api.hot("boardgame").close()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/xmlapi2/hot?type=boardgame", request.path)
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
    }

    @Test fun things_sendsIdsAsOneCommaSeparatedParameter() = runTest {
        server.enqueue(xml(sampleAsset("sample_things.xml")))

        api.things("13,822").close()

        val request = server.takeRequest()
        assertEquals("/xmlapi2/thing", request.requestUrl!!.encodedPath)
        assertEquals(listOf("13,822"), request.requestUrl!!.queryParameterValues("id"))
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
    }

    @Test fun search_sendsQueryAndType() = runTest {
        server.enqueue(xml(sampleAsset("sample_search.xml")))

        api.search("pandemic", "boardgame").close()

        assertEquals("/xmlapi2/search?query=pandemic&type=boardgame", server.takeRequest().path)
    }

    // the wrong token case, end to end through the real client: BGG answers 401, Retrofit throws
    // HttpException, safeApiCall turns it into Unauthorized, and the cache is not touched
    @Test fun wrongToken_401_isUnauthorized() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        val dao = FakeCatalogueDao()
        val repository = RoomGameRepository(api, dao, FakeShortlistDao(), StandardTestDispatcher(testScheduler))

        val result = repository.refresh()

        assertEquals(AppError.Unauthorized, (result.exceptionOrNull() as AppException).error)
        assertEquals(emptyList<Any>(), dao.rows.value)
    }

    @Test fun baseUrl_hasNoWww() {
        assertEquals("https://boardgamegeek.com/xmlapi2/", NetworkModule.BASE_URL)
    }
}
