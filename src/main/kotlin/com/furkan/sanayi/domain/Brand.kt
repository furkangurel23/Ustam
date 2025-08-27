package com.furkan.sanayi.domain

import com.furkan.sanayi.dto.BrandDto
import com.furkan.sanayi.dto.ProviderMiniDto
import jakarta.persistence.*

@Entity
@Table(name = "brands")
class Brand(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false, unique = true, length = 100)
    var name: String = "",

    @ManyToMany(mappedBy = "brands")
    var providers: MutableSet<Provider> = linkedSetOf()
) {

    fun toDto(providers: List<ProviderMiniDto>): BrandDto =
        BrandDto(
            id = this.id!!,
            name = this.name,
            providers = providers
        )
}