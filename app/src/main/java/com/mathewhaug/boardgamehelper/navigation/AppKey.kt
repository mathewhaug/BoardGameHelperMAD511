package com.mathewhaug.boardgamehelper.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// This one file is the whole map of the app - every destination the back stack can ever hold is
// listed right here, nowhere else. NavKey plus @Serializable is what lets rememberNavBackStack
// save the stack to a Bundle, so the back stack survives rotation and process death instead of
// resetting to Login every time. A sealed interface also means a when over AppKey is exhaustive,
// so adding a new screen without mapping it in App.kt is a compile error, not a runtime crash
@Serializable
sealed interface AppKey : NavKey

@Serializable data object Login : AppKey
@Serializable data object Home : AppKey
@Serializable data class GameDetail(val id: Int) : AppKey
@Serializable data object ConfirmSignOut : AppKey
