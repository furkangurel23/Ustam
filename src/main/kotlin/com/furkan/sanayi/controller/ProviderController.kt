package com.furkan.sanayi.controller

import com.furkan.sanayi.dto.*
import com.furkan.sanayi.service.ProviderService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/providers")
class ProviderController(
    private val providerService: ProviderService
) {
    @GetMapping
    fun list(
        @ParameterObject @Valid req: ProviderSearchRequest,
        @ParameterObject pageable: Pageable
    ): Page<ProviderListItem> = providerService.listProviders(req, pageable)

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Int): ProviderDetailDto = providerService.getProviderDetail(id)

    @Operation(summary = "Yeni servis sağlayıcı ekle")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody req: ProviderCreateRequest): ProviderCreateResponse = providerService.create(req)


}