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
                p.avgScore, p.ratingCount
            )
            from Provider p
            where lower(p.city) = lower(coalesce(:city, 'Ankara'))
              and (:district is null or lower(p.district) = lower(:district))
              and (:categoryId is null or exists (
                    select 1 from p.categories c where c.id = :categoryId
              ))
              and (:brandId is null or exists (
                    select 1 from p.brands b where b.id = :brandId
              ))
        """
    )
    fun search(
        @Param("categoryId") categoryId: Int?,
        @Param("city") city: String?,
        @Param("district") district: String?,
        @Param("brandId") brandId: Int?,
        pageable: Pageable
    ): Page<ProviderListItem>


}