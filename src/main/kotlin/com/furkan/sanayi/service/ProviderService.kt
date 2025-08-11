package com.furkan.sanayi.service

import com.furkan.sanayi.domain.Rating
import com.furkan.sanayi.domain.User
import com.furkan.sanayi.dto.ProviderListItem
import com.furkan.sanayi.dto.RatingRequest
import com.furkan.sanayi.repository.ProviderRepository
import com.furkan.sanayi.repository.RatingRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ProviderService(
    private val providerRepo: ProviderRepository,
    private val ratingRepo: RatingRepository
) {
    fun list(
        categoryId: Int?,
        city: String?,
        district: String?,
        brandId: Int?,
        page: Pageable
    ): Page<ProviderListItem> {
        return providerRepo.search(categoryId, city, district, brandId, page)
    }

    @Transactional
    fun rate(dto: RatingRequest): Int {
        dto.ensureIdentityValid()

        //tekil oy kontrolu (DB index'leri zaten garanti veriyor; burada kullaniciya iyi mesaj verelim
        if (dto.userId != null && ratingRepo.existsByProviderIdAndUserId(dto.providerId, dto.userId)) {
            error("Bu kullanıcı bu ustayı zaten oylamış.")
        }
        if (dto.anonymousId != null && ratingRepo.existsByProviderIdAndAnonymousId(dto.providerId, dto.anonymousId)) {
            error("Bu anonim kimlik bu ustayı zaten oylamış.")
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
        return saved.id!!
    }
}