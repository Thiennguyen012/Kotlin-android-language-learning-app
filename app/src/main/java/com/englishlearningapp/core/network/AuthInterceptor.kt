package com.englishlearningapp.core.network

import com.englishlearningapp.core.session.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor that adds the Authorization header to every request
 * if an access token is available in TokenManager.
 *
 * Header format: "Authorization: Bearer <access_token>"
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Read the token (blocking is OK here — interceptors run on OkHttp's IO thread)
        val token = runBlocking { tokenManager.getAccessToken() }

        // If no token, proceed without the header
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Add the Bearer token header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
