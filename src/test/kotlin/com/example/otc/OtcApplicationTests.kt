package com.example.otc

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
@org.testcontainers.junit.jupiter.Testcontainers
class OtcApplicationTests {

    companion object {
        @org.testcontainers.junit.jupiter.Container
        val mysql: org.testcontainers.containers.MySQLContainer<*> = org.testcontainers.containers.MySQLContainer("mysql:8.0")

        @JvmStatic
        @DynamicPropertySource
        fun registerProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }
        }
    }

	@Test
	fun contextLoads() {
	}

}
