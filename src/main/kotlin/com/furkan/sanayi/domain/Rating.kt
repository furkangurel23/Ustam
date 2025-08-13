package com.furkan.sanayi.domain

import jakarta.persistence.*
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.hibernate.annotations.ColumnTransformer
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "ratings")
class Rating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    var provider: Provider,

    @field:Min(-5)
    @field:Max(5)
    @Column(nullable = false)
    var score: Short,

    @Column(name = "comment_text", length = 500)
    var commentText: String? = null,

    // user_id XOR anonymous_id -> DB CHECK ile garanti
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @Column(name = "anonymous_id", length = 100)
    var anonymousId: String? = null,

    @Column(name = "ip_address", columnDefinition = "inet")
    @ColumnTransformer(write = "?::inet")
    var ipAddress: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null
)