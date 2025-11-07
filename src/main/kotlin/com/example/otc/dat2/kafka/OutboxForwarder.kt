package com.example.otc.dat2.kafka

import com.example.otc.dat1.mysql.OutboxEvtJpa
import com.example.otc.common.lang.Otc3
import com.example.otc.common.lang.OtcKafkaTemplate
import com.example.otc.wrap.kafka.KafkaWrapV1
import com.example.otc.common.lang.Otc10
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Sample Outbox forwarder (simulating CDC). In production, Debezium reads T_OUTBOX_EVT.
 * This job demonstrates minimal closed loop by forwarding unsent outbox rows to Kafka.
 */
@Component
class OutboxForwarder(
    private val outboxEvtJpa: OutboxEvtJpa,
    private val kafkaTemplate: OtcKafkaTemplate<String, String>
) {
    private val logger = Otc10.getLogger(OutboxForwarder::class.java)
    private val topic = "otc.applications"

    @Scheduled(fixedDelay = 10000)
    fun forward() {
        val rows = outboxEvtJpa.findTop50BySentAtIsNullOrderByCreatedAtAsc()
        if (rows.isEmpty()) return
        rows.forEach { row ->
            KafkaWrapV1.send(kafkaTemplate, topic, row.aggregateId, row.payloadJson)
            row.sentAt = Otc3.now()
            outboxEvtJpa.save(row)
            logger.info("[Outbox] forwarded event type={} aggId={} to topic={}", row.eventType, row.aggregateId, topic)
        }
    }
}