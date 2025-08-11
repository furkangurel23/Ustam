package com.furkan.sanayi.controller

import com.furkan.sanayi.dto.IdResponse
import com.furkan.sanayi.dto.RatingRequest
import com.furkan.sanayi.service.ProviderService
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ProviderController(
    private val providerService: ProviderService
) {
    @GetMapping("/providers")
    fun list(
        @RequestParam(required = false) categoryId: Int?,
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false) district: String?,
        @RequestParam(required = false) brandId: Int?,
        @ParameterObject pageable: Pageable
    ) = providerService.listProviders(categoryId, city, district, brandId, pageable)

    @GetMapping("/providers/{id}")
    fun detail(@PathVariable id: Int) = providerService.getProviderDetail(id)

    @GetMapping("/providers/{id}/ratings")
    fun ratings(@PathVariable id: Int, @ParameterObject pageable: Pageable) = providerService.listRatings(id, pageable)

    @PostMapping("/ratings")
    fun rate(@RequestBody @Valid body: RatingRequest): IdResponse = providerService.rate(body)

}