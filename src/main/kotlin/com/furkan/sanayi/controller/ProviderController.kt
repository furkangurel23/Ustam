package com.furkan.sanayi.controller

import com.furkan.sanayi.dto.ProviderDetailDto
import com.furkan.sanayi.dto.ProviderListItem
import com.furkan.sanayi.dto.ProviderNearItem
import com.furkan.sanayi.dto.ProviderSearchRequest
import com.furkan.sanayi.service.ProviderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/providers")
class ProviderController(
    private val providerService: ProviderService
) {
    @GetMapping
    fun list(
        @ParameterObject @Valid req: ProviderSearchRequest,
        @ParameterObject @PageableDefault(
            sort = ["avgScore", "ratingCount", "id"]
        ) pageable: Pageable
    ): Page<ProviderListItem> = providerService.listProviders(req, pageable)

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Int): ProviderDetailDto = providerService.getProviderDetail(id)

    @Operation(summary = "onuma göre yakın servis sağlayıcıları listele")
    @GetMapping("/near")
    fun near(
        @Parameter(description = "Latitude")
        @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") lat: Double,
        @Parameter(description = "Longitude")
        @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") lon: Double,
        @Parameter(description = "Yarıçap (km), 0-100")
        @RequestParam(defaultValue = "10.0") radiusKm: Double,
        @ParameterObject pageable: Pageable
    ): Page<ProviderNearItem> =
        providerService.nearby(lat = lat, lon = lon, radiusKm = radiusKm, pageable = pageable)

}