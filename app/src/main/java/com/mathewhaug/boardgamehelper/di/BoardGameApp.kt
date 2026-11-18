package com.mathewhaug.boardgamehelper.di

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

// @HiltAndroidApp makes hilt generate the singleton component and attach it to this Application,
// so the graph lives exactly as long as the process. it still has to be registered in the
// manifest with android:name, otherwise the system builds the stock Application and hilt never starts
@HiltAndroidApp
class BoardGameApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    // channels only exist from API 26, below that the notification just posts without one. the
    // channel has to be there before the timer service posts into it, and onCreate here runs
    // before any service or activity does. creating a channel that already exists is a no op, so
    // doing it on every launch is fine. LOW means it sits in the shade without a sound, which is
    // right for something that stays up the whole game
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            GAME_NIGHT_CHANNEL_ID,
            "Game night",
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val GAME_NIGHT_CHANNEL_ID = "game_night"
    }
}
