package com.furkan.sanayi.dto

import java.math.BigDecimal

data class ProviderListItem(
    val id: Int,
    val name: String,
    val city: String?,
    val district: String?,
    val phone: String?,
    val avgScore: BigDecimal?,
    val ratingCount: Int,
    val lat: Double?,
    val lon: Double?
)

