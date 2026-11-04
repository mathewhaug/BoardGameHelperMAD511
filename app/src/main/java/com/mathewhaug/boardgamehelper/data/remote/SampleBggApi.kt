package com.mathewhaug.boardgamehelper.data.remote

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody

// stands in for BGG while there is no token. it hands back the hand written files in assets as
// if they had just come over the wire, so everything after this (parsing, Room, the screen) runs
// exactly the code it will run against the real API. the arguments are ignored, every hot call
// gets the same hot list and every things call gets every game in the sample
class SampleBggApi(private val assets: AssetManager) : BggApi {

    override suspend fun hot(type: String): ResponseBody = load("sample_hot.xml")

    override suspend fun search(query: String, type: String): ResponseBody = load("sample_search.xml")

    override suspend fun things(ids: String): ResponseBody = load("sample_things.xml")

    // reading a file is blocking IO. Retrofit's suspend calls are safe to make from the main
    // thread, so this has to be too, or the fake would behave differently from the real thing
    private suspend fun load(name: String): ResponseBody = withContext(Dispatchers.IO) {
        val text = assets.open(name).bufferedReader().use { it.readText() }
        text.toResponseBody(XML)
    }

    private companion object {
        val XML = "text/xml".toMediaType()
    }
}
