package com.furkan.sanayi.repository

import com.furkan.sanayi.domain.Rating
import com.furkan.sanayi.dto.AdminRatingItem
import com.furkan.sanayi.dto.RatingDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

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

    /*
    * ilgili provider bulunup cacheli hedefi temizleyeegiz.
    * */
    @Query("select r.provider.id from Rating r where r.id = :id")
    fun findProviderIdByRatingId(@Param("id") id: Int): Int?

    @Modifying
    @Query("update Rating r set r.deletedAt = CURRENT_TIMESTAMP where r.id = :id and r.deletedAt is null")
    fun softDeleteById(@Param("id") id: Int): Int

    @Modifying
    @Query("update Rating r set r.deletedAt = null where r.id = :id and r.deletedAt is not null")
    fun restoreById(@Param("id") id: Int): Int

    @Query(
        value = """
        select new com.furkan.sanayi.dto.RatingDto(
            r.id, r.score, r.commentText, r.createdAt,
            u.displayName,
            (case when r.anonymousId is not null then true else false end)
        )
        from Rating r 
        left join r.user u
        where r.provider.id = :providerId 
            and r.deletedAt is null
        order by r.createdAt desc
    """
    )
    fun findRecentByProviderId(
        @Param("providerId") providerId: Int,
        pageable: Pageable
    ): List<RatingDto>

    @Query(
        value = """
        select
            r.score as score, count(*) as cnt
        from ratings r where r.provider_id = :providerId 
            and r.deleted_at is null 
        group by r.score
    """, nativeQuery = true
    )
    fun scoreHistogram(@Param("providerId") providerId: Int): List<Array<Any>>


    @Query(value = """
        select 
            new com.furkan.sanayi.dto.AdminRatingItem(
                r.id,
                p.id,
                p.name,
                r.score,
                r.commentText,
                r.createdAt,
                u.displayName,
                r.anonymousId,
                r.ipAddress,
                r.deletedAt
            )
        from Rating r
        join r.provider p
        left join r.user u
        where (:providerId is null or p.id = :providerId)
            and (:userId is null or u.id = :userId)
            and (:anonymousId is null or r.anonymousId = :anonymousId)
            and (:minScore is null or r.score >= :minScore)
            and (:maxScore is null or r.score <= :maxScore)
            and (:q = '' or lower(r.commentText) like concat(:q, '%'))
            and (r.createdAt >= coalesce(:from, r.createdAt))
            and (r.createdAt <= coalesce(:to, r.createdAt))
            and (
                :status = 'ALL'
                or (:status = 'ACTIVE' and r.deletedAt is null)
                or (:status = 'DELETED' and r.deletedAt is not null)
            )
    """)
    fun searchAdmin(
        @Param("providerId") providerId: Int?,
        @Param("userId") userId: Long?,
        @Param("anonymousId") anonymousId: String?,
        @Param("minScore") minScore: Short?,
        @Param("maxScore") maxScore: Short?,
        @Param("q") q: String?,
        @Param("status") status: String,
        @Param("from") from: Instant?,
        @Param("to") to: Instant?,
        pageable: Pageable
    ): Page<AdminRatingItem>
}
