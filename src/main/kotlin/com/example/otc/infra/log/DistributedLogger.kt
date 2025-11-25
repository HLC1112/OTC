package com.example.otc.infra.log

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class DistributedLogger(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${spring.application.name}") private val serviceName: String
) {
    fun info(message: String, traceId: String? = null) {
        val payload = mapOf(
            "type" to "sys:LOG",
            "payload" to mapOf(
                "sender" to serviceName,
                "level" to "INFO",
                "message" to message,
                "traceId" to traceId,
                "ts" to System.currentTimeMillis()
            )
        )
        val json = objectMapper.writeValueAsString(payload)
        kafkaTemplate.send("system-logs-topic", json)
        println(message)
    }

    fun info(className: String, message: String, traceId: String? = null) {
        val payload = mapOf(
            "type" to "sys:LOG",
            "payload" to mapOf(
                "sender" to serviceName,
                "level" to "INFO",
                "message" to "[${className}] ${message}",
                "traceId" to traceId,
                "ts" to System.currentTimeMillis()
            )
        )
        val json = objectMapper.writeValueAsString(payload)
        kafkaTemplate.send("system-logs-topic", json)
        println("[${serviceName}][${className}] ${message}")
    }
}
