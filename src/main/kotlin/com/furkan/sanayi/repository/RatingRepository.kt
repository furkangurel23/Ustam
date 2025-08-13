package com.furkan.sanayi.repository

import com.furkan.sanayi.domain.Rating
import com.furkan.sanayi.dto.RatingDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RatingRepository : JpaRepository<Rating, Int> {

    @Query(value = """
        select new com.furkan.sanayi.dto.RatingDto(
            r.id,
            r.score,
            r.commentText,
            r.createdAt,
            u.displayName,
            (case when r.anonymousId is not null then true else false end)
        )
        from Rating r
        left join r.user u
        where r.provider.id = :providerId
        order by r.createdAt desc
    """,
        countQuery = """
            select count(r) from Rating r where r.provider.id = :providerId
        """)
    fun findAllByProviderId(@Param("providerId") providerId: Int, pageable: Pageable): Page<RatingDto>
    fun existsByProviderIdAndUserId(providerId: Int, userId: Long): Boolean
    fun existsByProviderIdAndAnonymousId(providerId: Int, anonymousId: String): Boolean

}