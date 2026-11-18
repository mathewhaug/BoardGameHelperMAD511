package com.mathewhaug.boardgamehelper.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import com.mathewhaug.boardgamehelper.battery.BatteryReceiver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mathewhaug.boardgamehelper.timer.hasNotificationPermission

// hiltViewModel() replaces the hand written factory. hilt already knows how to build a
// HomeViewModel from its @Inject constructor, and it still scopes the instance to this back stack
// entry through the ViewModelStore the nav entry decorator gives it
// the Route collects the ViewModel's state and hands plain values down. HomeScreen takes a
// HomeUiState and lambdas, never a ViewModel, which is what keeps it previewable
@Composable
fun HomeRoute(
    onGameClick: (Int) -> Unit,
    onOpenShortlist: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // collectAsStateWithLifecycle stops collecting when the Activity drops below STARTED and picks
    // up again when it comes back, where the older collectAsState() keeps collecting in the
    // background the whole time. paired with WhileSubscribed in the ViewModel, that is what lets
    // the repository query actually go idle while the app is not on screen
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gameNight by viewModel.gameNight.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // registered on start and gone on stop, so nothing listens while Home is off screen and the
    // receiver can never outlive the Activity it was registered on
    //
    // battery low and battery okay are not sticky broadcasts: the system sends each one once, at
    // the moment the level crosses the line, and a receiver registered later never hears about
    // it. so the banner only appears when the state changes while this screen is on, opening
    // Home on an already low battery shows nothing until it crosses back
    LifecycleStartEffect(Unit) {
        val receiver = BatteryReceiver(viewModel::setLowBattery)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        onStopOrDispose { context.unregisterReceiver(receiver) }
    }

    // checked again on every resume, because the user can flip it in Settings and come back
    var notificationsAllowed by remember { mutableStateOf(hasNotificationPermission(context)) }
    LifecycleResumeEffect(Unit) {
        notificationsAllowed = hasNotificationPermission(context)
        onPauseOrDispose { }
    }

    // after a second no the system stops showing the dialog and answers no straight away. that
    // shows up as a denial with no rationale on offer, and from then on we stop asking and offer
    // the settings page instead. saveable so a rotation does not forget and ask again
    var notificationsBlocked by rememberSaveable { mutableStateOf(false) }

    // whatever the user answers, the timer starts. the notification is only a window onto the
    // timer, so saying no to it must never take the timer away
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsAllowed = granted
        notificationsBlocked = !granted && context.findActivity()
            ?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == false
        viewModel.startTimer()
    }

    val onStartTimer = {
        val shouldAsk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasNotificationPermission(context) && !notificationsBlocked
        if (shouldAsk) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.startTimer()
        }
    }

    // the one way back once the system has stopped asking: the app's own page in Settings, where
    // the user can switch notifications on by hand
    val onOpenSettings = {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", context.packageName, null))
        )
    }

    HomeScreen(
        uiState = uiState,
        onGameClick = onGameClick,
        onToggleShortlist = viewModel::toggleShortlist,
        onOpenShortlist = onOpenShortlist,
        onSignOut = onSignOut,
        onRefresh = viewModel::refresh,
        onNoticeShown = viewModel::onNoticeShown,
        gameNight = gameNight,
        notificationHidden = gameNight.elapsedSeconds != null && !notificationsAllowed,
        showOpenSettings = notificationsBlocked && !notificationsAllowed,
        onStartTimer = onStartTimer,
        onStopTimer = viewModel::stopTimer,
        onOpenSettings = onOpenSettings,
    )
}

// LocalContext is usually the Activity, but it can be wrapped (a themed context, for one), and
// shouldShowRequestPermissionRationale only exists on Activity, so unwrap until we find it
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
