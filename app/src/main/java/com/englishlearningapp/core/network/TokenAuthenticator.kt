package com.englishlearningapp.core.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.englishlearningapp.core.session.TokenManager
import com.englishlearningapp.data.remote.dto.auth.ApiResponse
import com.englishlearningapp.data.remote.dto.auth.LoginResponse
import com.englishlearningapp.data.remote.dto.auth.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.TimeUnit

class TokenAuthenticator(
    private val baseUrl: String,
    private val tokenManager: TokenManager,
    private val gson: Gson
) : Authenticator {

    private val refreshLock = Any()
    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (isAuthenticationRequest(response.request)) return null
        if (responseCount(response) >= MAX_REQUEST_ATTEMPTS) {
            invalidateSession()
            return null
        }

        val failedAccessToken = response.request
            .header(AUTHORIZATION_HEADER)
            ?.removePrefix(BEARER_PREFIX)

        return synchronized(refreshLock) {
            val latestAccessToken = runBlocking {
                tokenManager.getAccessToken()
            }

            if (!latestAccessToken.isNullOrBlank() && latestAccessToken != failedAccessToken) {
                return@synchronized response.request.withAccessToken(latestAccessToken)
            }

            val refreshToken = runBlocking {
                tokenManager.getRefreshToken()
            } ?: return@synchronized invalidateSession()

            val refreshedTokens = requestNewTokens(refreshToken)
                ?: return@synchronized invalidateSession()

            runBlocking {
                tokenManager.saveTokens(
                    accessToken = refreshedTokens.accessToken,
                    refreshToken = refreshedTokens.refreshToken
                )
            }

            response.request.withAccessToken(refreshedTokens.accessToken)
        }
    }

    private fun requestNewTokens(refreshToken: String): LoginResponse? {
        val requestBody = gson
            .toJson(RefreshTokenRequest(refreshToken))
            .toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("${baseUrl}api/admin/auth/refresh")
            .post(requestBody)
            .build()

        return try {
            refreshClient.newCall(request).execute().use { refreshResponse ->
                if (!refreshResponse.isSuccessful) return null

                val responseBody = refreshResponse.body?.string() ?: return null
                val responseType = object : TypeToken<ApiResponse<LoginResponse>>() {}.type
                val apiResponse: ApiResponse<LoginResponse> = gson.fromJson(
                    responseBody,
                    responseType
                )

                if (apiResponse.statusCode == 200) apiResponse.data else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun isAuthenticationRequest(request: Request): Boolean {
        return request.url.encodedPath.endsWith("/api/admin/auth/login") ||
            request.url.encodedPath.endsWith("/api/admin/auth/refresh")
    }

    private fun Request.withAccessToken(accessToken: String): Request {
        return newBuilder()
            .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$accessToken")
            .build()
    }

    private fun invalidateSession(): Request? {
        runCatching {
            runBlocking { tokenManager.clearTokens() }
        }
        return null
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer "
        const val MAX_REQUEST_ATTEMPTS = 2
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

internal fun responseCount(response: Response): Int {
    var count = 1
    var priorResponse = response.priorResponse
    while (priorResponse != null) {
        count++
        priorResponse = priorResponse.priorResponse
    }
    return count
}
