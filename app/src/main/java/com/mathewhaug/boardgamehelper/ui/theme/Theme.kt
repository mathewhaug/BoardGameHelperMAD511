package com.mathewhaug.boardgamehelper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val StClairBlue = Color(0xFF1A56C4)

// primary is the one role every interactive control reads: button fill, cursor, focused outline
private val StClairColorScheme = lightColorScheme(
    primary = StClairBlue
)

@Composable
fun StClairTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StClairColorScheme,
        content = content
    )
}
