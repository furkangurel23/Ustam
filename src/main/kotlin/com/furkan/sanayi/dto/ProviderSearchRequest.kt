package com.furkan.sanayi.dto

import com.furkan.sanayi.common.enums.SortMode
import com.furkan.sanayi.common.exceptions.InvalidRequestException
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin

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
    val maxScore: Double? = null,

    @Schema(example = "honda", description = "İsim/adres prefix araması, en az 2 karakter")
    val q: String? = null,

    @Schema(allowableValues = ["TOP", "WORST"], example = "TOP", description = "Sıralama modu")
    val sort: SortMode? = null,

    @Schema(example = "1", description = "Minimum oy sayısı (rating_count). sort kullanıldığında default 1)")
    val minRatings: Int? = null
) {
    fun ensureValid() {
        if (minScore != null && maxScore != null && minScore > maxScore) {
            throw InvalidRequestException("minScore, maxScore'dan büyük olamaz.")
        }

        if (q != null && q.trim().length < 2) {
            throw InvalidRequestException("q en az 2 karakter olmalıdır.")
        }
        if (minRatings != null && minRatings < 0) {
            throw InvalidRequestException("minRatings negatif olamaz.")
        }
    }
}
