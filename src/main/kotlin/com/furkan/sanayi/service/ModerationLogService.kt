package com.furkan.sanayi.service

import com.furkan.sanayi.domain.ModerationLog
import com.furkan.sanayi.repository.ModerationLogRepository
import com.furkan.sanayi.security.SecurityFacade
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ModerationLogService(
    private val moderationLogRepository: ModerationLogRepository,
    private val securityFacade: SecurityFacade
) {
    fun logModeration(
        action: String,
        ratingId: Int,
        providerId: Int?,
        reason: String?,
        ip: String?,
        entityType: String,
        details: Map<String, Any?>
    ) {
        val log = ModerationLog(
            action = action,
            entityType = entityType,
            entityId = ratingId.toLong(),
            providerId = providerId,
            actorUserId = securityFacade.idOrNull(),
            actorEmail = securityFacade.emailOrNull(),
            reason = reason?.take(500),
            ipAddress = ip,
            details = details
        )
        moderationLogRepository.save(log)
    }

    @Transactional(readOnly = true)
    fun logsForEntity(entityId: Int, entityType: String, pageable: Pageable): Page<ModerationLog> {
        return moderationLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            entityType,
            entityId.toLong(),
            pageable
        )
    }
}