package com.furkan.sanayi.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.jwt.secret}") secret: String,
    @Value("\${app.jwt.expiration-ms}") private val expirationMs: Long
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun extractEmail(token: String): String? = claims(token = token).subject

    fun generate(email: String, roles: Collection<String>): String {
        val now = Date()
        return Jwts.builder()
            .setSubject(email)
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + expirationMs))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun valid(token: String, email: String): Boolean {
        val c = claims(token)
        return c.subject == email && c.expiration.after(Date())
    }


    private fun claims(token: String): Claims =
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
}