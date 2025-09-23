package com.furkan.sanayi.bootstrap

import com.furkan.sanayi.domain.User
import com.furkan.sanayi.repository.UserRepository
import com.furkan.sanayi.security.Role
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder

@Profile("local","dev")
@Configuration
class DataInitializer {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun seedAdmin(userRepo: UserRepository, encoder: PasswordEncoder) = ApplicationRunner {
        val email = "admin@sanayi.local"
        if (userRepo.findByEmailIgnoreCase(email) == null) {
            val u = User(
                email = email,
                passwordHash = encoder.encode("admin123"),
                displayName = "Super Admin",
                role = Role.ADMIN,
                enabled = true
            )
            userRepo.save(u)
            log.info("Seeded admin -> {} / {}", email, "admin123")
        }
    }
}