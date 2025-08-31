package com.furkan.sanayi.controller

import com.furkan.sanayi.dto.IdResponse
import com.furkan.sanayi.dto.RatingDto
import com.furkan.sanayi.dto.RatingRequest
import com.furkan.sanayi.service.RatingService
import com.furkan.sanayi.web.ClientIpResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class RatingController(
    private val ratingService: RatingService,
    private val ipResolver: ClientIpResolver
) {

    @GetMapping("/providers/{id}/ratings")
    fun ratings(@PathVariable id: Int, @ParameterObject pageable: Pageable): Page<RatingDto> =
        ratingService.listRatings(id, pageable)

    @PostMapping("/ratings/rate")
    fun rate(
        request: HttpServletRequest,
        @RequestBody @Valid body: RatingRequest
    ): IdResponse {
        val ip = ipResolver.from(request)
        return ratingService.addRating(body.copy(ip = ip))
    }
}