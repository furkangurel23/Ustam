package com.furkan.sanayi

import com.furkan.sanayi.testsupport.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class GlobalExceptionHandlerTest : BaseIntegrationTest() {

    @Test
    fun `rating with both userId and anonymousId returns 409`() {
        val payload = """
          {"providerId":1,"score":5,"comment":"dup","userId":10,"anonymousId":"a-1"}
        """.trimIndent()

        mockMvc.post("/api/ratings/rate") {
            contentType = MediaType.APPLICATION_JSON
            content = payload
        }.andExpect { status().isConflict }
    }
}
