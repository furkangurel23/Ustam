package com.furkan.sanayi

import com.furkan.sanayi.web.ClientIpResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest

class ClientIpResolverTest {
    private val r = ClientIpResolver()
    @Test
    fun `takes first from XFF when present`() {
        val req = MockHttpServletRequest().apply {
            addHeader("X-Forwarded-For","9.9.9.9, 10.0.0.1")
            remoteAddr = "127.0.0.1"
        }
        assertEquals("9.9.9.9", r.from(req))
    }
    @Test
    fun `falls back to X-Real-IP`() {
        val req = MockHttpServletRequest()
        req.addHeader("X-Real-IP", "8.8.8.8")
        req.remoteAddr = "127.0.0.1"
        assertEquals("8.8.8.8", r.from(req))
    }

    @Test
    fun `falls back to remoteAddr`() {
        val req = MockHttpServletRequest()
        req.remoteAddr = "127.0.0.1"
        assertEquals("127.0.0.1", r.from(req))
    }
}
