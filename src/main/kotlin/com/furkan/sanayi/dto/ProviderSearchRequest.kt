package com.furkan.sanayi.dto

import com.furkan.sanayi.common.exceptions.InvalidRequestException
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*

data class ProviderSearchRequest(
    @Schema(example = "Ankara")
    val city: String? = null,

    @Schema(example = "Yenimahalle")
    val district: String? = null,

    @Schema(example = "1")
    val categoryId: Int? = null,

    @Schema(example = "3")
    val brandId: Int? = null,

    @field:DecimalMin("-5.0") @field:DecimalMax("5.0")
    @Schema(example = "2.0", description = "Minimum ortalama puan (-5..5)")
    val minScore: Double? = null,

    @field:DecimalMin("-5.0") @field:DecimalMax("5.0")
    @Schema(example = "5.0", description = "Maksimum ortalama puan (-5..5)")
    val maxScore: Double? = null
) {
    fun ensureValid() {
        if (minScore != null && maxScore != null && minScore > maxScore) {
            throw InvalidRequestException("minScore, maxScore'dan büyük olamaz.")
        }
    }
}
