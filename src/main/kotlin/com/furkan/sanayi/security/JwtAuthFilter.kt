package com.furkan.sanayi.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
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
            if (!email.isNullOrEmpty() && SecurityContextHolder.getContext().authentication == null) {
                val tokenRoles = jwt.extractRoles(token)
                val ud = uds.loadUserByUsername(email)

                val authorities = if (tokenRoles.isNotEmpty())
                    tokenRoles.map { SimpleGrantedAuthority("ROLE_${it.uppercase()}") }
                else ud.authorities

                val auth = UsernamePasswordAuthenticationToken(ud.username, null, authorities).also {
                    it.details = WebAuthenticationDetailsSource().buildDetails(request)
                }

                SecurityContextHolder.getContext().authentication = auth
            }
        }
        filterChain.doFilter(request, response)
    }
}