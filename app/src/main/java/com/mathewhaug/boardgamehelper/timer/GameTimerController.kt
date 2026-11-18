package com.mathewhaug.boardgamehelper.timer

import android.content.Context
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

// the ViewModel asks for this instead of a Context, so it keeps no Android types of its own and
// the unit tests can hand it a fake that starts nothing
interface GameTimerController {
    fun start()
    fun stop()
}

// the application context and never an Activity, because the ViewModel outlives a rotation and
// holding the old Activity would leak it
class ServiceGameTimerController @Inject constructor(
    @ApplicationContext private val context: Context,
) : GameTimerController {

    // startForegroundService from API 26, plain startService below, ContextCompat picks for us.
    // this only ever runs from a button tap while Home is on screen, which is the one time a
    // foreground service is always allowed to start
    override fun start() {
        ContextCompat.startForegroundService(context, GameTimerService.startIntent(context))
    }

    // a plain startService carrying ACTION_STOP rather than stopService, so the stop goes down the
    // same path as the notification's Stop button and TimerState is always cleared by the service
    override fun stop() {
        context.startService(GameTimerService.stopIntent(context))
    }
}
