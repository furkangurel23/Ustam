package com.furkan.sanayi.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwt: JwtService,
    private val uds: JpaUserDetailService
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val h = request.getHeader("Authorization")
        val token = if (h != null && h.startsWith("Bearer ")) h.substring(7) else null

        if (token != null && SecurityContextHolder.getContext().authentication == null) {
            val email = runCatching { jwt.extractEmail(token) }.getOrNull()
            if (!email.isNullOrEmpty()) {
                val ud = uds.loadUserByUsername(email)
                if (jwt.valid(token, ud.username)) {
                    val auth = UsernamePasswordAuthenticationToken(ud, null, ud.authorities).also {
                        it.details = WebAuthenticationDetailsSource().buildDetails(request)
                    }
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}