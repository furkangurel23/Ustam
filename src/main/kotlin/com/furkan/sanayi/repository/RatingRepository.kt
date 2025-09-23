package com.furkan.sanayi.repository

import com.furkan.sanayi.domain.Rating
import com.furkan.sanayi.dto.RatingDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RatingRepository : JpaRepository<Rating, Int> {

    @Query(
        value = """
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
      and r.deletedAt is null    
    order by r.createdAt desc
""",
        countQuery = """
        select count(r) from Rating r
        where r.provider.id = :providerId
          and r.deletedAt is null          
    """
    )
    fun findAllByProviderId(@Param("providerId") providerId: Int, pageable: Pageable): Page<RatingDto>

    @Query("select r from Rating r where r.provider.id = :providerId and r.user.id = :userId and r.deletedAt is null")
    fun findActiveByProviderAndUser(
        @Param("providerId") providerId: Int,
        @Param("userId") userId: Long  // User.id türüne göre Int/Long
    ): Rating?

    @Query("select r from Rating r where r.provider.id = :providerId and r.anonymousId = :anon and r.deletedAt is null")
    fun findActiveByProviderAndAnon(
        @Param("providerId") providerId: Int,
        @Param("anon") anonymousId: String
    ): Rating?
}
