package com.furkan.sanayi.controller

import com.furkan.sanayi.dto.ProviderCreateRequest
import com.furkan.sanayi.dto.ProviderCreateResponse
import com.furkan.sanayi.service.ProviderService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/providers")
class ProviderAdminController(
    private val providerService: ProviderService
) {
    @Operation(summary = "Yeni servis sağlayıcı ekle")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody req: ProviderCreateRequest): ProviderCreateResponse = providerService.create(req)
}
