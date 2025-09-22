package com.furkan.sanayi.controller

import com.furkan.sanayi.dto.IdResponse
import com.furkan.sanayi.dto.RatingDto
import com.furkan.sanayi.dto.RatingRequest
import com.furkan.sanayi.security.SecurityFacade
import com.furkan.sanayi.security.recaptcha.RecaptchaVerifier
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
    private val ipResolver: ClientIpResolver,
    private val securityFacade: SecurityFacade,
    private val recaptcha: RecaptchaVerifier
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
        val fixed = securityFacade.idOrNull()?.let { id ->
            body.copy(userId = id, anonymousId = null, ip = ip)
        } ?: body.copy(ip = ip)

        //Anonim ise ve prod'da reCAPTCHA aciksa dogrula
        if (fixed.userId == null) {
            recaptcha.verifyOrThrow(fixed.recaptchaToken, ip)
        }

        return ratingService.addRating(fixed)
    }
}