package com.mathewhaug.boardgamehelper.di

import android.content.Context
import android.util.Log
import com.mathewhaug.boardgamehelper.BuildConfig
import com.mathewhaug.boardgamehelper.data.remote.AuthInterceptor
import com.mathewhaug.boardgamehelper.data.remote.BggApi
import com.mathewhaug.boardgamehelper.data.remote.SampleBggApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Provider
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

    // a blank token means nobody has put one in local.properties yet, and BGG would turn every
    // request down with a 401, so the app runs on the sample files instead. Provider<Retrofit>
    // means the real client is only ever built when it is actually going to be used. the log line
    // says which one won, so nobody has to guess why the list looks the way it does. it names the
    // source and never prints the token
    @Provides
    @Singleton
    fun provideBggApi(
        @ApplicationContext context: Context,
        retrofit: Provider<Retrofit>,
    ): BggApi =
        if (BuildConfig.BGG_TOKEN.isBlank()) {
            Log.i(TAG, "BGG_TOKEN is blank, using SampleBggApi (sample data from assets)")
            SampleBggApi(context.assets)
        } else {
            Log.i(TAG, "BGG_TOKEN is set, using the real BGG api at $BASE_URL")
            retrofit.get().create()
        }

    private const val TAG = "NetworkModule"

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
