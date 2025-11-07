package com.example.otc.dsv.adapter

import com.example.otc.common.cmd.ApplyForAcceptorCmd
import com.example.otc.common.error.ErrorResponse
import com.example.otc.dsv.fsm.FSM_1
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiGatewayController(
    private val saga: FSM_1
) {
    private val logger = LoggerFactory.getLogger(ApiGatewayController::class.java)

    @PostMapping("/otc/acceptor/apply")
    fun apply(@RequestBody cmd: ApplyForAcceptorCmd): ResponseEntity<Any> {
        val appId = saga.apply(cmd)
        logger.info("[API] Accepted application. appId={}", appId)
        return ResponseEntity.accepted().body(mapOf("applicationId" to appId))
    }
}

@Deprecated("LegacyService is deprecated; use AcceptorApplicationSaga via ApiGatewayController")
class LegacyService