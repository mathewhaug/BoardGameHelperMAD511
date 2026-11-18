package com.mathewhaug.boardgamehelper.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// the service and the ViewModel never talk to each other directly, they both get this one
// instance from hilt. a @Singleton lives as long as the process, which is as long as the service
// can, so the start time survives the Home screen being closed and opened again
@Singleton
class TimerState @Inject constructor() {
    // null means no timer is running. only a timestamp is kept, the elapsed time is worked out
    // from it whenever someone needs it, so nothing here has to tick
    private val _startedAt = MutableStateFlow<Long?>(null)
    val startedAt: StateFlow<Long?> = _startedAt.asStateFlow()

    // a second tap on Start, or the system calling onStartCommand again, must not reset the clock
    fun start(now: Long = System.currentTimeMillis()) {
        _startedAt.compareAndSet(null, now)
    }

    fun stop() {
        _startedAt.value = null
    }
}
