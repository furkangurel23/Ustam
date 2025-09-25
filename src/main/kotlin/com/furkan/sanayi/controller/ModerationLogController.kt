package com.furkan.sanayi.controller

import com.furkan.sanayi.domain.ModerationLog
import com.furkan.sanayi.service.ModerationLogService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/logs")
@SecurityRequirement(name = "bearerAuth")
class ModerationLogController(
    private val moderationLogService: ModerationLogService
) {
    @Operation(summary = "Belirli bir rating için moderasyon logları")
    @GetMapping("/{id}/logs")
    fun logs(
        @PathVariable id: Int,
        @RequestParam entity: String,
        @ParameterObject pageable: Pageable
    ): Page<ModerationLog> = moderationLogService.logsForEntity(id, entity, pageable)
}