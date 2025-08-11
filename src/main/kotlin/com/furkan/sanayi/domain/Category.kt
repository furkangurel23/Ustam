package com.furkan.sanayi.domain

import jakarta.persistence.*

@Entity
@Table(name = "categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false, unique = true, length = 100)
    var name: String = "",

    @ManyToMany(mappedBy = "categories")
    var providers: MutableSet<Provider> = linkedSetOf()
)