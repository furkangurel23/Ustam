package com.furkan.sanayi.service

import com.furkan.sanayi.dto.BrandDto
import com.furkan.sanayi.dto.CategoryDto
import com.furkan.sanayi.dto.ProviderMiniDto
import com.furkan.sanayi.repository.BrandRepository
import com.furkan.sanayi.repository.CategoryRepository
import com.furkan.sanayi.repository.ProviderRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MetaService(
    private val providerRepository: ProviderRepository,
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository
) {

    @Transactional(readOnly = true)
    fun listCategories(pageable: Pageable): Page<CategoryDto> {
        val page = categoryRepository.findAll(pageable)
        if (page.isEmpty) return PageImpl(emptyList(), pageable, 0)

        val categoryIds = page.content.mapNotNull { it.id }
        val providers = providerRepository.findByCategoryIds(categoryIds)

        val content = page.content.mapNotNull { cat ->
            val catProviders = providers.filter { p -> p.categories.any { it.id == cat.id } }
            val items = catProviders.map { it.toMiniDto() }
            cat.toDto(items)
        }
        return PageImpl(content, pageable, page.totalElements)
    }

    @Transactional(readOnly = true)
    fun listBrands(pageable: Pageable): Page<BrandDto> {
        val page = brandRepository.findAll(pageable)
        if (page.isEmpty) return PageImpl(emptyList(), pageable, 0)

        val brandIds = page.content.mapNotNull { it.id }
        val providers = providerRepository.findByBrandIds(brandIds)

        val byBrandId: Map<Int, List<ProviderMiniDto>> =
            providers.flatMap { p -> p.brands.map { it.id!! to p } }.groupBy({ it.first }, { it.second.toMiniDto() })

        val content = page.content.map { brand ->
            val providersForBrand = byBrandId[brand.id] ?: emptyList()
            brand.toDto(providersForBrand)
        }

        return PageImpl(content, pageable, page.totalElements)
    }
}