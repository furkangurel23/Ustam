package com.furkan.sanayi.dto

import java.time.Instant

data class RatingDto(
    val id: Int,
    val score: Int,
    val comment: String?,
    val createdAt: Instant,
    val userDisplayName: String?,
    val isAnonymous: Boolean
)
