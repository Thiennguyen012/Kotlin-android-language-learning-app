package com.englishlearningapp.core.network

import com.google.gson.JsonParser
import com.englishlearningapp.data.remote.dto.auth.ApiResponse
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class BackendResult<T>(
    val data: T,
    val message: String
)

data class BackendActionResult(
    val message: String
)

fun <T> ApiResponse<T>.requireBackendData(): BackendResult<T> {
    if (statusCode !in 200..299 || data == null) throw BackendMessageException(message)
    return BackendResult(data = data, message = message)
}

fun ApiResponse<*>.requireBackendSuccess(): BackendActionResult {
    if (statusCode !in 200..299) throw BackendMessageException(message)
    return BackendActionResult(message)
}

fun Throwable.toBackendMessage(): String {
    if (this is BackendMessageException) return message.orEmpty()
    if (this is SocketTimeoutException) {
        return "The server took too long to respond. Please try again."
    }
    if (this is ConnectException || this is UnknownHostException) {
        return "Unable to connect to the server. Check your connection and try again."
    }
    if (this is HttpException) {
        val backendMessage = runCatching {
            val body = response()?.errorBody()?.string().orEmpty()
            val json = JsonParser.parseString(body).asJsonObject
            json.getAsJsonObject("errors")
                ?.entrySet()
                ?.firstOrNull()
                ?.value
                ?.asJsonArray
                ?.firstOrNull()
                ?.asString
                ?: json.get("message")?.asString
        }.getOrNull()
        if (!backendMessage.isNullOrBlank()) return backendMessage
        return if (code() >= 500) {
            "The server is currently unavailable. Please try again later."
        } else {
            "The request could not be completed. Please try again."
        }
    }
    cause?.takeIf { it !== this }?.let { return it.toBackendMessage() }
    return localizedMessage ?: message ?: javaClass.simpleName
}

private class BackendMessageException(message: String?) :
    Exception(message?.takeIf { it.isNotBlank() } ?: "The server returned an unsuccessful response.")
