package com.mathewhaug.boardgamehelper.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathewhaug.boardgamehelper.ui.theme.StClairTheme

// Stateless- every value arrives as a parameter, so it renders in the preview with no app running
// this composable has not changed since Week 2. the stateful caller that used to sit above it
// moved out to LoginRoute and grew a ViewModel, and this Screen never noticed - which is exactly
// what the stateless split was buying us back then
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
