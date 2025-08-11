package com.furkan.sanayi.repository

import com.furkan.sanayi.domain.Brand
import org.springframework.data.jpa.repository.JpaRepository

interface BrandRepository : JpaRepository<Brand, Int> {
}