package com.furkan.sanayi

import com.furkan.sanayi.testsupport.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ProviderIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `providers search returns 200`() {
        mockMvc.get("/api/providers") {
            param("sort", "avgScore,desc")
            param("page", "0")
            param("size", "5")
        }.andExpect { status().isOk }
    }

    @Test
    fun `admin can create provider`() {
        val body = """
      {
        "name":"Test From Admin",
        "city":"Ankara",
        "district":"Çankaya",
        "lat":39.9, "lon":32.85,
        "categoryIds":[1]
      }
    """.trimIndent()

        mockMvc.post("/api/admin/providers") {
            with(httpBasic("admin", "admin123"))
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status().isCreated }
    }
}