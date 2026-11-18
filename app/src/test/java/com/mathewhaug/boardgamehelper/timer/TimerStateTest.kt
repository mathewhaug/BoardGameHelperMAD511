package com.mathewhaug.boardgamehelper.timer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimerStateTest {

    @Test
    fun start_setsTheTime() {
        val state = TimerState()

        state.start(now = 1_000L)

        assertEquals(1_000L, state.startedAt.value)
    }

    // the clock keeps its first start time, otherwise a stray second tap would zero the timer
    @Test
    fun secondStart_doesNotChangeTheTime() {
        val state = TimerState()

        state.start(now = 1_000L)
        state.start(now = 5_000L)

        assertEquals(1_000L, state.startedAt.value)
    }

    @Test
    fun stop_clearsTheTime() {
        val state = TimerState()
        state.start(now = 1_000L)

        state.stop()

        assertNull(state.startedAt.value)
    }
}
