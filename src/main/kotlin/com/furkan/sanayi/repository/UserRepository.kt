package com.furkan.sanayi.repository

import com.furkan.sanayi.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
}