package com.mathewhaug.boardgamehelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mathewhaug.boardgamehelper.navigation.App
import com.mathewhaug.boardgamehelper.ui.theme.StClairTheme
import dagger.hilt.android.AndroidEntryPoint

// hiltViewModel() below this activity needs it to be a hilt entry point to reach the graph
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // the activity just wires up the theme and the map, it holds no logic of its own
            StClairTheme {
                App()
            }
        }
    }
}
