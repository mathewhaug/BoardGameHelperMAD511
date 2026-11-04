package com.mathewhaug.boardgamehelper.data

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

// every way a network call can fail, in the app's own words. the screens only ever see one of
// these, never an IOException or a status code, and the when over a sealed interface means a new
// case cannot be added without every screen being told how to show it
sealed interface AppError {
    data object NoNetwork : AppError
    data object Unauthorized : AppError
    data object RateLimited : AppError
    data object NotFound : AppError
    data class Server(val code: Int) : AppError
    data object BadData : AppError
}

// Result can only carry a Throwable, so this is the wrapper that gets an AppError through it
class AppException(val error: AppError) : Exception(error.toString())

// runs one network operation and turns whatever goes wrong into an AppError, so no exception
// ever escapes into a ViewModel. the order of the catches matters:
// - CancellationException first and rethrown: it is how a coroutine is told to stop, and if it
//   were swallowed here the coroutine would carry on after its screen had already gone
// - IOException is the connection itself failing: no signal, DNS, a timeout
// - HttpException means the server answered, just not with a 2xx
// - anything else is something we could not read, a parser error most often
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(AppException(AppError.NoNetwork))
    } catch (e: HttpException) {
        Result.failure(AppException(e.toAppError()))
    } catch (e: Exception) {
        Result.failure(AppException(AppError.BadData))
    }

// BGG throttles with 429, but under load it also answers 500 or 503, and in practice all three
// mean the same thing: back off and try again later
fun HttpException.toAppError(): AppError = when (code()) {
    401 -> AppError.Unauthorized
    404 -> AppError.NotFound
    429, 500, 503 -> AppError.RateLimited
    else -> AppError.Server(code())
}
