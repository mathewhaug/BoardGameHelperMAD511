package com.mathewhaug.boardgamehelper.timer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.mathewhaug.boardgamehelper.R
import com.mathewhaug.boardgamehelper.di.BoardGameApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// a started service, not a bound one: Home asks it to start or stop and then forgets about it.
// it runs in the foreground so the system leaves the process alone while the phone sits on the
// table for a two hour game. the clock itself is just a timestamp in TimerState, the
// notification's chronometer does the counting, so there is no loop or coroutine in here to cancel
@AndroidEntryPoint
class GameTimerService : Service() {

    @Inject lateinit var timerState: TimerState

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            timerState.stop()
            stopSelf()
            return START_NOT_STICKY
        }

        // a service started with startForegroundService has about five seconds to call
        // startForeground or the system kills the app with an ANR style crash, so it goes first
        // before anything that could be slow or throw. a running timer keeps its time, a new
        // one starts now
        val startedAt = timerState.startedAt.value ?: System.currentTimeMillis()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(startedAt),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )
        timerState.start(startedAt)

        // if the process dies there is nothing worth restarting into, the start time died with it
        return START_NOT_STICKY
    }

    private fun buildNotification(startedAt: Long): Notification {
        // FLAG_IMMUTABLE because nothing else should be able to rewrite this intent, and from
        // API 31 a PendingIntent has to say which one it is. minSdk 24 already has the flag
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, GameTimerService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, BoardGameApp.GAME_NIGHT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle("Game night timer")
            // setWhen plus setUsesChronometer makes the system draw a live count up from the
            // start time, so the notification never has to be posted again to stay current
            .setWhen(startedAt)
            .setUsesChronometer(true)
            .setShowWhen(true)
            .setOngoing(true)
            .addAction(R.drawable.ic_timer, "Stop", stopIntent)
            .build()
    }

    companion object {
        const val ACTION_STOP = "com.mathewhaug.boardgamehelper.timer.ACTION_STOP"
        private const val NOTIFICATION_ID = 1

        fun startIntent(context: Context) = Intent(context, GameTimerService::class.java)

        fun stopIntent(context: Context) =
            Intent(context, GameTimerService::class.java).setAction(ACTION_STOP)
    }
}
