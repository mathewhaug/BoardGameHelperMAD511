package com.mathewhaug.boardgamehelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mathewhaug.boardgamehelper.ui.theme.StClairTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StClairTheme {
                LoginScreen()
            }
        }
    }
}
