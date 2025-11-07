package com.example.otc.dsv.adapter
/**
 * API 网关控制器：
 * - 对外暴露申请成为承兑商的 HTTP 接口。
 * - 将接收到的 `ApplyForAcceptorCmd` 命令交给 `SqlPlanRunner` 执行指定的 SQL 状态机计划。
 * - 返回生成的 `applicationId`，并记录审计日志。
 */

import com.example.otc.common.cmd.ApplyForAcceptorCmd
import com.example.otc.common.error.ErrorResponse
import com.example.otc.fsmdsv.engine.SqlPlanRunner
import com.example.otc.common.lang.Otc10
import com.example.otc.common.lang.OtcResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiGatewayController(
    private val runner: SqlPlanRunner
) {
    private val logger = Otc10.getLogger(ApiGatewayController::class.java)

    @PostMapping("/otc/acceptor/apply")
    /**
     * 接收承兑申请请求，执行成功方案 `FSM_Apply_Acceptor_Success.sql`，返回申请单号。
     */
    fun apply(@RequestBody cmd: ApplyForAcceptorCmd): OtcResponseEntity<Any> {
        val appId = runner.run(cmd, "FSM_Apply_Acceptor_Success.sql")
        logger.info("[API] Accepted application. appId={}", appId)
        return OtcResponseEntity.accepted().body(mapOf("applicationId" to appId))
    }
}

@Deprecated("LegacyService 已废弃；请通过 ApiGatewayController 使用 SqlPlanRunner")
class LegacyService