package com.furkan.sanayi.dto

data class CategoryDto(
    val id: Int,
    val name: String,
    val providers: List<ProviderMiniDto>
)
