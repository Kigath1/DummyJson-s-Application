package com.xmlapplication.core.network


import com.xmlapplication.auth.models.RefreshTokenRequest
import com.xmlapplication.core.local.TokenManager
import com.xmlapplication.auth.network.AuthApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    baseRetrofit: Retrofit
) : Authenticator {

    // Built from the NON-authenticated Retrofit instance passed in,
    // so calling refresh() here can never trigger this same Authenticator again.
    private val refreshApi: AuthApiService = baseRetrofit.create(AuthApiService::class.java)

    override fun authenticate(route: Route?, response: Response): Request? {
        // Loop guard: if we've already retried this call once, stop.
        // Without this, a permanently-invalid refresh token retries forever.
        if (responseCount(response) >= 2) {
            tokenManager.clearTokens()
            return null
        }

        // Don't try to refresh a 401 that came from the refresh call itself.
        if (response.request.url.encodedPath.contains("auth/refresh")) {
            tokenManager.clearTokens()
            return null
        }

        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            tokenManager.clearTokens()
            return null
        }

        // Authenticator runs on OkHttp's background thread, not a coroutine,
        // so we bridge into the suspend refresh() call with runBlocking.
        val newTokens = try {
            runBlocking {
                refreshApi.tokenRefresh(RefreshTokenRequest(refreshToken = refreshToken))
            }
        } catch (e: Exception) {
            null
        }

        if (newTokens == null) {
            tokenManager.clearTokens()
            return null
        }

        tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)

        // Critical: the retried request must carry the new token directly.
        // The retry does NOT pass back through the app-level auth interceptor,
        // so setting it here is the only place it gets attached.
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}