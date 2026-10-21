package com.mathewhaug.boardgamehelper.di

import com.mathewhaug.boardgamehelper.data.GameRepository
import com.mathewhaug.boardgamehelper.data.RoomGameRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// hilt already knows how to build RoomGameRepository from its @Inject constructor, what it cannot
// guess is that a GameRepository request should get that class. @Binds is that one line, and like
// the old container it is the only place in the app that names a concrete repository
//
// FakeGameRepository stays in the project on purpose. previews and unit tests keep using it,
// because they want a repository and do not want a database
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // @Singleton so every screen shares one repository, same as when the container held it
    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: RoomGameRepository): GameRepository
}
