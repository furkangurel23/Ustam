package com.furkan.sanayi

import com.furkan.sanayi.testsupport.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class RateLimitIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `4th rating request within window returns 429`() {
        val provider = createProvider(name = "usta 2")
        val providerId = provider.id
        val payload = """
          {"providerId":$providerId,"score":3,"comment":"ok","anonymousId":"iptest","ip":null}
        """.trimIndent()

        mockMvc.post("/api/ratings/rate") {
            header("X-Forwarded-For", "1.2.3.4")
            contentType = MediaType.APPLICATION_JSON
            content = payload
        }.andExpect { status { is2xxSuccessful() } }

        repeat(2) {
            mockMvc.post("/api/ratings/rate") {
                header("X-Forwarded-For", "1.2.3.4")
                contentType = MediaType.APPLICATION_JSON
                content = payload
            }.andExpect { status().is4xxClientError } // ilkleri ya 201 ya 409 olabilir
        }

        // 4. istek 429 olmalı
        mockMvc.post("/api/ratings/rate") {
            header("X-Forwarded-For", "1.2.3.4")
            contentType = MediaType.APPLICATION_JSON
            content = payload
        }.andExpect { status().isTooManyRequests }
    }
}
