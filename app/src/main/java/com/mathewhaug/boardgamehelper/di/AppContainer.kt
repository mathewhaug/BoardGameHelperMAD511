package com.mathewhaug.boardgamehelper.di

import android.content.Context
import com.mathewhaug.boardgamehelper.data.GameRepository
import com.mathewhaug.boardgamehelper.data.RoomGameRepository
import com.mathewhaug.boardgamehelper.data.local.AppDatabase

// dependency injection without a framework: a class receives what it needs through its
// constructor instead of building it itself. this container is the one place in the whole app
// that names a concrete repository, every ViewModel just asks for a GameRepository and is handed
// whatever is wired up here, which is why moving the shortlist into Room changed nothing under ui/
//
// the container takes the application context so that any implementation that needs one (Room
// does, DataStore does) can be built right here, rather than a ViewModel having to reach for a
// Context and dragging Android into a class we want to unit test on a plain JVM
//
// FakeGameRepository is still in the project on purpose. the previews and the unit tests keep
// using it, because they want a repository and do not want a database
class AppContainer(context: Context) {
    // the only line in the app that knows Room exists
    val repository: GameRepository = RoomGameRepository(AppDatabase.get(context).shortlistDao())
}
