package com.furkan.sanayi.controller

import com.furkan.sanayi.dto.AdminRatingItem
import com.furkan.sanayi.dto.AdminRatingSearchRequest
import com.furkan.sanayi.service.AdminRatingService
import com.furkan.sanayi.web.ClientIpResolver
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/ratings")
@SecurityRequirement(name = "bearerAuth")
class AdminRatingController(
    private val adminRatingService: AdminRatingService,
    private val ipResolver: ClientIpResolver
) {
    @Operation(summary = "Admin: yorum/puan listesi (filtrelenebilir)")
    @GetMapping
    fun list(
        @ParameterObject req: AdminRatingSearchRequest,
        @ParameterObject pageable: Pageable
    ): Page<AdminRatingItem> = adminRatingService.search(req, pageable)


    @Operation(summary = "Yorumu soft-delete yap (admin)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun softDelete(
        request: HttpServletRequest,
        @PathVariable id: Int,
        @RequestParam(required = false) reason: String?
    ) {
        val ip = ipResolver.from(request)
        adminRatingService.softDelete(id, reason, ip)
    }

    @Operation(summary = "Soft-delete'i geri al (admin)")
    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun restore(
        request: HttpServletRequest,
        @PathVariable id: Int,
        @RequestParam reason: String?
    ) {
        val ip = ipResolver.from(request)
        adminRatingService.restore(id, reason, ip)
    }
}