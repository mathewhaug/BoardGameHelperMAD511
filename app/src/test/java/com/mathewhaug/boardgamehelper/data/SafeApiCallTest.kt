package com.mathewhaug.boardgamehelper.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SafeApiCallTest {

    // the same exception Retrofit throws for a non 2xx answer, built without a server
    private fun http(code: Int) = HttpException(Response.error<Unit>(code, "".toResponseBody(null)))

    private suspend fun errorFrom(throwable: Throwable): AppError {
        val result = safeApiCall<Unit> { throw throwable }
        return (result.exceptionOrNull() as AppException).error
    }

    @Test fun success_isWrapped() = runTest {
        assertEquals(Result.success(42), safeApiCall { 42 })
    }

    @Test fun ioException_isNoNetwork() = runTest {
        assertEquals(AppError.NoNetwork, errorFrom(IOException("no route to host")))
    }

    @Test fun http401_isUnauthorized() = runTest {
        assertEquals(AppError.Unauthorized, errorFrom(http(401)))
    }

    @Test fun http404_isNotFound() = runTest {
        assertEquals(AppError.NotFound, errorFrom(http(404)))
    }

    @Test fun http429and500and503_areRateLimited() = runTest {
        listOf(429, 500, 503).forEach { assertEquals(AppError.RateLimited, errorFrom(http(it))) }
    }

    @Test fun otherHttpCodes_areServerWithTheCode() = runTest {
        assertEquals(AppError.Server(502), errorFrom(http(502)))
        assertEquals(AppError.Server(400), errorFrom(http(400)))
    }

    @Test fun anythingElse_isBadData() = runTest {
        assertEquals(AppError.BadData, errorFrom(IllegalStateException("bad xml")))
    }

    // if this were swallowed into a Result, a cancelled refresh would keep going after its screen
    // was gone, so it has to come back out as the same exception
    @Test fun cancellation_isRethrown() = runTest {
        val cancel = CancellationException("screen closed")
        try {
            safeApiCall<Unit> { throw cancel }
            fail("CancellationException should have been rethrown")
        } catch (e: CancellationException) {
            assertEquals(cancel, e)
        }
    }
}
