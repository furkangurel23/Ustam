package com.furkan.sanayi.service

import com.furkan.sanayi.common.exceptions.InvalidRequestException
import com.furkan.sanayi.domain.Provider
import com.furkan.sanayi.dto.*
import com.furkan.sanayi.repository.BrandRepository
import com.furkan.sanayi.repository.CategoryRepository
import com.furkan.sanayi.repository.ProviderRepository
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
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
        req: ProviderSearchRequest,
        page: Pageable
    ): Page<ProviderListItem> {
        req.ensureValid()

        val city = req.city?.trim()?.lowercase() ?: "ankara"
        val district = req.district?.trim()?.lowercase()
        val q = req.q?.trim()?.lowercase()

        return providerRepo.search(
            categoryId = req.categoryId,
            city = city,
            district = district,
            brandId = req.brandId,
            minScore = req.minScore,
            maxScore = req.maxScore,
            q = q,
            pageable = page
        )
    }

    @Cacheable(cacheNames = ["providerDetail"], key = "#id")
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

    @CacheEvict(cacheNames = ["providerDetail", "categories", "brands"], allEntries = true)
    @Transactional
    fun create(req: ProviderCreateRequest): ProviderCreateResponse {
        req.ensureValid()
        val categories = if (req.categoryIds.isNotEmpty())
            categoryRepo.findAllById(req.categoryIds).toSet() else emptySet()
        val missingCats = req.categoryIds - categories.map { it.id!! }.toSet()
        if (missingCats.isNotEmpty())
            throw InvalidRequestException("Geçersiz kategori ID(ler): ${missingCats.joinToString(",")}")

        val brands = if (req.brandIds.isNotEmpty())
            brandRepo.findAllById(req.brandIds).toSet() else emptySet()
        val missingBrands = req.brandIds - brands.map { it.id!! }.toSet()
        if (missingBrands.isNotEmpty())
            throw InvalidRequestException("Geçersiz marka ID(ler): ${missingBrands.joinToString(",")}")


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

    @Transactional(readOnly = true)
    fun nearby(lat: Double, lon: Double, radiusKm: Double, pageable: Pageable): Page<ProviderNearItem> {
        if (radiusKm <= 0.0 || radiusKm > 100.0) {
            throw InvalidRequestException("radiusKm 0-100 arasında olmalıdır.")
        }
        val page = providerRepo.findNearby(lat, lon, radiusKm, pageable)
        val items = page.content.map { row ->
            ProviderNearItem(
                id = (row[0] as Number).toInt(),
                name = row[1] as String,
                city = row[2] as String?,
                district = row[3] as String?,
                avgScore = (row[4] as? Number)?.toDouble(),
                ratingCount = (row[5] as? Number)?.toLong(),
                distanceKm = (row[6] as Number).toDouble()
            )
        }
        return PageImpl(items, pageable, page.totalElements)
    }

    private fun Point.toDto(): LocationDto = LocationDto(lon = this.x, lat = this.y)
}