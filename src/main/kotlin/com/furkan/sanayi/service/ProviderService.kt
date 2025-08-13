package com.furkan.sanayi.service

import com.furkan.sanayi.domain.Rating
import com.furkan.sanayi.domain.User
import com.furkan.sanayi.dto.*
import com.furkan.sanayi.repository.ProviderRepository
import com.furkan.sanayi.repository.RatingRepository
import org.locationtech.jts.geom.Point
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProviderService(
    private val providerRepo: ProviderRepository,
    private val ratingRepo: RatingRepository
) {
    @Transactional(readOnly = true)
    fun listProviders(
        categoryId: Int?,
        city: String?,
        district: String?,
        brandId: Int?,
        page: Pageable
    ): Page<ProviderListItem> {
        return providerRepo.search(categoryId, city ?: "Ankara", district, brandId, page)
    }

    @Transactional(readOnly = true)
    fun getProviderDetail(id: Int): ProviderDetailDto {
        val p = providerRepo.findWithGraphById(id) ?: throw NoSuchElementException("Usta bulunamadi: $id")

        return ProviderDetailDto(
            id = p.id!!,
            name = p.name,
            address = p.address,
            city = p.city,
            district = p.district,
            phone = p.phone,
            location = p.location?.toDto(),
            avgScore = p.avgScore,
            ratingCount = p.ratingCount,
            brands = p.brands.map { IdNameDto(it.id!!, it.name) }.sortedBy { it.name },
            categories = p.categories.map { IdNameDto(it.id!!, it.name) }.sortedBy { it.name }
        )
    }

    @Transactional(readOnly = true)
    fun listRatings(providerId: Int, pageable: Pageable): Page<RatingDto> =
        ratingRepo.findAllByProviderId(providerId, pageable)

    @Transactional
    fun rate(dto: RatingRequest): IdResponse {
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
        return IdResponse(saved.id!!)
    }

    private fun Point.toDto(): LocationDto = LocationDto(lon = this.x, lat = this.y)
}