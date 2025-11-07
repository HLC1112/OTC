package com.example.otc.dat2.kafka

import com.example.otc.common.doc.ApplicationApprovedDoc
import com.example.otc.dat2.mongo.MongoProjectionService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ApplicationsProjector(
    private val objectMapper: ObjectMapper,
    private val projectionService: MongoProjectionService
) {
    private val logger = LoggerFactory.getLogger(ApplicationsProjector::class.java)

    @KafkaListener(
        topics = ["otc.applications"],
        groupId = "otc-projection",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun onMessage(payload: String) {
        try {
            val doc = objectMapper.readValue(payload, ApplicationApprovedDoc::class.java)
            projectionService.projectApproved(doc)
            logger.info("[Projector] projected ApplicationApproved to Mongo appId={}", doc.applicationId)
        } catch (ex: Exception) {
            logger.error("[Projector] failed to project ApplicationApproved. payload={}", payload, ex)
        }
    }
}