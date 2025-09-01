package com.furkan.sanayi

import com.furkan.sanayi.security.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JwtServiceUnitTest {
    @Test
    fun `generate and extract email`() {
        val secret = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
        val svc = JwtService(secret, 60_000)

        val token = svc.generate("user@ex.com", roles = emptyList())
        assertEquals("user@ex.com", svc.extractEmail(token))
        assertTrue(svc.valid(token, "user@ex.com"))
    }
}
