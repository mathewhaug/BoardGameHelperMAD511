package com.mathewhaug.boardgamehelper.ui.home

import com.mathewhaug.boardgamehelper.data.AppError
import com.mathewhaug.boardgamehelper.data.AppException
import com.mathewhaug.boardgamehelper.data.FakeGameRepository
import com.mathewhaug.boardgamehelper.data.SampleGames
import com.mathewhaug.boardgamehelper.timer.GameTimerController
import com.mathewhaug.boardgamehelper.timer.TimerState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// this runs on a plain JVM in milliseconds: no emulator, no Robolectric, no Android at all,
// because HomeViewModel depends on the GameRepository interface and has no Android imports. the
// fake it is handed is the same FakeGameRepository the previews use, so there is no separate mock
// to keep in step with the real thing
class HomeViewModelTest {
    // viewModelScope launches on Dispatchers.Main, which does not exist on the JVM, so each test
    // swaps in a dispatcher it controls before running and puts the real one back afterwards
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private val now get() = System.currentTimeMillis()
    private val twoHoursAgo get() = now - 2 * 60 * 60 * 1000L
    private val noNetwork = Result.failure<Unit>(AppException(AppError.NoNetwork))

    // stands in for the real controller, which would need a Context to reach the service. it
    // plays the service's part by writing TimerState, and counts calls so a test can check them
    private class FakeTimerController(private val state: TimerState) : GameTimerController {
        var starts = 0
        var stops = 0
        override fun start() { starts++; state.start() }
        override fun stop() { stops++; state.stop() }
    }

    private val timerState = TimerState()
    private val timerController = FakeTimerController(timerState)

    private fun viewModel(repository: FakeGameRepository) =
        HomeViewModel(repository, timerState, timerController)

    // uiState only runs while someone collects it (WhileSubscribed), so each test keeps a
    // collector going in backgroundScope, which runTest cancels on its own at the end
    private fun TestScope.started(repository: FakeGameRepository): HomeViewModel {
        val viewModel = viewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun toggleShortlist_addsThenRemoves() = runTest {
        val viewModel = viewModel(FakeGameRepository(lastFetch = now))
        val catan = SampleGames.first()

        // first { } collects until the predicate is true, which is the natural way to wait on a
        // StateFlow that updates itself off the back of a repository write
        viewModel.uiState.first { it is HomeUiState.Success }
        viewModel.toggleShortlist(catan)
        val added = viewModel.uiState.first { it is HomeUiState.Success && it.shortlistIds.isNotEmpty() }
        assertTrue(catan.id in (added as HomeUiState.Success).shortlistIds)

        viewModel.toggleShortlist(catan)
        val removed = viewModel.uiState.first { it is HomeUiState.Success && it.shortlistIds.isEmpty() }
        assertTrue((removed as HomeUiState.Success).shortlistIds.isEmpty())
    }

    @Test
    fun freshCache_isSuccessAndDoesNotRefresh() = runTest {
        val repository = FakeGameRepository(lastFetch = now)

        val viewModel = started(repository)

        assertEquals(HomeUiState.Success(SampleGames), viewModel.uiState.value)
        assertEquals(0, repository.refreshCount)
    }

    @Test
    fun staleCache_refreshesOnInit() = runTest {
        val repository = FakeGameRepository(lastFetch = twoHoursAgo)

        started(repository)

        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun neverFetched_refreshesOnInit() = runTest {
        val repository = FakeGameRepository(lastFetch = null)

        started(repository)

        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun noGames_whileRefreshing_isLoading_thenEmpty() = runTest {
        val gate = CompletableDeferred<Result<Unit>>()
        val viewModel = started(FakeGameRepository(games = emptyList(), onRefresh = { gate.await() }))

        assertEquals(HomeUiState.Loading, viewModel.uiState.value)

        gate.complete(Result.success(Unit))
        advanceUntilIdle()
        assertEquals(HomeUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun noGames_refreshFails_isError() = runTest {
        val viewModel = started(FakeGameRepository(games = emptyList(), onRefresh = { noNetwork }))

        assertEquals(HomeUiState.Error(AppError.NoNetwork), viewModel.uiState.value)
    }

    @Test
    fun cachedGames_whileRefreshing_isSuccessWithRefreshing() = runTest {
        val gate = CompletableDeferred<Result<Unit>>()
        val viewModel = started(FakeGameRepository(onRefresh = { gate.await() }))

        assertEquals(HomeUiState.Success(SampleGames, refreshing = true), viewModel.uiState.value)

        gate.complete(Result.success(Unit))
        advanceUntilIdle()
        assertEquals(HomeUiState.Success(SampleGames), viewModel.uiState.value)
    }

    // cached data wins: a failed refresh with games on hand is a snackbar, not an error screen
    @Test
    fun cachedGames_refreshFails_isSuccessWithNotice_untilShown() = runTest {
        val viewModel = started(FakeGameRepository(onRefresh = { noNetwork }))

        assertEquals(HomeUiState.Success(SampleGames, notice = AppError.NoNetwork), viewModel.uiState.value)

        viewModel.onNoticeShown()
        advanceUntilIdle()
        assertEquals(HomeUiState.Success(SampleGames), viewModel.uiState.value)
    }

    // the Retry button path: after an error, refresh() runs the repository again
    @Test
    fun refresh_afterError_triesAgain() = runTest {
        val repository = FakeGameRepository(games = emptyList(), onRefresh = { noNetwork })
        val viewModel = started(repository)

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(2, repository.refreshCount)
    }

    // the ticker never goes idle while a timer runs, so these tests step with runCurrent and
    // advanceTimeBy instead of advanceUntilIdle, which would wait forever
    @Test
    fun noTimer_hasNoElapsedTime() = runTest {
        val viewModel = viewModel(FakeGameRepository(lastFetch = now))
        backgroundScope.launch { viewModel.gameNight.collect {} }
        runCurrent()

        assertNull(viewModel.gameNight.value.elapsedSeconds)
    }

    @Test
    fun runningTimer_showsElapsedSeconds_untilStopped() = runTest {
        val viewModel = viewModel(FakeGameRepository(lastFetch = now))
        backgroundScope.launch { viewModel.gameNight.collect {} }
        timerState.start(now = now - 65_000)
        runCurrent()

        val elapsed = viewModel.gameNight.value.elapsedSeconds
        assertTrue("elapsed was $elapsed", elapsed != null && elapsed in 65L..66L)

        viewModel.stopTimer()
        runCurrent()
        assertNull(viewModel.gameNight.value.elapsedSeconds)
        assertEquals(1, timerController.stops)
    }

    @Test
    fun startTimer_goesThroughTheController() = runTest {
        val viewModel = viewModel(FakeGameRepository(lastFetch = now))

        viewModel.startTimer()

        assertEquals(1, timerController.starts)
    }

    @Test
    fun setLowBattery_showsAndClearsTheFlag() = runTest {
        val viewModel = viewModel(FakeGameRepository(lastFetch = now))
        backgroundScope.launch { viewModel.gameNight.collect {} }

        viewModel.setLowBattery(true)
        runCurrent()
        assertTrue(viewModel.gameNight.value.lowBattery)

        viewModel.setLowBattery(false)
        runCurrent()
        assertTrue(!viewModel.gameNight.value.lowBattery)
    }
}
