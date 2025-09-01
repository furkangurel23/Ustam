package com.furkan.sanayi

import com.furkan.sanayi.testsupport.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
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
    fun `admin can create provider via JWT`() {
        val loginJson = """{"email":"admin@sanayi.local","password":"admin123"}"""
        val token = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = loginJson
        }
            .andExpect { status().isOk }
            .andReturn().response.contentAsString
            .let {
                com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                    .readTree(it)["token"].asText()
            }

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
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status().isCreated }
    }

}