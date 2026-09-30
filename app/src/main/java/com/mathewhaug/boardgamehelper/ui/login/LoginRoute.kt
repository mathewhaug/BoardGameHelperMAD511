package com.mathewhaug.boardgamehelper.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

// the Route is the stateful caller from Week 2, now one line thick: ask for the ViewModel, read
// its state, hand plain values and function references down to the Screen. viewModel() with no
// factory works here because LoginViewModel has a no argument constructor, and the instance it
// returns belongs to this back stack entry, so it survives rotation and is cleared when Login is
// removed from the stack
//
// the signInEvent counter and its LaunchedEffect are gone: a button tap is already an event that
// fires once per click, so there is nothing to de-duplicate, and onSignedIn can be wired straight
// into the Button
@Composable
fun LoginRoute(
    onSignedIn: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    // safeDrawingPadding covers status bars, the display cutout (camera notch), the navigation
    // bar, and the keyboard all in one call - there is no Scaffold on this screen to do it for us,
    // so without this the title would sit under the notch and Sign In would sit under the nav bar
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        LoginForm(
            email = viewModel.email, onEmailChange = viewModel::onEmailChange,
            password = viewModel.password, onPasswordChange = viewModel::onPasswordChange,
            onSignIn = onSignedIn, signInEnabled = viewModel.canSignIn
        )
    }
}
