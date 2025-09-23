package com.furkan.sanayi.service

import com.furkan.sanayi.common.exceptions.InvalidRequestException
import com.furkan.sanayi.repository.RatingRepository
import org.springframework.cache.annotation.CacheEvict
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
}