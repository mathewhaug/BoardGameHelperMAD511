package com.mathewhaug.boardgamehelper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathewhaug.boardgamehelper.ui.theme.StClairTheme

//Stateful-owns the state passes values down and receives events back up
@Composable
fun LoginScreen(onSignedIn: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // derivedStateOf reads email and password but only wakes the button when the answer flips
    val canSignIn by remember {
        derivedStateOf { "@" in email && password.length >= 8 }
    }

    // The counter is still the event identity - a new value restarts the effect, so a
    // recomposition triggered by something unrelated never re-fires the sign in
    var signInEvent by remember { mutableIntStateOf(0) }

    LaunchedEffect(signInEvent) {
        if (signInEvent > 0) {
            // Navigating here instead of showing a snackbar is a deliberate choice - showSnackbar
            // suspends until the snackbar is dismissed, so calling it and navigating after it
            // would leave the user staring at the login screen for several seconds first
            onSignedIn()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LoginForm(
            email = email, onEmailChange = { email = it },
            password = password, onPasswordChange = { password = it },
            onSignIn = { signInEvent++ }, signInEnabled = canSignIn
        )
    }
}

// Stateless- every value arrives as a parameter, so it renders in the preview with no app running
@Composable
fun LoginForm(
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit, signInEnabled: Boolean
) {
    // Derived from the parameters - not stored, so there is nothing to remember here
    // Only flag an error once the user has actually typed something, so the form
    // does not greet a brand new user with two red fields before they type a key
    val emailError = email.isNotEmpty() && "@" !in email
    val passwordError = password.isNotEmpty() && password.length < 8

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // No image asset for a logo, so a styled title stands in for one here
        Text(
            text = "St. Clair Board Game Helper",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            isError = emailError,
            supportingText = { if (emailError) Text("Email must contain @") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            isError = passwordError,
            supportingText = { if (passwordError) Text("Password must be at least 8 characters") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onSignIn,
            enabled = signInEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign In")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginFormPreview() {
    StClairTheme {
        LoginForm(
            email = "mhaug@stclaircollege.ca", onEmailChange = {},
            password = "composeui", onPasswordChange = {},
            onSignIn = {}, signInEnabled = true
        )
    }
}
