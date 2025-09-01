package com.furkan.sanayi

import com.furkan.sanayi.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class DataInitializerTest {

    @Autowired
    lateinit var userRepo: UserRepository
    @Autowired
    @Qualifier("seedAdmin")
    lateinit var seedAdmin: ApplicationRunner

    @Test
    fun `seedAdmin is idempotent`() {
        seedAdmin.run(null)
        val exists = userRepo.findByEmailIgnoreCase("admin@sanayi.local") != null
        assertTrue(exists)
    }
}
