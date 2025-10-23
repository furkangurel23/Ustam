package com.furkan.sanayi.service

import com.furkan.sanayi.domain.Provider
import com.furkan.sanayi.domain.Rating
import com.furkan.sanayi.domain.User
import com.furkan.sanayi.dto.IdResponse
import com.furkan.sanayi.dto.RatingDto
import com.furkan.sanayi.dto.RatingRequest
import com.furkan.sanayi.repository.RatingRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RatingService(
    private val ratingRepo: RatingRepository,
) {

    @Cacheable(
        cacheNames = ["providerRatings"],
        key = "#providerId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort"
    )
    @Transactional(readOnly = true)
    fun listRatings(providerId: Int, pageable: Pageable): Page<RatingDto> =
        ratingRepo.findAllByProviderId(providerId, pageable)

    @Caching(
        evict = [
            CacheEvict(cacheNames = ["providerRatings"], allEntries = true),
            CacheEvict(cacheNames = ["providerDetail"], key = "#req.providerId")
        ]
    )
    @Transactional
    fun addRating(req: RatingRequest): IdResponse {
        req.ensureIdentityValid()

        val existing = when {
            req.userId != null ->
                ratingRepo.findActiveByProviderAndUser(req.providerId, req.userId)

            !req.anonymousId.isNullOrBlank() ->
                ratingRepo.findActiveByProviderAndAnon(req.providerId, req.anonymousId)

            else -> null
        }

        return if (existing == null) {
            val entity = Rating(
                provider = Provider(id = req.providerId),
                score = req.score,
                commentText = req.comment?.trim(),
                user = req.userId?.let { User(id = it) },  // User.id türünü projendeki ile eşleştir
                anonymousId = req.anonymousId,
                ipAddress = req.ip,
                deletedAt = null
            )
            IdResponse(ratingRepo.save(entity).id!!)
        } else {
            existing.score = req.score
            existing.commentText = req.comment?.trim()
            existing.ipAddress = req.ip

            // provider değiştirmiyoruz burada; gerekirse ayrı uç
            IdResponse(ratingRepo.save(existing).id!!)
        }
    }
}