package com.furkan.sanayi.service

import com.furkan.sanayi.common.enums.NearSort
import com.furkan.sanayi.common.enums.SortMode
import com.furkan.sanayi.common.exceptions.InvalidRequestException
import com.furkan.sanayi.domain.Provider
import com.furkan.sanayi.dto.*
import com.furkan.sanayi.repository.BrandRepository
import com.furkan.sanayi.repository.CategoryRepository
import com.furkan.sanayi.repository.ProviderRepository
import com.furkan.sanayi.repository.RatingRepository
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProviderService(
    private val providerRepo: ProviderRepository,
    private val categoryRepo: CategoryRepository,
    private val brandRepo: BrandRepository,
    private val geometryFactory: GeometryFactory,
    private val ratingRepo: RatingRepository
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

        val effectiveMinRatings = req.minRatings ?: if (req.sort != null) 1 else null

        val effectivePageable = when (req.sort) {
            SortMode.TOP -> {
                val s = Sort.by(
                    Sort.Order.desc("avgScore"),
                    Sort.Order.desc("ratingCount"),
                    Sort.Order.desc("id")
                )
                PageRequest.of(page.pageNumber, page.pageSize, s)
            }

            SortMode.WORST -> {
                val s = Sort.by(
                    Sort.Order.asc("avgScore"),
                    Sort.Order.desc("ratingCount"),
                    Sort.Order.desc("id")
                )
                PageRequest.of(page.pageNumber, page.pageSize, s)
            }

            null -> page
        }

        return providerRepo.search(
            categoryId = req.categoryId,
            city = city,
            district = district,
            brandId = req.brandId,
            minScore = req.minScore,
            maxScore = req.maxScore,
            q = q,
            minRatings = effectiveMinRatings,
            pageable = effectivePageable
        )
    }

    @Cacheable(cacheNames = ["providerDetail"], key = "#id")
    @Transactional(readOnly = true)
    fun getProviderDetail(id: Int): ProviderDetailDto {
        val p = providerRepo.findWithGraphById(id) ?: throw NoSuchElementException("Usta bulunamadi: $id")

        //Son 3 yorum
        val recent = ratingRepo.findRecentByProviderId(id, PageRequest.of(0, 3))

        //Histgoram (eksik bucket'lari 0 ile doldur)
        val raw = ratingRepo.scoreHistogram(id)
        val map = mutableMapOf<Int, Long>()
        for (i in -5..5) map[i] = 0
        raw.forEach { row ->
            val score = (row[0] as Number).toInt()
            val cnt = (row[1] as Number).toLong()
            map[score] = cnt
        }

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
            categories = p.categories.map { IdNameDto(it.id!!, it.name) }.sortedBy { it.name },
            recentRatings = recent,
            scoreHistogram = map.toSortedMap()
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
    fun nearby(
        lat: Double,
        lon: Double,
        radiusKm: Double,
        mode: NearSort?,
        minRatings: Int?,
        maxDistanceKm: Double?,
        limit: Int?,
        pageable: Pageable
    ): Page<ProviderNearItem> {
        if (radiusKm <= 0.0 || radiusKm > 100.0) {
            throw InvalidRequestException("radiusKm 0-100 arasında olmalıdır.")
        }

        val effectiveMaxKm = maxDistanceKm?.let {
            if(it <= 0 || it > 100) {
                throw InvalidRequestException("maxDistanceKm 0-100 arasında olmalıdır.")
            }
            kotlin.math.min(it, radiusKm)
        }

        val effectiveSort = mode ?: NearSort.DISTANCE
        val effectiveMinRatings = minRatings ?: if (effectiveSort != NearSort.DISTANCE) 1 else null

        // Pageable: her ihtimale karşı unsorted, ayrıca limit geldiyse sayfa boyutunu override et
        val pageSize = limit?.coerceIn(1, 500) ?: pageable.pageSize
        // !! pageable’daki sort’u sıfırla ki Spring ikinci ORDER BY eklemesin
        val pageNoSort = PageRequest.of(pageable.pageNumber, pageSize)

        val page = providerRepo.findNearby(lat, lon, radiusKm, effectiveSort.name, effectiveMinRatings, effectiveMaxKm, pageNoSort)
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