package com.mathewhaug.boardgamehelper.ui.home

import com.mathewhaug.boardgamehelper.data.FakeGameRepository
import com.mathewhaug.boardgamehelper.data.SampleGames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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

    @Test
    fun toggleShortlist_addsThenRemoves() = runTest {
        val viewModel = HomeViewModel(FakeGameRepository())
        val catan = SampleGames.first()

        // first { } collects until the predicate is true, which is the natural way to wait on a
        // StateFlow that updates itself off the back of a repository write
        viewModel.toggleShortlist(catan)
        val added = viewModel.uiState.first { it.shortlistIds.isNotEmpty() }
        assertTrue(catan.id in added.shortlistIds)

        viewModel.toggleShortlist(catan)
        val removed = viewModel.uiState.first { it.shortlistIds.isEmpty() }
        assertTrue(removed.shortlistIds.isEmpty())
    }
}
