package com.furkan.sanayi.dto

data class ProviderNearItem(
    val id: Int,
    val name: String,
    val city: String?,
    val district: String?,
    val avgScore: Double?,
    val ratingCount: Long?,
    val lat: Double?,
    val lon: Double?,
    val distanceKm: Double
)
