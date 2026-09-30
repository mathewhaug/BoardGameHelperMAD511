package com.mathewhaug.boardgamehelper.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// the two remember { mutableStateOf("") } lines from Week 2 moved here, and that is the whole
// change: Compose state is perfectly fine inside a ViewModel, it just no longer needs remember,
// because the ViewModel itself is now the thing that survives recomposition and rotation
//
// deliberately no Android imports beyond androidx.lifecycle - no Context, no resources, no Toast -
// so this class can be constructed and exercised on a plain JVM
class LoginViewModel : ViewModel() {

    // private set is the property version of the backing property idea: anyone can read email,
    // only this class can write it, and every write goes through a named function below, so the
    // ViewModel stays the single place that decides how the form changes
    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    fun onEmailChange(value: String) {
        email = value
    }

    fun onPasswordChange(value: String) {
        password = value
    }

    // computed, not stored. reading it reads email and password, so Compose tracks those two and
    // recomposes the button when the answer flips - the derivedStateOf from Week 2 is not needed
    val canSignIn: Boolean
        get() = "@" in email && password.length >= 8
}
