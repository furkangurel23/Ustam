package com.furkan.sanayi.controller

import com.furkan.sanayi.dto.LoginRequest
import com.furkan.sanayi.dto.LoginResponse
import com.furkan.sanayi.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(authService.login(req))
    }
}