package com.example.otc.fsmdsv.engine
/**
 * SQL 计划执行器：
 * - 按加载的 `SqlFsmConfig` 逐步执行 SQL（SELECT 使用查询，非 SELECT 使用更新）。
 * - 根据计划名构造不同的 `payloadJson`，用于出站事件或审计记录。
 * - 提供默认路由 `run(cmd)`，调用成功方案 `FSM_Apply_Acceptor_Success.sql`。
 */

import com.example.otc.common.cmd.ApplyForAcceptorCmd
import com.example.otc.common.doc.ApplicationApprovedDoc
import com.example.otc.common.lang.Otc10
import com.example.otc.common.lang.OtcObjectMapper
import com.example.otc.common.lang.Otc1
import com.example.otc.common.lang.Otc3
import com.example.otc.common.lang.Otc4
import com.example.otc.fsmdsv.config.SqlFsmConfigLoader
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.annotation.Transactional

@Otc1
class SqlPlanRunner(
    private val loader: SqlFsmConfigLoader,
    private val jdbc: NamedParameterJdbcTemplate,
    private val mapper: OtcObjectMapper
) {
    private val logger = Otc10.getLogger(SqlPlanRunner::class.java)

    @Transactional
    fun run(cmd: ApplyForAcceptorCmd, planName: String): String {
        val plan = loader.load(planName)
        val applicationId = Otc4.randomUUID().toString()
        val outboxId = Otc4.randomUUID().toString()
        val permissionId = Otc4.randomUUID().toString()

        val payloadJson: String = when {
            planName.contains("Apply_Acceptor_Success", ignoreCase = true) -> {
                val payloadDoc = ApplicationApprovedDoc(
                    applicationId = applicationId,
                    userId = cmd.userId,
                    approvedAt = Otc3.now(),
                    details = mapOf("permission" to "OTC_ACCEPTOR")
                )
                mapper.writeValueAsString(payloadDoc)
            }
            planName.contains("Apply_Acceptor_Failed", ignoreCase = true) -> {
                mapper.writeValueAsString(
                    mapOf(
                        "applicationId" to applicationId,
                        "userId" to cmd.userId,
                        "occurredAt" to Otc3.now().toString(),
                        "reason" to "Validation failed"
                    )
                )
            }
            planName.contains("Permission_Timeout", ignoreCase = true) -> {
                mapper.writeValueAsString(
                    mapOf(
                        "applicationId" to applicationId,
                        "userId" to cmd.userId,
                        "occurredAt" to Otc3.now().toString(),
                        "reason" to "Permission grant timeout"
                    )
                )
            }
            planName.contains("Permission_WriteFailed", ignoreCase = true) -> {
                mapper.writeValueAsString(
                    mapOf(
                        "applicationId" to applicationId,
                        "userId" to cmd.userId,
                        "occurredAt" to Otc3.now().toString(),
                        "reason" to "Permission write failed"
                    )
                )
            }
            planName.contains("Permission_Revoke", ignoreCase = true) -> {
                mapper.writeValueAsString(
                    mapOf(
                        "applicationId" to applicationId,
                        "userId" to cmd.userId,
                        "permission" to "OTC_ACCEPTOR",
                        "revokedAt" to Otc3.now().toString()
                    )
                )
            }
            planName.contains("Application_Cancel", ignoreCase = true) -> {
                mapper.writeValueAsString(
                    mapOf(
                        "applicationId" to applicationId,
                        "userId" to cmd.userId,
                        "canceledAt" to Otc3.now().toString(),
                        "reason" to "User canceled"
                    )
                )
            }
            else -> {
                // default to success payload for backward compatibility
                val payloadDoc = ApplicationApprovedDoc(
                    applicationId = applicationId,
                    userId = cmd.userId,
                    approvedAt = Otc3.now(),
                    details = mapOf("permission" to "OTC_ACCEPTOR")
                )
                mapper.writeValueAsString(payloadDoc)
            }
        }

        val params = mapOf(
            "applicationId" to applicationId,
            "userId" to cmd.userId,
            "depositAmount" to cmd.depositAmount,
            "depositCurrency" to cmd.depositCurrency,
            "permissionId" to permissionId,
            "outboxId" to outboxId,
            "payloadJson" to payloadJson
        )

        plan.steps.forEach { (step, lines) ->
            val statements = toStatements(lines)
            statements.forEach { sql ->
                if (sql.startsWith("SELECT", ignoreCase = true)) {
                    jdbc.queryForList(sql, params)
                } else {
                    jdbc.update(sql, params)
                }
            }
            logger.info("[SQL-RUNNER] step={} executed statements={}", step, statements.size)
        }

        return applicationId
    }

    @Transactional
    fun run(cmd: ApplyForAcceptorCmd): String {
        // default route calls success plan
        return run(cmd, "FSM_Apply_Acceptor_Success.sql")
    }

    private fun toStatements(lines: List<String>): List<String> {
        val buf = StringBuilder()
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("--")) return@forEach
            buf.append(line).append('\n')
        }
        return buf.toString()
            .split(';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}