package com.furkan.sanayi.repository

import com.furkan.sanayi.domain.Rating
import org.springframework.data.jpa.repository.JpaRepository

interface RatingRepository : JpaRepository<Rating, Int> {
    fun findAllByProviderId(providerId: Int): List<Rating>
    fun existsByProviderIdAndUserId(providerId: Int, userId: Long): Boolean
    fun existsByProviderIdAndAnonymousId(providerId: Int, anonymousId: String): Boolean
}