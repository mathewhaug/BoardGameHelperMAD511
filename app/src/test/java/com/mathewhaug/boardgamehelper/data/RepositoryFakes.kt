package com.mathewhaug.boardgamehelper.data

import com.mathewhaug.boardgamehelper.data.local.CachedGame
import com.mathewhaug.boardgamehelper.data.local.CatalogueDao
import com.mathewhaug.boardgamehelper.data.local.ShortlistDao
import com.mathewhaug.boardgamehelper.data.local.ShortlistedGame
import com.mathewhaug.boardgamehelper.data.remote.BggApi
import com.mathewhaug.boardgamehelper.data.remote.sampleAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody

// answers with the sample files by default. set failure and every call throws it instead, which
// is how a test pretends the phone is offline or the token was turned down
class FakeBggApi(
    private val hotXml: String = sampleAsset("sample_hot.xml"),
    private val thingsXml: String = sampleAsset("sample_things.xml"),
) : BggApi {
    var failure: Throwable? = null
    val thingsCalls = mutableListOf<String>()

    override suspend fun hot(type: String) = respond(hotXml)
    override suspend fun search(query: String, type: String) = respond("<items/>")
    override suspend fun things(ids: String): ResponseBody {
        thingsCalls += ids
        return respond(thingsXml)
    }

    private fun respond(xml: String): ResponseBody {
        failure?.let { throw it }
        return xml.toResponseBody("text/xml".toMediaType())
    }
}

// Room needs a device, so the repository tests run against these in memory stand ins instead
class FakeCatalogueDao(initial: List<CachedGame> = emptyList()) : CatalogueDao {
    val rows = MutableStateFlow(initial)
    override fun observeAll(): Flow<List<CachedGame>> = rows.map { it.sortedBy { game -> game.name } }
    override suspend fun upsertAll(games: List<CachedGame>) = rows.update { current ->
        (current.associateBy { it.id } + games.associateBy { it.id }).values.toList()
    }
    override suspend fun oldestFetch(): Long? = rows.value.minOfOrNull { it.fetchedAt }
}

class FakeShortlistDao : ShortlistDao {
    private val rows = MutableStateFlow<List<ShortlistedGame>>(emptyList())
    override fun observeAll(): Flow<List<ShortlistedGame>> = rows
    override fun search(term: String): Flow<List<ShortlistedGame>> =
        rows.map { list -> list.filter { it.name.startsWith(term, ignoreCase = true) } }
    override suspend fun upsert(game: ShortlistedGame) = rows.update { it.filterNot { r -> r.id == game.id } + game }
    override suspend fun delete(game: ShortlistedGame) = rows.update { it.filterNot { r -> r.id == game.id } }
}
