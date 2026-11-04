package com.mathewhaug.boardgamehelper.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mathewhaug.boardgamehelper.R
import com.mathewhaug.boardgamehelper.data.AppError
import com.mathewhaug.boardgamehelper.data.Game
import com.mathewhaug.boardgamehelper.data.SampleGames
import com.mathewhaug.boardgamehelper.ui.theme.StClairTheme
import com.mathewhaug.boardgamehelper.ui.userMessage

// This screen never sees the back stack or the ViewModel, it only reports what the user did
// through its lambdas. Everything it draws arrives in one HomeUiState, so the whole screen can be
// put into any state you like just by building that object, which is what lets it preview with
// no app running and no NavDisplay in sight
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onGameClick: (Int) -> Unit,
    onToggleShortlist: (Game) -> Unit,
    onOpenShortlist: () -> Unit,
    onSignOut: () -> Unit,
    onRefresh: () -> Unit,
    onNoticeShown: () -> Unit,
) {
    // which snackbar is on screen is pure UI state, so it is fine for it to live here
    val snackbarHostState = remember { SnackbarHostState() }

    // the notice is a one off: show it, then tell the ViewModel so it is cleared and does not come
    // back after a rotation. keyed on the notice, so a new failure gets its own snackbar
    val notice = (uiState as? HomeUiState.Success)?.notice
    val noticeText = notice?.let { stringResource(it.userMessage()) }
    LaunchedEffect(notice) {
        if (noticeText != null) {
            snackbarHostState.showSnackbar(noticeText)
            onNoticeShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Board Game Helper") },
                actions = {
                    TextButton(onClick = onOpenShortlist) {
                        Text("Shortlist")
                    }
                    TextButton(onClick = onSignOut) {
                        Text("Sign out")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BggCredit() },
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding)
        // an exhaustive when over the sealed interface: add a fifth state to HomeUiState and this
        // stops compiling until it is drawn
        when (uiState) {
            HomeUiState.Loading -> Box(modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            HomeUiState.Empty -> Box(modifier, contentAlignment = Alignment.Center) {
                Text("No games to show yet")
            }
            is HomeUiState.Error -> Column(
                modifier = modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(uiState.error.userMessage()), textAlign = TextAlign.Center)
                Button(onClick = onRefresh) {
                    Text("Retry")
                }
            }
            is HomeUiState.Success -> PullToRefreshBox(
                isRefreshing = uiState.refreshing,
                onRefresh = onRefresh,
                modifier = modifier,
            ) {
                GameList(uiState, onGameClick, onToggleShortlist)
            }
        }
    }
}

@Composable
private fun GameList(
    uiState: HomeUiState.Success,
    onGameClick: (Int) -> Unit,
    onToggleShortlist: (Game) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // key gives each row a stable identity tied to the game, not its position in the
        // list, so remembered state (scroll position, a future expanded flag) follows the
        // item instead of the slot, the same call site identity idea from Week 2
        items(uiState.games, key = { it.id }) { game ->
            // the row does not store whether it is shortlisted, it asks the state, so there
            // is no local flag that could drift out of step with the repository
            val shortlisted = game.id in uiState.shortlistIds
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onGameClick(game.id) }
            ) {
                ListItem(
                    leadingContent = { GameThumbnail(game) },
                    headlineContent = { Text(game.name) },
                    supportingContent = {
                        Text("${game.minPlayers} to ${game.maxPlayers} players, ${game.playTimeMinutes} min")
                    },
                    trailingContent = {
                        IconButton(onClick = { onToggleShortlist(game) }) {
                            Icon(
                                imageVector = if (shortlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (shortlisted) "Remove from shortlist" else "Add to shortlist"
                            )
                        }
                    }
                )
            }
        }
    }
}

// Coil fetches these with its own OkHttpClient, the one coil-network-okhttp builds for it, and
// never the one in NetworkModule. the images come from BGG's image host, not the API host, and
// our AuthInterceptor would attach the BGG token to every one of those requests. a null url
// skips the network entirely and goes straight to the error image
@Composable
private fun GameThumbnail(game: Game) {
    AsyncImage(
        model = game.thumbnailUrl,
        contentDescription = "Box art for ${game.name}",
        placeholder = painterResource(R.drawable.thumbnail_placeholder),
        error = painterResource(R.drawable.thumbnail_error),
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small),
    )
}

// BGG's terms ask apps that use the XML API to credit it, so this sits under every state
@Composable
private fun BggCredit() {
    // the official "Powered by BGG" logo goes here, in place of or beside this text
    Text(
        text = "Powered by BGG",
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(vertical = 8.dp),
    )
}

// one preview per state, built from a UiState literal: no ViewModel, no repository, no network
@Composable
private fun HomeScreenPreview(uiState: HomeUiState) {
    StClairTheme {
        HomeScreen(
            uiState = uiState,
            onGameClick = {},
            onToggleShortlist = {},
            onOpenShortlist = {},
            onSignOut = {},
            onRefresh = {},
            onNoticeShown = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenLoadingPreview() = HomeScreenPreview(HomeUiState.Loading)

@Preview(showBackground = true)
@Composable
fun HomeScreenEmptyPreview() = HomeScreenPreview(HomeUiState.Empty)

@Preview(showBackground = true)
@Composable
fun HomeScreenErrorPreview() = HomeScreenPreview(HomeUiState.Error(AppError.NoNetwork))

@Preview(showBackground = true)
@Composable
fun HomeScreenSuccessPreview() = HomeScreenPreview(
    HomeUiState.Success(games = SampleGames, shortlistIds = setOf(SampleGames.first().id))
)
