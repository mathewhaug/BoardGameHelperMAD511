package com.mathewhaug.boardgamehelper.di

import com.mathewhaug.boardgamehelper.timer.GameTimerController
import com.mathewhaug.boardgamehelper.timer.ServiceGameTimerController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// same idea as RepositoryModule: the ViewModel asks for the interface and this is the one place
// that says the real one talks to GameTimerService
@Module
@InstallIn(SingletonComponent::class)
abstract class TimerModule {

    @Binds
    abstract fun bindGameTimerController(impl: ServiceGameTimerController): GameTimerController
}
