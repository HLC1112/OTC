package com.example.otc.dsv.l

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import org.springframework.scheduling.annotation.Scheduled
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicReference

data class AcceptorRules(
    val minDepositAmount: BigDecimal,
    val allowedCurrencies: List<String>
)

@Service
class L_AcceptorRules(
    private val resourceLoader: ResourceLoader,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(L_AcceptorRules::class.java)
    private val cache = AtomicReference(AcceptorRules(BigDecimal(1000), listOf("USDT", "BTC")))

    fun current(): AcceptorRules = cache.get()

    @Scheduled(fixedDelay = 60000)
    fun refresh() {
        try {
            val res = resourceLoader.getResource("classpath:config/otc/acceptor-rules.json")
            if (res.exists()) {
                val node = objectMapper.readTree(res.inputStream)
                val min = node.get("min_deposit_amount").decimalValue()
                val allowed = node.get("allowed_currencies").map { it.asText() }
                val rules = AcceptorRules(minDepositAmount = min, allowedCurrencies = allowed)
                cache.set(rules)
                logger.info("[L] acceptor rules refreshed: min={} allowed={}", min, allowed)
            }
        } catch (ex: Exception) {
            logger.warn("[L] acceptor rules refresh failed: {}", ex.message)
        }
    }
}