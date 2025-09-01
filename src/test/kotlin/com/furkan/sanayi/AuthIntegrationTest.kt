package com.furkan.sanayi

import com.fasterxml.jackson.databind.json.JsonMapper
import com.furkan.sanayi.testsupport.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AuthIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `login success and can call admin with bearer`() {
        val token = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"admin@sanayi.local","password":"admin123"}"""
        }.andExpect { status().isOk }
            .andReturn().response.contentAsString.let {
                JsonMapper.builder().build()
                    .readTree(it)["token"].asText()
            }

        mockMvc.get("/api/providers")
            .andExpect { status().isOk }

        // admin endpoint: boş body ile sadece 401 yerine 400 vs. gelmesin diye GET olmayan bir uç hedefleme
        // burada oluşturma testini ProviderIntegrationTest yapıyor; burada sadece yetkilendirmeyi deneriz
        mockMvc.post("/api/admin/providers") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """
              {"name":"A","city":"Ankara","district":"Çankaya","lat":39.9,"lon":32.85,"categoryIds":[1]}
            """.trimIndent()
        }.andExpect { status().isCreated }
    }

    @Test
    fun `login fails with wrong password`() {
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"admin@sanayi.local","password":"wrong"}"""
        }.andExpect { status().isUnauthorized }
    }
}
