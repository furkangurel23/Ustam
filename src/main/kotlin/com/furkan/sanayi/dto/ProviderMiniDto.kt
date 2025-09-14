package com.furkan.sanayi.dto

data class ProviderMiniDto(
    val id: Int,
    val name: String,
    val address: String?,
    val city: String?,
    val district: String?,
    val phone: String?,
    val lat: Double?,
    val lon: Double?
)
