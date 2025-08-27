package com.furkan.sanayi.controller

import com.furkan.sanayi.dto.ProviderCreateRequest
import com.furkan.sanayi.dto.ProviderCreateResponse
import com.furkan.sanayi.dto.ProviderDetailDto
import com.furkan.sanayi.dto.ProviderListItem
import com.furkan.sanayi.service.ProviderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
        @Parameter(description = "Şehir (boşsa varsayılan Ankara)") @RequestParam(required = false) city: String?,
        @Parameter(description = "İlçe") @RequestParam(required = false) district: String?,
        @Parameter(description = "Kategori ID") @RequestParam(required = false) categoryId: Int?,
        @Parameter(description = "Marka ID") @RequestParam(required = false) brandId: Int?,
        @ParameterObject pageable: Pageable
    ): Page<ProviderListItem> = providerService.listProviders(categoryId, city, district, brandId, pageable)

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Int): ProviderDetailDto = providerService.getProviderDetail(id)

    @Operation(summary = "Yeni servis sağlayıcı ekle")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody req: ProviderCreateRequest): ProviderCreateResponse = providerService.create(req)


}