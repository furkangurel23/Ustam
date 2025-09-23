package com.furkan.sanayi.controller

import com.furkan.sanayi.service.AdminRatingService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/ratings")
class AdminRatingController(
    private val adminRatingService: AdminRatingService
) {

    @Operation(summary = "Yorumu soft-delete yap (admin)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun softDelete(@PathVariable id: Int) {
        adminRatingService.softDelete(id)
    }

    @Operation(summary = "Soft-delete'i geri al (admin)")
    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun restore(@PathVariable id: Int) {
        adminRatingService.restore(id)
    }
}