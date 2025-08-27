package com.furkan.sanayi.dto

import com.furkan.sanayi.common.exceptions.InvalidRequestException
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*

data class ProviderCreateRequest(
    @field:NotBlank @field:Size(min = 2, max = 120)
    @Schema(example = "Ostim Honda Usta")
    val name: String,

    @field:NotBlank @field:Size(max = 250)
    @Schema(example = "Ankara")
    val city: String,

    @field:NotBlank @field:Size(max = 120)
    @Schema(example = "Yenimahalle")
    val district: String,

    @field:NotBlank @field:Size(max = 1000)
    @Schema(example = "1003. Cad. No:10")
    val address: String,

    @Schema(example = "+90 312 000 0001")
    val phone: String? = null,

    @field:NotNull @field:DecimalMin(value = "-90.0") @field:DecimalMax(value = "90.0")
    @Schema(example = "39.1578", description = "Latitude")
    val lat: Double,

    @field:NotNull @field:DecimalMin(value = "-180.0") @field:DecimalMax(value = "180.0")
    @Schema(example = "39.1578", description = "Longitude")
    val lon: Double,

    @Schema(example = "[1,2]")
    val categoryIds: Set<Int> = emptySet(),

    @Schema(example = "[3,4]")
    val brandIds: Set<Int> = emptySet()
) {
    fun ensureValid() {
        if (categoryIds.isEmpty())
            throw InvalidRequestException("En az bir kategori seçilmelidir.")
    }
}