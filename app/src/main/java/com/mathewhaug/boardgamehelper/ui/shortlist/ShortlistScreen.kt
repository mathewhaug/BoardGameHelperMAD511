package com.mathewhaug.boardgamehelper.ui.shortlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathewhaug.boardgamehelper.data.Game
import com.mathewhaug.boardgamehelper.data.SampleGames
import com.mathewhaug.boardgamehelper.ui.theme.StClairTheme

// stateless like every other Screen: a list in, two lambdas out. it does not know the list came
// from a StateFlow, and it would render exactly the same if the Route handed it a hard coded one
//
// the search box is the same hoisting as the login form: the text arrives as query and every
// keystroke goes back out through onQueryChange. the screen never filters anything itself, the
// games it is handed are already the matches
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortlistScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    games: List<Game>,
    onRemove: (Game) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shortlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Search shortlist") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                // the clear button only exists while there is something to clear
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (games.isEmpty()) {
                // the empty case is drawn on purpose rather than left as a blank screen, so the user
                // can tell "nothing here yet" apart from "still loading" or "something broke".
                // an empty list means two different things once there is a query, and the message
                // should say which one, otherwise a typo looks like the shortlist was wiped
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (query.isNotBlank()) "No games match" else "Nothing shortlisted yet")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // key by id again, so when a row is removed the rows below it move up as the same
                    // items rather than being rebuilt from scratch in their new slots
                    items(games, key = { it.id }) { game ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            ListItem(
                                headlineContent = { Text(game.name) },
                                supportingContent = {
                                    Text("${game.minPlayers} to ${game.maxPlayers} players, ${game.playTimeMinutes} min")
                                },
                                trailingContent = {
                                    IconButton(onClick = { onRemove(game) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Remove ${game.name}")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShortlistScreenPreview() {
    StClairTheme {
        ShortlistScreen(
            query = "",
            onQueryChange = {},
            games = SampleGames.take(2),
            onRemove = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ShortlistScreenEmptyPreview() {
    StClairTheme {
        ShortlistScreen(
            query = "",
            onQueryChange = {},
            games = emptyList(),
            onRemove = {},
            onBack = {}
        )
    }
}

// same empty list, but with a query, so this one shows the "No games match" text and the clear icon
@Preview(showBackground = true)
@Composable
fun ShortlistScreenNoMatchPreview() {
    StClairTheme {
        ShortlistScreen(
            query = "zzz",
            onQueryChange = {},
            games = emptyList(),
            onRemove = {},
            onBack = {}
        )
    }
}
