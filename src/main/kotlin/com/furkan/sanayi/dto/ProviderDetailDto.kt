package com.furkan.sanayi.dto

import java.math.BigDecimal

data class ProviderDetailDto(
    val id: Int,
    val name: String,
    val address: String?,
    val city: String?,
    val district: String?,
    val phone: String?,
    val location: LocationDto?,
    val avgScore: BigDecimal?,
    val ratingCount: Int,
    val brands: List<IdNameDto>,
    val categories: List<IdNameDto>,
    val recentRatings: List<RatingDto> = emptyList(),
    val scoreHistogram: Map<Int, Long> = emptyMap()
)
