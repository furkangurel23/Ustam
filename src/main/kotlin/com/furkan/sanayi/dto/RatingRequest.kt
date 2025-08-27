package com.furkan.sanayi.dto

import com.furkan.sanayi.common.exceptions.InvalidRequestException
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(description = "Bir sağlayıcıya yeni puan/yorum ekleme isteği. userId veya anonymousId alanlarından biri DOLDURULMALI, ikisi aynı anda olamaz.")
data class RatingRequest(

    @field:NotNull
    @Schema(example = "1", description = "Puan verilen sağlayıcının ID’si")
    val providerId: Int,

    @field:Min(-5) @field:Max(5)
    @Schema(example = "4", description = "Puan aralığı: -5 .. 5")
    val score: Short,

    @field:Size(max = 500)
    @Schema(example = "Hızlı ve ilgili, tavsiye ederim.", description = "İsteğe bağlı yorum (en fazla 500 karakter)")
    val comment: String? = null,

    @Schema(example = "42", description = "Kayıtlı kullanıcı ID (anonymousId boş olmalı)")
    val userId: Long? = null,

    @Schema(example = "anon-123", description = "Anonim kullanıcı kimliği (userId boş olmalı)")
    val anonymousId: String? = null,

    @Schema(example = "1.2.3.4", description = "İstemcinin IP adresi (IPv4 veya IPv6)")
    val ip: String? = null
) {
    fun ensureIdentityValid() {
        if (!((userId != null) xor (anonymousId != null))) {
            throw InvalidRequestException("Either userId or anonymousId must be provided (exclusively).")
        }
    }
}
