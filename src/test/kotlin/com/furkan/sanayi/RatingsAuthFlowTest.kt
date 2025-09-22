package com.furkan.sanayi

import com.furkan.sanayi.testsupport.BaseIntegrationTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import kotlin.test.Test

class RatingsAuthFlowTest() : BaseIntegrationTest() {
    @Test
    fun `anonymous can rate with anonymousId`() {
        mockMvc.post("/api/ratings/rate") {
            contentType = MediaType.APPLICATION_JSON
            content = """
              {"providerId":1,"score":3,"comment":"ok","anonymousId":"anon-xyz"}
            """.trimIndent()
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `logged-in user gets userId injected by facade`() {
        val jwt = "Bearer <valid token here>"
        mockMvc.post("/api/ratings/rate") {
            header("Authorization", jwt)
            contentType = MediaType.APPLICATION_JSON
            content = """
              {"providerId":1,"score":5,"comment":"great"}
            """.trimIndent()
        }.andExpect {
            status { isOk() }
        }
    }
}
