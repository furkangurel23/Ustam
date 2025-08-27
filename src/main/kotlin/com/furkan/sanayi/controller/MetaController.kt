package com.furkan.sanayi.controller

import com.furkan.sanayi.dto.BrandDto
import com.furkan.sanayi.dto.CategoryDto
import com.furkan.sanayi.service.MetaService
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MetaController(
    private val metaService: MetaService
) {
    @GetMapping("/categories")
    fun listCategories(@ParameterObject pageable: Pageable): Page<CategoryDto> =
        metaService.listCategories(pageable)

    @GetMapping("/brands")
    fun listBrands(@ParameterObject pageable: Pageable): Page<BrandDto> = metaService.listBrands(pageable)
}