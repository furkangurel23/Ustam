package com.furkan.sanayi.dto

import com.furkan.sanayi.common.enums.AdminRatingStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class AdminRatingSearchRequest(
    @Schema(example = "1")
    val providerId: Int? = null,
    @Schema(example = "42")
    val userId: Long? = null,
    @Schema(example = "anon-123")
    val anonymousId: String? = null,
    @Schema(example = "-2")
    val minScore: Short? = null,
    @Schema(example = "5")
    val maxScore: Short? = null,
    @Schema(example = "iyi", description = "Yorum prefix, lower-case")
    val q: String? = null,
    @Schema(allowableValues = ["ACTIVE", "DELETED", "ALL"], example = "ACTIVE")
    val status: AdminRatingStatus? = null,
    @Schema(example = "2025-01-01T00:00:00Z")
    val from: Instant? = null,
    @Schema(example = "2025-12-31T23:59:59Z")
    val to: Instant? = null
)
