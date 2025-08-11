package com.furkan.sanayi.domain

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
)