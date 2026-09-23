package com.mathewhaug.boardgamehelper

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mathewhaug.boardgamehelper.ui.theme.StClairTheme

// This is just a composable - nothing in here decides that it floats as a dialog over Home
// instead of replacing the whole screen. That decision lives in App.kt, as metadata attached
// to the ConfirmSignOut entry, which is what keeps this file free of any navigation concern
@Composable
fun SignOutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign out?") },
        text = { Text("You will be returned to the login screen") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sign out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SignOutDialogPreview() {
    StClairTheme {
        SignOutDialog(onConfirm = {}, onDismiss = {})
    }
}
