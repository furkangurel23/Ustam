package com.furkan.sanayi.testsupport

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

object SharedPostgres {
    val container: PostgisContainer by lazy {
        PostgisContainer.instance().apply { start() }
    }
}

object TestDbProps {
    @JvmStatic
    @DynamicPropertySource
    fun register(reg: DynamicPropertyRegistry) {
        val c = SharedPostgres.container
        reg.add("spring.datasource.url") { c.jdbcUrl }
        reg.add("spring.datasource.username") { c.username }
        reg.add("spring.datasource.password") { c.password }
    }
}
