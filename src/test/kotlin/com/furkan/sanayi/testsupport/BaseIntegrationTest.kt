package com.furkan.sanayi.testsupport

import com.furkan.sanayi.domain.Provider
import com.furkan.sanayi.repository.ProviderRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(scripts = ["/test-sql/reset.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
abstract class BaseIntegrationTest {
    companion object {
        @Container
        @JvmStatic
        @ServiceConnection
        val postgres = PostgisContainer.instance()
            .withDatabaseName("sanayi")
            .withUsername("sanayi_user")
            .withPassword("furkan")

    }

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var providerRepository: ProviderRepository

    @Autowired
    lateinit var geometryFactory: GeometryFactory

    @BeforeEach
    fun baseSetup() {
    }

    protected fun createProvider(name: String = "P-${System.nanoTime()}", city: String = "Ankara"): Provider {
        val pt = geometryFactory.createPoint(Coordinate(32.85, 39.93))
        pt.srid = 4326
        val p = Provider().apply {
            this.name = name; this.city = city; this.district = "Çankaya"; this.location = pt
        }
        return providerRepository.save(p)
    }
}