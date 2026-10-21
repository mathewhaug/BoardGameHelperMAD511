package com.mathewhaug.boardgamehelper.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// @HiltAndroidApp makes hilt generate the singleton component and attach it to this Application,
// so the graph lives exactly as long as the process. it still has to be registered in the
// manifest with android:name, otherwise the system builds the stock Application and hilt never starts
@HiltAndroidApp
class BoardGameApp : Application()
