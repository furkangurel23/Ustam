package com.furkan.sanayi.dto

import java.time.Instant

data class AdminRatingItem(
    val id: Int,
    val providerId: Int,
    val providerName: String,
    val score: Short,
    val comment: String?,
    val createdAt: Instant,
    val userDisplayName: String?,
    val anonymousId: String?,
    val ip: String?,
    val deletedAt: Instant?
)
