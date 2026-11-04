package com.mathewhaug.boardgamehelper.ui

import androidx.annotation.StringRes
import com.mathewhaug.boardgamehelper.R
import com.mathewhaug.boardgamehelper.data.AppError

// a resource id rather than a String, so the data layer never needs a Context and the text can
// be translated later without touching any Kotlin. the screen turns it into words with stringResource
@StringRes
fun AppError.userMessage(): Int = when (this) {
    AppError.NoNetwork -> R.string.error_no_network
    AppError.Unauthorized -> R.string.error_unauthorized
    AppError.RateLimited -> R.string.error_rate_limited
    AppError.NotFound -> R.string.error_not_found
    is AppError.Server -> R.string.error_server
    AppError.BadData -> R.string.error_bad_data
}
