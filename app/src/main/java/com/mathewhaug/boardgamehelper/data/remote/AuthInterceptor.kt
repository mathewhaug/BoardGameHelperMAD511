package com.mathewhaug.boardgamehelper.data.remote

import okhttp3.Interceptor
import okhttp3.Response

// every BGG request needs the token, and doing it here means BggApi never has to mention it.
// the token comes in through the constructor rather than being read from BuildConfig in here, so
// a test can hand it a fake one and check it arrives
class AuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        )
}
