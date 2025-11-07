package com.example.otc.dsv.l
/**
 * L 层承兑规则：
 * - 从配置中心（classpath 资源）加载承兑规则（最低保证金、允许币种），并缓存热更新。
 * - 提供统一的 `current()` 访问接口给业务校验使用。
 */

import com.example.otc.common.lang.OtcObjectMapper
import com.example.otc.common.lang.OtcAtomicReference
import com.example.otc.common.lang.OtcResourceLoader
import com.example.otc.common.lang.Otc10
import org.springframework.stereotype.Service
import org.springframework.scheduling.annotation.Scheduled
import com.example.otc.common.lang.OtcBigDecimal

data class AcceptorRules(
    val minDepositAmount: OtcBigDecimal,
    val allowedCurrencies: List<String>
)

@Service
class L_AcceptorRules(
    private val resourceLoader: OtcResourceLoader,
    private val objectMapper: OtcObjectMapper
) {
    private val logger = Otc10.getLogger(L_AcceptorRules::class.java)
    private val cache = OtcAtomicReference(AcceptorRules(OtcBigDecimal(1000), listOf("USDT", "BTC")))

    fun current(): AcceptorRules = cache.get()

    @Scheduled(fixedDelay = 60000)
    /**
     * 定时刷新：每 60 秒读取最新规则并更新缓存，失败时记录警告日志。
     */
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