package com.furkan.sanayi

import com.furkan.sanayi.testsupport.BaseIntegrationTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

class RateLimitIntegrationTest : BaseIntegrationTest() {
    @Test
    @Disabled
    fun `4th request returns 429`() {
        repeat(3) { i ->
            val payload = """{"providerId":1,"score":3,"comment":"ok","anonymousId":"iptest-$i"}"""
            mockMvc.post("/api/ratings/rate") {
                header("X-Forwarded-For", "1.2.3.4")
                contentType = MediaType.APPLICATION_JSON
                content = payload
            }.andExpect { status { is4xxClientError() } }
        }

        val payload4 = """{"providerId":1,"score":3,"comment":"ok","anonymousId":"iptest-3"}"""
        mockMvc.post("/api/ratings/rate") {
            header("X-Forwarded-For", "1.2.3.4")
            contentType = MediaType.APPLICATION_JSON
            content = payload4
        }.andExpect { status { isTooManyRequests() } }
    }

}
