package com.example.otc.dsv.fsm

import com.example.otc.common.cmd.ApplyForAcceptorCmd
import com.example.otc.common.error.ErrorCodes
import com.example.otc.common.error.OtcException
import com.example.otc.dsv.da_0.PermissionWriter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.boot.test.mock.mockito.MockBean
import org.mockito.Mockito
import java.math.BigDecimal

@ExtendWith(SpringExtension::class)
@SpringBootTest
@ContextConfiguration(initializers = [SagaTimeoutTest.PropertyInit::class])
@org.testcontainers.junit.jupiter.Testcontainers
class SagaTimeoutTest {

    class PropertyInit : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of("fsm.grant_permission.timeout_ms=50").applyTo(applicationContext)
        }
    }

    @Autowired
    lateinit var saga: FSM_1

    @MockBean
    lateinit var permissionWriter: PermissionWriter

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
    fun `授权超时 → 返回E_OTC_5002`() {
        Mockito.`when`(permissionWriter.grantPermission(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenAnswer {
                Thread.sleep(200)
                throw RuntimeException("should not reach")
            }

        val cmd = ApplyForAcceptorCmd(
            userId = "u-2",
            depositAmount = BigDecimal("1500"),
            depositCurrency = "USDT"
        )
        val ex = assertThrows(OtcException::class.java) {
            saga.apply(cmd)
        }
        assertEquals(ErrorCodes.PERMISSION_TIMEOUT.code, ex.error.code)
    }
}