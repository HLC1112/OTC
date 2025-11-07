package com.example.otc.dat2.kafka

import com.example.otc.dat1.mysql.OutboxEvtJpa
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Sample Outbox forwarder (simulating CDC). In production, Debezium reads T_OUTBOX_EVT.
 * This job demonstrates minimal closed loop by forwarding unsent outbox rows to Kafka.
 */
@Component
class OutboxForwarder(
    private val outboxEvtJpa: OutboxEvtJpa,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(OutboxForwarder::class.java)
    private val topic = "otc.applications"

    @Scheduled(fixedDelay = 10000)
    fun forward() {
        val rows = outboxEvtJpa.findTop50BySentAtIsNullOrderByCreatedAtAsc()
        if (rows.isEmpty()) return
        rows.forEach { row ->
            kafkaTemplate.send(topic, row.aggregateId, row.payloadJson)
            row.sentAt = Instant.now()
            outboxEvtJpa.save(row)
            logger.info("[Outbox] forwarded event type={} aggId={} to topic={}", row.eventType, row.aggregateId, topic)
        }
    }
}