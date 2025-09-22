package com.furkan.sanayi.security

import com.furkan.sanayi.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityFacadeImpl(
    private val userRepository: UserRepository
) : SecurityFacade {
    override fun idOrNull(): Long? {
        val email = emailOrNull() ?: return null
        return userRepository.findByEmailIgnoreCase(email)?.id
    }

    override fun emailOrNull(): String? {
        val auth = SecurityContextHolder.getContext().authentication ?: return null
        if (!auth.isAuthenticated) return null
        val name = auth.name?.trim()
        return name?.takeIf { it.isNotEmpty() }
    }

    override fun isAuthenticated(): Boolean = emailOrNull() != null

}