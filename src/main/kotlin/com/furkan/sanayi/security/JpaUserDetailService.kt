package com.furkan.sanayi.security

import com.furkan.sanayi.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class JpaUserDetailService(
    private val userRepo: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val u = userRepo.findByEmailIgnoreCase(username) ?: throw UsernameNotFoundException("User not found: $username")
        val auths = listOf(SimpleGrantedAuthority("ROLE_${u.role.name}"))
        val pwd = u.passwordHash ?: "" // anonimler icin bos olabilir
        return User(
            u.email ?: "",
            pwd,
            u.enabled,
            true,
            true,
            true,
            auths
        )
    }

}