package com.furkan.sanayi.repository

import com.furkan.sanayi.domain.Category
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Int> {
    @EntityGraph(attributePaths = ["providers"])
    override fun findAll(pageable: Pageable): Page<Category>
}