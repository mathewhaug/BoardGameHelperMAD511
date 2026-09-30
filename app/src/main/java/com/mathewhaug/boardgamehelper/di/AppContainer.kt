package com.mathewhaug.boardgamehelper.di

import android.content.Context
import com.mathewhaug.boardgamehelper.data.FakeGameRepository
import com.mathewhaug.boardgamehelper.data.GameRepository

// dependency injection without a framework: a class receives what it needs through its
// constructor instead of building it itself. this container is the one place in the whole app
// that names a concrete repository - every ViewModel just asks for a GameRepository and is handed
// whatever is wired up here, so swapping the right hand side for a Room backed class later
// changes nothing under ui/
//
// the container takes the application context so that any implementation that needs one (Room
// does, DataStore does) can be built right here, rather than a ViewModel having to reach for a
// Context and dragging Android into a class we want to unit test on a plain JVM
class AppContainer(context: Context) {
    val repository: GameRepository = FakeGameRepository()
}
