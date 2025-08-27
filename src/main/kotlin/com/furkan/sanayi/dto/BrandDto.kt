package com.furkan.sanayi.dto

data class BrandDto(
    val id: Int,
    val name: String,
    val providers: List<ProviderMiniDto>
)
