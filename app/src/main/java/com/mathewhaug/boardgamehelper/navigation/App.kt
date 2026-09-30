package com.mathewhaug.boardgamehelper.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.mathewhaug.boardgamehelper.SignOutDialog
import com.mathewhaug.boardgamehelper.ui.detail.GameDetailRoute
import com.mathewhaug.boardgamehelper.ui.home.HomeRoute
import com.mathewhaug.boardgamehelper.ui.login.LoginRoute
import com.mathewhaug.boardgamehelper.ui.shortlist.ShortlistRoute

// This is the only file that knows the back stack exists - every Route it hosts only takes lambdas
// and reports what happened, App is where those reports turn into real navigation moves. App() now
// knows only keys and Routes: the SampleGames lookup moved into the ViewModels, behind the repository
@Composable
fun App() {
    // backStack is a snapshot state list that the app owns and NavDisplay only renders.
    // rememberNavBackStack rather than mutableStateListOf so the list of NavKeys is saved to the
    // instance state Bundle and survives rotation instead of resetting to Login every time
    val backStack = rememberNavBackStack(Login)

    NavDisplay(
        backStack = backStack,
        // this is what the system Back button and the predictive back gesture both call into -
        // Nav3 does not decide what Back means, we do, just by writing this one lambda
        onBack = { backStack.removeLastOrNull() },
        // the second decorator gives each back stack entry its own ViewModelStore. without it every
        // ViewModel would be scoped to the Activity, so opening game 42 after game 13 would reuse
        // game 13's GameDetailViewModel, id and all. with it, each entry owns and clears its own
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        sceneStrategies = listOf(remember { DialogSceneStrategy() }),
        // forward slides the new screen in from the right, back mirrors it from the left
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
            // clearing the stack before adding Home is the Nav3 way of writing
            // popUpTo(Login) { inclusive = true }, otherwise Back from Home would land on the
            // login screen instead of exiting the app like a signed in user would expect
            entry<Login> { LoginRoute(onSignedIn = { backStack.clear(); backStack.add(Home) }) }
            entry<Home> {
                HomeRoute(
                    onGameClick = { backStack.add(GameDetail(it)) },
                    onOpenShortlist = { backStack.add(Shortlist) },
                    onSignOut = { backStack.add(ConfirmSignOut) },
                )
            }
            entry<Shortlist> { ShortlistRoute(onBack = { backStack.removeLastOrNull() }) }
            entry<GameDetail> { key -> GameDetailRoute(key.id, onBack = { backStack.removeLastOrNull() }) }
            entry<ConfirmSignOut>(metadata = DialogSceneStrategy.dialog()) {
                // dialog() metadata is what makes NavDisplay float this entry over Home instead of
                // replacing it, and Back dismisses it for free because Back just pops the top entry
                SignOutDialog(
                    onConfirm = { backStack.clear(); backStack.add(Login) },
                    onDismiss = { backStack.removeLastOrNull() },
                )
            }
        }
    )
}
