package com.example.otc.dat2.kafka

import com.example.otc.common.doc.ApplicationApprovedDoc
import com.example.otc.dat2.mongo.MongoProjectionService
import com.example.otc.common.lang.OtcObjectMapper
import com.example.otc.common.lang.Otc10
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ApplicationsProjector(
    private val objectMapper: OtcObjectMapper,
    private val projectionService: MongoProjectionService
) {
    private val logger = Otc10.getLogger(ApplicationsProjector::class.java)

    @KafkaListener(
        topics = ["otc.applications"],
        groupId = "otc-projection",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun onMessage(payload: String) {
        try {
            val node = objectMapper.readTree(payload)
            when {
                node.has("approvedAt") -> {
                    val doc = objectMapper.readValue(payload, ApplicationApprovedDoc::class.java)
                    projectionService.projectApproved(doc)
                    logger.info("[Projector] projected ApplicationApproved to Mongo appId={}", doc.applicationId)
                }
                node.has("occurredAt") && node.has("reason") -> {
                    val appId = node.path("applicationId").asText()
                    val reason = node.path("reason").asText()
                    logger.info("[Projector] received ApplicationFailed-like event aggId={} reason={}", appId, reason)
                }
                node.has("occurredAt") && node.has("errorCode") && node.has("message") -> {
                    val appId = node.path("applicationId").asText()
                    val errorCode = node.path("errorCode").asText()
                    val message = node.path("message").asText()
                    logger.info("[Projector] received PermissionGrantFailedEvt aggId={} errorCode={} message={}", appId, errorCode, message)
                }
                node.has("revokedAt") -> {
                    val appId = node.path("applicationId").asText()
                    logger.info("[Projector] received PermissionRevokedDoc aggId={}", appId)
                }
                node.has("canceledAt") -> {
                    val appId = node.path("applicationId").asText()
                    logger.info("[Projector] received ApplicationCanceledDoc aggId={}", appId)
                }
                else -> {
                    logger.warn("[Projector] unknown event payload shape; payload={}", payload)
                }
            }
        } catch (ex: Exception) {
            logger.error("[Projector] failed to process event. payload={}", payload, ex)
        }
    }
}