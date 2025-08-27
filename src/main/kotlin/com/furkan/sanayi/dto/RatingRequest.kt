package com.furkan.sanayi.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull

data class RatingRequest(
    @field:NotNull val providerId: Int,
    @field:Min(-5) @field:Max(5) val score: Short,
    @field:Size(max = 500) val comment: String? = null,
    val userId: Long? = null,
    val anonymousId: String? = null,
    val ip: String? = null
) {
    fun ensureIdentityValid() {
        require((userId != null) xor (anonymousId != null)) {
            "Either userId or anonymousId must be provided (exclusively)."
        }
    }
}