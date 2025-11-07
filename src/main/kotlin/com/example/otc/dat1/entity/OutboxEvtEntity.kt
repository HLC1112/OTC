package com.example.otc.dat1.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import com.example.otc.common.lang.Otc3

@Entity
@Table(name = "T_OUTBOX_EVT")
class OutboxEvtEntity(
    @Id
    var id: String = "",

    @Column(name = "aggregate_type", nullable = false)
    var aggregateType: String = "",

    @Column(name = "aggregate_id", nullable = false)
    var aggregateId: String = "",

    @Column(name = "event_type", nullable = false)
    var eventType: String = "",

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    var payloadJson: String = "",

    @Column(name = "created_at", nullable = false)
    var createdAt: Otc3 = Otc3.now(),

    @Column(name = "sent_at", nullable = true)
    var sentAt: Otc3? = null
)