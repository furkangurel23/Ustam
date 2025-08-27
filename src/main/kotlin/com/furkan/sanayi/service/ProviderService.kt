package com.furkan.sanayi.service

import com.furkan.sanayi.domain.Provider
import com.furkan.sanayi.dto.*
import com.furkan.sanayi.repository.BrandRepository
import com.furkan.sanayi.repository.CategoryRepository
import com.furkan.sanayi.repository.ProviderRepository
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProviderService(
    private val providerRepo: ProviderRepository,
    private val categoryRepo: CategoryRepository,
    private val brandRepo: BrandRepository,
    private val geometryFactory: GeometryFactory
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

    @Transactional
    fun create(req: ProviderCreateRequest): ProviderCreateResponse {
        val categories = if (req.categoryIds.isNotEmpty())
            categoryRepo.findAllById(req.categoryIds).toSet() else emptySet()
        if (categories.size != req.categoryIds.size)
            error("Geçersiz caegoryIds tespit edildi.")
        val brands = if (req.brandIds.isNotEmpty())
            brandRepo.findAllById(req.brandIds).toSet() else emptySet()
        if (brands.size != req.brandIds.size)
            error("Geçersiz brandIds tespit edildi.")

        //Location (lon, lat) -> JTS Point(SRID 4326)

        val point = geometryFactory.createPoint(Coordinate(req.lon, req.lat))
        point.srid = 4326

        val p = Provider().apply {
            name = req.name
            city = req.city
            district = req.district
            address = req.address
            phone = req.phone
            location = point
            categories.forEach { this.categories.add(it) }
            brands.forEach { this.brands.add(it) }
        }

        val saved = providerRepo.save(p)
        return ProviderCreateResponse(id = saved.id!!, name = saved.name)

    }

    private fun Point.toDto(): LocationDto = LocationDto(lon = this.x, lat = this.y)
}