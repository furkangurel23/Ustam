package com.furkan.sanayi.service

import com.furkan.sanayi.dto.LoginRequest
import com.furkan.sanayi.dto.LoginResponse
import com.furkan.sanayi.security.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val am: AuthenticationManager,
    private val jwtService: JwtService
) {
    fun login(req: LoginRequest): LoginResponse {
        val auth = am.authenticate(UsernamePasswordAuthenticationToken(req.email, req.password))
        val roles = auth.authorities.map { it.authority.removePrefix("ROLE_") }
        val token = jwtService.generate(req.email, roles = roles)
        return LoginResponse(token = token)
    }
}