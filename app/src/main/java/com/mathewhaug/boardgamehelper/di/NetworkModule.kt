package com.mathewhaug.boardgamehelper.di

import com.mathewhaug.boardgamehelper.BuildConfig
import com.mathewhaug.boardgamehelper.data.remote.AuthInterceptor
import com.mathewhaug.boardgamehelper.data.remote.BggApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// one OkHttpClient for the whole app, because each one owns a connection pool and a thread pool,
// and building a new one per request throws both away every time
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // no www: the www host answers with a redirect, and that costs an extra round trip per call
    const val BASE_URL = "https://boardgamegeek.com/xmlapi2/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = okHttpClient(BuildConfig.BGG_TOKEN)

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = retrofit(BASE_URL, client)

    @Provides
    @Singleton
    fun provideBggApi(retrofit: Retrofit): BggApi = retrofit.create()

    // the providers above only pick the real token and the real host. the building lives in these
    // two so the MockWebServer test can run exactly the same setup against a local server
    internal fun okHttpClient(token: String): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(token))
        // debug only, so a release build never writes request lines to logcat. BASIC does not
        // print headers today, but the redaction means bumping it to HEADERS later still cannot
        // leak the token. added after AuthInterceptor so it sees the request with the header on
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                    redactHeader("Authorization")
                }
            )
        }
        return builder.build()
    }

    internal fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .build()
}
