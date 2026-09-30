package com.mathewhaug.boardgamehelper.di

import android.app.Application

// Application is the one object that outlives every Activity, so it is the natural owner of the
// container: build it once here and every screen shares the same repository for the life of the
// process. it has to be registered in the manifest with android:name, otherwise the system keeps
// using the stock Application and container is never initialised
class BoardGameApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
