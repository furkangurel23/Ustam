package com.furkan.sanayi.repository

import com.furkan.sanayi.domain.Provider
import com.furkan.sanayi.dto.ProviderListItem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProviderRepository : JpaRepository<Provider, Int> {

    @EntityGraph(attributePaths = ["brands", "categories"])
    fun findWithGraphById(id: Int): Provider?

    @Query(
        """
        select new com.furkan.sanayi.dto.ProviderListItem(
            p.id, p.name, p.city, p.district, p.phone,
            p.avgScore, p.ratingCount, 
            cast(function('ST_Y', p.location) as double),  
            cast(function('ST_X', p.location) as double) 
        )
        from Provider p
        where (:city is null or p.city = :city)
          and (:district is null or p.district = :district)
          and (:categoryId is null or exists (
                select 1 from p.categories c where c.id = :categoryId
          ))
          and (:brandId is null or exists (
                select 1 from p.brands b where b.id = :brandId
          ))
          and (:minScore is null or p.avgScore >= :minScore)
          and (:maxScore is null or p.avgScore <= :maxScore)
    """
    )
    fun search(
        @Param("categoryId") categoryId: Int?,
        @Param("city") city: String? = "Ankara",
        @Param("district") district: String?,
        @Param("brandId") brandId: Int?,
        @Param("minScore") minScore: Double?,
        @Param("maxScore") maxScore: Double?,
        pageable: Pageable
    ): Page<ProviderListItem>


    @Query(
        """
        select p from Provider p 
        join p.categories c
        where c.id in :categoryIds
    """
    )
    fun findByCategoryIds(@Param("categoryIds") categoryIds: Collection<Int>): List<Provider>

    @Query(
        """
        select p from Provider p 
        join p.brands b
        where b.id in :brandIds
    """
    )
    fun findByBrandIds(@Param("brandIds") brandIds: Collection<Int>): List<Provider>

    @Query(
        value = """
        SELECT 
          p.id,
          p.name,
          p.city,
          p.district,
          p.avg_score AS avgScore,
          p.rating_count AS ratingCount,
          ST_Distance(
            p.location::geography,
            ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
          ) / 1000.0 AS distanceKm
        FROM providers p
        WHERE ST_DWithin(
          p.location::geography,
          ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
          :radiusKm * 1000.0
        )
        ORDER BY distanceKm
        """,
        countQuery = """
        SELECT COUNT(1)
        FROM providers p
        WHERE ST_DWithin(
          p.location::geography,
          ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
          :radiusKm * 1000.0
        )
        """,
        nativeQuery = true
    )
    fun findNearby(
        @Param("lat") lat: Double,
        @Param("lon") lon: Double,
        @Param("radiusKm") radiusKm: Double,
        pageable: Pageable
    ): Page<Array<Any>>
}