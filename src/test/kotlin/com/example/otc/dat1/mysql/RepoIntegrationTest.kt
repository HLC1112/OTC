package com.example.otc.dat1.mysql

import com.example.otc.dsv.dbs.repository.ApplicationsRepository
import com.example.otc.dsv.dbs.repository.UserPermissionsRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(SpringExtension::class)
@SpringBootTest
@org.testcontainers.junit.jupiter.Testcontainers
class RepoIntegrationTest {

    @Autowired
    lateinit var applicationsRepository: ApplicationsRepository

    @Autowired
    lateinit var userPermissionsRepository: UserPermissionsRepository

    companion object {
        @org.testcontainers.junit.jupiter.Container
        val mysql: org.testcontainers.containers.MySQLContainer<*> = org.testcontainers.containers.MySQLContainer("mysql:8.0")

        @JvmStatic
        @org.springframework.test.context.DynamicPropertySource
        fun registerProps(registry: org.springframework.test.context.DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }
        }
    }

    @Test
    fun `ApplicationsRepositoryImpl and UserPermissionsRepositoryImpl`() {
        val appId = UUID.randomUUID().toString()
        val created = applicationsRepository.createApplication(appId, "u-3", BigDecimal("1500"), "USDT", "Requested")
        assertTrue(created)
        val upd = applicationsRepository.updateStatus(appId, "Approved")
        assertTrue(upd)
        val granted = userPermissionsRepository.grant("u-3", "OTC_ACCEPTOR")
        assertTrue(granted)
    }
}