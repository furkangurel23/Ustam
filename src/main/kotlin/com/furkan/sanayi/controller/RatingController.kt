package com.furkan.sanayi.controller

import com.furkan.sanayi.dto.IdResponse
import com.furkan.sanayi.dto.RatingDto
import com.furkan.sanayi.dto.RatingRequest
import com.furkan.sanayi.service.RatingService
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rating")
class RatingController(
    private val ratingService: RatingService
) {

    @GetMapping("/provider/{id}")
    fun ratings(@PathVariable id: Int, @ParameterObject pageable: Pageable): Page<RatingDto> =
        ratingService.listRatings(id, pageable)

    @PostMapping("/rate")
    fun rate(@RequestBody @Valid body: RatingRequest): IdResponse = ratingService.addRating(body)
}