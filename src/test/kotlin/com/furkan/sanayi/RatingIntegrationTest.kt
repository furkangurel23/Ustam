package com.furkan.sanayi

import com.furkan.sanayi.testsupport.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class RatingIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `duplicate anonymous rating returns 409`() {
        val provider = createProvider(name = "usta 1")
        val providerId = provider.id
        val anonId = "anon-tc-0012"

        val payload = """
            {
              "providerId": $providerId,
              "score": 3,
              "comment": "ilk oy",
              "anonymousId": "$anonId",
              "ip": "2.3.4.5"
            }
        """.trimIndent()

        // 1. oy
        mockMvc.post("/api/ratings/rate") {
            contentType = MediaType.APPLICATION_JSON
            content = payload
        }.andExpect { status().is2xxSuccessful }

        // 2. oy (aynı anon aynı provider) -> 409
        mockMvc.post("/api/ratings/rate") {
            contentType = MediaType.APPLICATION_JSON
            content = payload
        }.andExpect { status().isConflict }
    }
}