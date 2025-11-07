package com.example.otc.dsv.adapter
/**
 * FSM 演示控制器：
 * - 一次请求可触发多个 SQL 状态机计划，用于演示和联调。
 * - 将入口命令与出口 Outbox 事件一起汇总输出到日志与响应，便于观察完整流程。
 */

import com.example.otc.common.cmd.ApplyForAcceptorCmd
import com.example.otc.common.lang.Otc10
import com.example.otc.common.lang.OtcResponseEntity
import com.example.otc.dat1.mysql.OutboxEvtJpa
import com.example.otc.fsmdsv.engine.SqlPlanRunner
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import com.example.otc.common.lang.OtcBigDecimal

data class DemoCase(
    val plan: String,
    val userId: String,
    val depositAmount: OtcBigDecimal,
    val depositCurrency: String
)

data class MultiDemoRequest(
    val cases: List<DemoCase>
)

data class EventSummary(
    val id: String,
    val aggregateId: String,
    val eventType: String,
    val createdAt: String,
    val sentAt: String?
)

@RestController
class FsmDemoController(
    private val runner: SqlPlanRunner,
    private val outboxJpa: OutboxEvtJpa
) {
    private val logger = Otc10.getLogger(FsmDemoController::class.java)

    /**
     * 触发多个 FSM SQL 计划，并将入口（命令）与出口（Outbox 事件）一并输出。
     */
    @PostMapping("/otc/fsm/demo/multi")
    fun runMulti(@RequestBody req: MultiDemoRequest): OtcResponseEntity<Any> {
        if (req.cases.isEmpty()) {
            return OtcResponseEntity.badRequest().body(mapOf("error" to "cases must not be empty"))
        }

        val aggregateIds = mutableListOf<String>()

        req.cases.forEachIndexed { idx, c ->
            val cmd = ApplyForAcceptorCmd(
                userId = c.userId,
                depositAmount = c.depositAmount,
                depositCurrency = c.depositCurrency
            )
            logger.info("[DEMO] Ingress[{}] plan={} userId={} amount={} currency={}",
                idx, c.plan, c.userId, c.depositAmount, c.depositCurrency)
            val appId = runner.run(cmd, c.plan)
            aggregateIds.add(appId)
            logger.info("[DEMO] Ingress[{}] accepted appId={} plan={}",
                idx, appId, c.plan)
        }

        val outboxRows = outboxJpa.findAllByAggregateIdInOrderByCreatedAtAsc(aggregateIds)
        val events = outboxRows.map { r ->
            EventSummary(
                id = r.id,
                aggregateId = r.aggregateId,
                eventType = r.eventType,
                createdAt = r.createdAt.toString(),
                sentAt = r.sentAt?.toString()
            )
        }

        events.forEachIndexed { idx, e ->
            logger.info("[DEMO] Egress[{}] type={} aggId={} created={} sent={}",
                idx, e.eventType, e.aggregateId, e.createdAt, e.sentAt)
        }

        val summary = mapOf(
            "applications" to aggregateIds,
            "events" to events
        )

        return OtcResponseEntity.ok().body(summary)
    }
}