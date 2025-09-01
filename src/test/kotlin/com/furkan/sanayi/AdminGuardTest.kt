package com.furkan.sanayi

import com.furkan.sanayi.domain.User
import com.furkan.sanayi.repository.UserRepository
import com.furkan.sanayi.security.JwtService
import com.furkan.sanayi.security.Role
import com.furkan.sanayi.testsupport.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.post

class AdminGuardTest : BaseIntegrationTest() {

    @Autowired
    lateinit var userRepo: UserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwt: JwtService

    @Test
    fun `anon get 401 on admin endpoint`() {
        mockMvc.post("/api/admin/providers") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"X","city":"A","district":"B","lat":1,"lon":1,"categoryIds":[1]}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `user role gets 403 on admin endpoint`() {
        val u = userRepo.save(
            User().apply {
                email = "user@sanayi.local"
                passwordHash = passwordEncoder.encode("p")
                displayName = "User"
                role = Role.USER
                enabled = true
            }
        )
        val token = jwt.generate(u.email!!, roles = listOf("USER"))

        mockMvc.post("/api/admin/providers") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"X","city":"A","district":"B","lat":1,"lon":1,"categoryIds":[1]}"""
        }.andExpect {
            status { isForbidden() }
        }
    }
}
