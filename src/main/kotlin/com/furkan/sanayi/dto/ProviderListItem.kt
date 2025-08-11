package com.furkan.sanayi.dto

data class ProviderListItem(
    val id: Int,
    val name: String,
    val city: String?,
    val district: String?,
    val phone: String?,
    val avgScore: Double?,
    val ratingsCount: Long
)
