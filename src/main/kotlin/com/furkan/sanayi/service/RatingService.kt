package com.furkan.sanayi.service

import com.furkan.sanayi.common.exceptions.InvalidRequestException
import com.furkan.sanayi.domain.Rating
import com.furkan.sanayi.domain.User
import com.furkan.sanayi.dto.IdResponse
import com.furkan.sanayi.dto.RatingDto
import com.furkan.sanayi.dto.RatingRequest
import com.furkan.sanayi.repository.ProviderRepository
import com.furkan.sanayi.repository.RatingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RatingService(
    private val ratingRepo: RatingRepository,
    private val providerRepo: ProviderRepository
) {

    @Transactional(readOnly = true)
    fun listRatings(providerId: Int, pageable: Pageable): Page<RatingDto> =
        ratingRepo.findAllByProviderId(providerId, pageable)

    @Transactional(readOnly = false)
    fun addRating(dto: RatingRequest): IdResponse {
        dto.ensureIdentityValid()

        //tekil oy kontrolu (DB index'leri zaten garanti veriyor; burada kullaniciya iyi mesaj verelim
        if (dto.userId != null && ratingRepo.existsByProviderIdAndUserId(dto.providerId, dto.userId)) {
            throw InvalidRequestException("Bu sağlayıcıyı zaten oyladınız. (kullanıcı)")
        }
        if (dto.anonymousId != null && ratingRepo.existsByProviderIdAndAnonymousId(dto.providerId, dto.anonymousId)) {
            throw InvalidRequestException("Bu sağlayıcıyı zaten oyladınız. (anonim)")
        }


        val provider = providerRepo.findById(dto.providerId)
            .orElseThrow { IllegalStateException("Usta bulunamadı: ${dto.providerId}") }
        val saved = ratingRepo.save(
            Rating(
                provider = provider,
                score = dto.score,
                commentText = dto.comment,
                user = dto.userId?.let { User(it) },
                anonymousId = dto.anonymousId,
                ipAddress = dto.ip
            )
        )
        return IdResponse(saved.id!!)
    }
}