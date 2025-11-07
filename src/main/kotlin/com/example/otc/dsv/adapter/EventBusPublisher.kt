package com.example.otc.dsv.adapter

import com.example.otc.common.doc.ApplicationApprovedDoc
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class EventBusPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(EventBusPublisher::class.java)
    private val topic = "otc.applications"

    fun publishApplicationApproved(doc: ApplicationApprovedDoc) {
        val json = objectMapper.writeValueAsString(doc)
        kafkaTemplate.send(topic, doc.applicationId, json)
        logger.info("[EventBus] published ApplicationApproved to topic={} appId={}", topic, doc.applicationId)
    }
}