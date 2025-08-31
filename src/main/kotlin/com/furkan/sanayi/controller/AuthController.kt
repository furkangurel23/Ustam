package com.furkan.sanayi.controller

import com.furkan.sanayi.dto.LoginRequest
import com.furkan.sanayi.dto.LoginResponse
import com.furkan.sanayi.security.JwtService
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val am: AuthenticationManager,
    private val jwt: JwtService
) {
    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<LoginResponse> {
        am.authenticate(UsernamePasswordAuthenticationToken(req.email, req.password))
        val token = jwt.generate(req.email, roles = emptyList())
        return ResponseEntity.ok(LoginResponse(token))
    }
}