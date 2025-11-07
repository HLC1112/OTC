package com.example.otc.dsv.adapter
/**
 * 事件总线发布器：
 * - 将 `ApplicationApprovedDoc` 序列化为 JSON 并发布到 Kafka 主题 `otc.applications`。
 * - 负责记录发布日志，便于审计和问题追踪。
 */

import com.example.otc.common.doc.ApplicationApprovedDoc
import com.example.otc.common.lang.OtcKafkaTemplate
import com.example.otc.common.lang.OtcObjectMapper
import com.example.otc.common.lang.Otc10
import org.springframework.stereotype.Component

@Component
class EventBusPublisher(
    private val kafkaTemplate: OtcKafkaTemplate<String, String>,
    private val objectMapper: OtcObjectMapper
) {
    private val logger = Otc10.getLogger(EventBusPublisher::class.java)
    private val topic = "otc.applications"

    /**
     * 发布“申请已批准”事件到事件总线。
     */
    fun publishApplicationApproved(doc: ApplicationApprovedDoc) {
        val json = objectMapper.writeValueAsString(doc)
        kafkaTemplate.send(topic, doc.applicationId, json)
        logger.info("[EventBus] published ApplicationApproved to topic={} appId={}", topic, doc.applicationId)
    }
}