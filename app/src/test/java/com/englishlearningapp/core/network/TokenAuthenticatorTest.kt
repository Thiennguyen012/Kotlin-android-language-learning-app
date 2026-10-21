package com.englishlearningapp.core.network

import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Test

class TokenAuthenticatorTest {

    @Test
    fun countsOriginalRequestAsFirstAttempt() {
        val response = createUnauthorizedResponse()

        assertEquals(1, responseCount(response))
    }

    @Test
    fun countsRetriedRequestAsSecondAttempt() {
        val firstResponse = createUnauthorizedResponse()
        val secondResponse = createUnauthorizedResponse(priorResponse = firstResponse)

        assertEquals(2, responseCount(secondResponse))
    }

    private fun createUnauthorizedResponse(
        priorResponse: Response? = null
    ): Response {
        val request = Request.Builder()
            .url("http://localhost/api/admin/tests")
            .build()

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .priorResponse(priorResponse)
            .build()
    }
}
