package com.furkan.sanayi.controller

import com.furkan.sanayi.dto.RatingRequest
import com.furkan.sanayi.service.ProviderService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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
        pageable: Pageable
    ) = providerService.list(categoryId, city, district, brandId, pageable)

    @PostMapping("/ratigs")
    fun rate(@RequestBody @Valid body: RatingRequest): ResponseEntity<Map<String, Any>> {
        val id = providerService.rate(body)
        return ResponseEntity.ok(mapOf("id" to id, "status" to "ok"))
    }
}