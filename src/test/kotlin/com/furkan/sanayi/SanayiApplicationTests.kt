package com.furkan.sanayi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(properties = ["spring.flyway.enabled=false"])
@Testcontainers
class SanayiApplicationTests {

    /*companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgis/postgis:17-3.5")
                .apply {
                    withDatabaseName("sanayi")
                    withUsername("sanayi_user")
                    withPassword("furkan")
                }

        @JvmStatic
        @DynamicPropertySource
        fun overrideProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }*/

    @Test
    fun contextLoads() {
        // …
    }
}