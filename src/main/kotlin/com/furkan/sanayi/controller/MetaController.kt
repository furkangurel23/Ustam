package com.furkan.sanayi.controller

import com.furkan.sanayi.repository.BrandRepository
import com.furkan.sanayi.repository.CategoryRepository
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MetaController(
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository
) {
    @GetMapping("/categories")
    fun listCategories(@ParameterObject pageable: Pageable) = categoryRepository.findAllByOrderByNameAsc(pageable)

    @GetMapping("/brands")
    fun listBrands(@ParameterObject pageable: Pageable) = brandRepository.findAllByOrderByNameAsc(pageable)
}