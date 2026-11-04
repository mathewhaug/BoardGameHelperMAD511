package com.mathewhaug.boardgamehelper.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

// a qualifier because hilt picks what to inject by type, and every dispatcher is just a
// CoroutineDispatcher. the point of injecting it at all is that a test can hand in its own
// dispatcher, and then delays and context switches run on virtual time instead of real time
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
