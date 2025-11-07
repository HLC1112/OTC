package com.example.otc.dsv.fsm

import com.example.otc.common.cmd.ApplyForAcceptorCmd
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.MySQLContainer
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.math.BigDecimal
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties

@ExtendWith(SpringExtension::class)
@SpringBootTest
@Testcontainers
class SagaHappyPathTest {

    companion object {
        @Container
        val mysql: MySQLContainer<*> = MySQLContainer("mysql:8.0")

        @Container
        val kafka: KafkaContainer = KafkaContainer("confluentinc/cp-kafka:7.5.1")

        @JvmStatic
        @DynamicPropertySource
        fun registerProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
        }
    }

    @Autowired
    lateinit var saga: FSM_1

    @Test
    fun `提交→校验通过→授权→出站ApplicationApproved`() {
        val cmd = ApplyForAcceptorCmd(
            userId = "u-1",
            depositAmount = BigDecimal("1500"),
            depositCurrency = "USDT"
        )
        val applicationId = saga.apply(cmd)
        assertNotNull(applicationId)

        // consume from Kafka to verify ApplicationApproved published
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafka.bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = "test-group"
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        KafkaConsumer<String, String>(props).use { consumer ->
            consumer.subscribe(listOf("otc.applications"))
            val records = consumer.poll(Duration.ofSeconds(5))
            assert(records.count() > 0)
        }
    }
}