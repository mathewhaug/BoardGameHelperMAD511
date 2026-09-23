package com.mathewhaug.boardgamehelper

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.mathewhaug.boardgamehelper.data.SampleGames
import com.mathewhaug.boardgamehelper.navigation.ConfirmSignOut
import com.mathewhaug.boardgamehelper.navigation.GameDetail
import com.mathewhaug.boardgamehelper.navigation.Home
import com.mathewhaug.boardgamehelper.navigation.Login

// This is the only file that knows the back stack exists - every screen it hosts only takes
// lambdas and reports what happened, App is where those reports turn into real navigation moves
@Composable
fun App() {
    // backStack is a snapshot state list that the app owns - NavDisplay only reads and renders
    // it, it never mutates it by itself. rememberNavBackStack is used here instead of plain
    // remember { mutableStateListOf() } because it also survives rotation: the list of NavKeys
    // gets saved to the saved instance state Bundle and restored from it, where a bare
    // mutableStateListOf would reset back to Login on every configuration change
    val backStack = rememberNavBackStack(Login)

    NavDisplay(
        backStack = backStack,
        // this is what the system Back button and the predictive back gesture both call into -
        // Nav3 does not decide what Back means, we do, just by writing this one lambda
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(remember { DialogSceneStrategy() }),
        // forward slides the new screen in from the right, back mirrors that by sliding the
        // previous screen back in from the left as the current one exits to the right
        transitionSpec = {
            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
        },
        popTransitionSpec = {
            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
        },
        predictivePopTransitionSpec = {
            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
        },
        entryProvider = entryProvider {
            entry<Login> {
                // clearing the stack before adding Home is the Nav3 way of writing
                // popUpTo(Login) { inclusive = true } - skip it and Back from Home would just
                // return to the login screen instead of exiting the app like a signed in user
                // would expect
                LoginScreen(onSignedIn = { backStack.clear(); backStack.add(Home) })
            }
            entry<Home> {
                HomeScreen(
                    games = SampleGames,
                    onGameClick = { id -> backStack.add(GameDetail(id)) },
                    onSignOut = { backStack.add(ConfirmSignOut) },
                )
            }
            entry<GameDetail> { key ->
                // key already carries a typed Int id, so there is no string parsing the way
                // there would be with Navigation 2 route arguments. The lookup lives here, in
                // App, rather than in the screen, because App is the piece that owns the list of
                // games - GameDetailScreen just renders whatever Game it is handed
                val game = SampleGames.first { it.id == key.id }
                GameDetailScreen(game = game, onBack = { backStack.removeLastOrNull() })
            }
            entry<ConfirmSignOut>(metadata = DialogSceneStrategy.dialog()) {
                // this dialog() metadata is what makes NavDisplay float this entry over Home in
                // a Dialog instead of replacing it outright, and Back dismisses it for free,
                // because Back just pops the top entry no matter what it is
                SignOutDialog(
                    onConfirm = { backStack.clear(); backStack.add(Login) },
                    onDismiss = { backStack.removeLastOrNull() },
                )
            }
        }
    )
}
