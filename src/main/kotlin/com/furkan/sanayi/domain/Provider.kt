package com.furkan.sanayi.domain

import com.furkan.sanayi.dto.ProviderMiniDto
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.locationtech.jts.geom.Point
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "providers")
class Provider(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false, length = 150)
    var name: String = "",

    @Column(columnDefinition = "text")
    var address: String? = null,

    @Column(length = 100)
    var city: String? = null,

    @Column(length = 100)
    var district: String? = null,

    @Column(length = 50)
    var phone: String? = null,

    @Column(columnDefinition = "geometry(Point,4326)")
    var location: Point? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null,

    @ManyToMany
    @JoinTable(
        name = "provider_brands",
        joinColumns = [JoinColumn(name = "provider_id")],
        inverseJoinColumns = [JoinColumn(name = "brand_id")]
    )
    var brands: MutableSet<Brand> = linkedSetOf(),

    @ManyToMany
    @JoinTable(
        name = "provider_categories",
        joinColumns = [JoinColumn(name = "provider_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    var categories: MutableSet<Category> = linkedSetOf(),

    @OneToMany(mappedBy = "provider", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ratings: MutableList<Rating> = mutableListOf(),

    @Column(name = "rating_count", nullable = false)
    var ratingCount: Int = 0,

    @Column(name = "rating_sum", nullable = false)
    var ratingSum: Int = 0,

    @Column(name = "avg_score", insertable = false, updatable = false)
    var avgScore: BigDecimal? = null
) {
    fun toMiniDto(): ProviderMiniDto =
        ProviderMiniDto(
            id = this.id!!,
            name = this.name,
            address = this.address,
            city = this.city,
            district = this.district,
            phone = this.phone,
            lat = this.location?.y, // JTS: y=lat
            lng = this.location?.x  // JTS: x=lng
        )

    fun lat() = this.location?.x

    fun lng() = this.location?.y
}