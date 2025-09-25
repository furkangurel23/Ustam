package com.furkan.sanayi.domain

import jakarta.persistence.*
import org.hibernate.annotations.ColumnTransformer
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "moderation_logs")
class ModerationLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 64)
    var action: String = "",

    @Column(name = "entity_type", nullable = false, length = 64)
    var entityType: String = "RATING",

    @Column(name = "entity_id", nullable = false)
    var entityId: Long = 0,

    @Column(name = "provider_id")
    var providerId: Int? = null,

    @Column(name = "actor_user_id")
    var actorUserId: Long? = null,

    @Column(name = "actor_email")
    var actorEmail: String? = null,

    @Column(length = 500)
    var reason: String? = null,

    @Column(name = "ip_address", columnDefinition = "inet")
    @ColumnTransformer(write = "?::inet")
    var ipAddress: String? = null,

    @Column(name = "details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    var details: Map<String, Any?>? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

)