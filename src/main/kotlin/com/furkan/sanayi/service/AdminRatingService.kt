package com.furkan.sanayi.service

import com.furkan.sanayi.common.enums.AdminRatingStatus
import com.furkan.sanayi.common.exceptions.InvalidRequestException
import com.furkan.sanayi.dto.AdminRatingItem
import com.furkan.sanayi.dto.AdminRatingSearchRequest
import com.furkan.sanayi.repository.RatingRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminRatingService(
    private val ratingRepository: RatingRepository
) {

    /**
     *
     * roviderId’yi aldık ama şimdilik yalnızca doğrulama için kullandık. Cache key’lerini sade tuttuğun için hedefli evict zor. “Çalışsın, sonra inceltiriz” taktiği.
     * */

    @Transactional
    @CacheEvict(cacheNames = ["providerDetail", "providerRatings"], allEntries = true)
    fun softDelete(ratingId: Int) {
        val providerId = ratingRepository.findProviderIdByRatingId(ratingId)
            ?: throw NoSuchElementException("Rating bulunamadi: $ratingId")

        val updated = ratingRepository.softDeleteById(ratingId)
        if (updated == 0) {
            throw InvalidRequestException("Zaten silinmis.")
        }
    }

    @Transactional
    @CacheEvict(cacheNames = ["providerDetail", "providerRatings"], allEntries = true)
    fun restore(ratingId: Int) {
        val providerId = ratingRepository.findProviderIdByRatingId(ratingId)
            ?: throw NoSuchElementException("Rating bulunamadi: $ratingId")

        val updated = ratingRepository.restoreById(ratingId)
        if (updated == 0) {
            throw InvalidRequestException("Zaten aktif.")
        }
    }

    @Transactional(readOnly = true)
    fun search(req: AdminRatingSearchRequest, pageable: Pageable): Page<AdminRatingItem> {
        val q = req.q?.trim()?.lowercase() ?: ""
        val status = (req.status ?: AdminRatingStatus.ACTIVE).name
        val effective = if (pageable.sort.isUnsorted)
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Order.desc("createdAt")))
        else pageable
        return ratingRepository.searchAdmin(
            providerId = req.providerId,
            userId = req.userId,
            anonymousId = req.anonymousId,
            minScore = req.minScore,
            maxScore = req.maxScore,
            q = q,
            status = status,
            from = req.from,
            to = req.to,
            pageable = effective
        )
    }
}