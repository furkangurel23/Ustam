package com.furkan.sanayi.repository

import com.furkan.sanayi.domain.Provider
import com.furkan.sanayi.dto.ProviderListItem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProviderRepository : JpaRepository<Provider, Int> {

    fun findAllByCityIgnoreCaseAndDistrictIgnoreCase(city: String, district: String): List<Provider>

    @Query(
        value = """
        select new com.furkan.sanayi.dto.ProviderListItem(
            p.id, p.name, p.city, p.district, p.phone,
            CAST(p.avgScore as double), CAST(p.ratingCount as long)
        )
        from Provider p
        left join p.categories c
        left join p.brands b
        where lower(p.city) = lower(coalesce(:city, 'Ankara'))
            and (:district is null or lower(p.district) = lower(:district))
            and (:categoryId is null or c.id = :categoryId)
            and (:brandId is null or b.id = :brandId)
        group by p.id, p.name, p.city, p.district, p.phone, p.avgScore, p.ratingCount
    """
    )
    fun search(
        categoryId: Int?,
        city: String?,
        district: String?,
        brandId: Int?,
        pageable: Pageable
    ): Page<ProviderListItem>

}