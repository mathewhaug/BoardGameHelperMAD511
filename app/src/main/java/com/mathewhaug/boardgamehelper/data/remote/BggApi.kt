package com.mathewhaug.boardgamehelper.data.remote

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

// BGG answers in XML, so there is no converter in the way: every call hands back the raw body
// and BggXmlParser turns it into DTOs. paths are relative on purpose, they get appended to the
// base url in NetworkModule, and a leading slash here would throw away the xmlapi2/ part of it
interface BggApi {
    @GET("hot")
    suspend fun hot(@Query("type") type: String): ResponseBody

    @GET("search")
    suspend fun search(@Query("query") query: String, @Query("type") type: String): ResponseBody

    // BGG wants one id parameter holding a comma separated list, not id=1&id=2, so the caller
    // joins them into a single string before it gets here
    @GET("thing")
    suspend fun things(@Query("id") ids: String): ResponseBody
}
