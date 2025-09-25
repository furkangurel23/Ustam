package com.furkan.sanayi.repository

import com.furkan.sanayi.domain.ModerationLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ModerationLogRepository : JpaRepository<ModerationLog, Long> {

    fun findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
        entityType: String,
        entityId: Long,
        pageable: Pageable
    ): Page<ModerationLog>
}