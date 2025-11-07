package com.example.otc.wrap.kafka
/**
 * Kafka 封装 V1：
 * - 统一发送消息并记录日志，便于审计与问题追踪。
 */

import com.example.otc.common.lang.OtcKafkaTemplate
import com.example.otc.common.lang.Otc10

object KafkaWrapV1 {
    private val logger = Otc10.getLogger(KafkaWrapV1::class.java)

    fun send(kafkaTemplate: OtcKafkaTemplate<String, String>, topic: String, key: String, value: String) {
        kafkaTemplate.send(topic, key, value)
        logger.info("[WRAP.kafka.send.v1] topic={} key={}", topic, key)
    }
}